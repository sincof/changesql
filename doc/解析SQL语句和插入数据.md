# 解析SQL语句

解析的内容全部参考官方文档[mysqlType](https://dev.mysql.com/doc/refman/8.0/en/numeric-types.html)

解析的内容就先这样不管了，等完成多线程插入再看看如何修改

### DECIMAL

1. **DECIMAL 如果总长度小于等于M，但是小数点的位数超过了N会导致超过精度后的数字被删除！这不会影响插入的结果**

   ```mysql
   create table `a`(
   	id char (32) NOT NULL DEFAULT "9999" primary key,
   	par1 INTEGER default 10,
   	par2 DECIMAL(10, 5) default 10.9
   );
   
   INSERT into a values ('18', 1, 12345.11111111111111111);
   -- 数据库中的结果 12345.11111
   ```

2. 如果未设定N，会导致只有整数位，没有小数位

   ```mysql
   create table `a`(
   	id char (32) NOT NULL DEFAULT "9999" primary key,
   	par1 INTEGER default 10,
   	par2 DECIMAL default 10.9
   );
   
   INSERT into a values ('18', 1, 12345.11111111111111111);
   -- 数据库中的结果 12345
   ```

   

### STRING类型

1. char char如果后面不设置长度，即长度为1

   ```mysql
   create table `a`(
   	id char NOT NULL DEFAULT "" primary key
   );
   INSERT into a values ('1');
   
   CHAR,       // 0-255 bytes 		如果不设置长度值，长度为1
   VARCHAR,    // 0-65535 bytes 	如果不设置长度值，直接报错，不能不设置长度值
   -- 下面的是不能设置字段长度 否则会报错
   TINYBLOB,   // 0-255 bytes 		如果不设置长度值，不超过 255 个字符的二进制字符串
   TINYTEXT,   // 0-255 bytes 		如果不设置长度值，不超过 255 个字符的二进制字符串
   -- 下面能设置字段长度
   BLOB,       // 0-65 535 bytes 二进制形式的长文本数据
   TEXT,       // 0-65 535 bytes 长文本数据
   -- 下面的是不能设置字段长度 否则会报错
   MEDIUMBLOB, // 0-16 777 215 bytes 二进制形式的中等长度文本数据
   MEDIUMTEXT, // 0-16 777 215 bytes 中等长度文本数据
   LONGBLOB,   // 0-4 294 967 295 bytes二进制形式的极大文本数据
   LONGTEXT;   // 0-4 294 967 295 bytes 极大文本数据
   ```

能设置长度的只有char varchar blob text，其中如果要使用必须要设置长度值的是char（不设置长度为1） 和varchar

当我们对text设置长度值的时候，并且长度值小于等于63，会自动退化到tinytext，当设置的值大于等于64的时候，关于这列的text长度定义会自动删除.

**！！可能存在问题** 我们只对char类型和varchar类型进行长度比较，其他直接按照

#### BLOB & TEXT

区别：BLOB值被视为二进制字符串（字节字符串）。他们没有字符集，排序和比较基于列值中字节的数值。TEXT值被视为非二进制字符串（字符串）。他们有一个字符集，并且根据字符集的排序规则对值进行排序和比较。

BLOB, TEXT, GEOMETRY or JSON column 'id' can't have a default value。

这玩意不能有初始值。

# 插入数据注意内容
## 插入数据过程

需要注意的是，多个源端的数据中的表和数据可能存在冲突，对于同表的数据有冲突的情况（注：每个表都有类型为datetime的updated_at字段）： 

- 如果有主键或者非空唯一索引，唯一索引相同的情况下，以行updated_at时间戳来判断是否覆盖数据，如果updated_at比原来的数据更新，那么覆盖数据；否则忽略数据。不存在主键相同，updated_at时间戳相同，但数据不同的情况。
- 如果没有主键或者非空唯一索引，如果除updated_at其他数据都一样，只更新updated_at字段；否则，插入一条新的数据。
注：每个表都有类型为datetime的updated_at字段

1. 构建parparedStatement，利用parparedstatement来实现查询数据和插入数据两个操作
2. 由于数据太大，在本地判断数据出没出现过很难控制使用内存大小的（4核心，8GB内存），很容易爆掉内存，因此肯定不能再本地去判断数据是否重复，只能通过数据库来知道数据是否存在重复的情况
3. **这个项目对于数据库的操作只有插入和更新两种操作，对数据插入的流程如下：**
   1. 有主键或者有非空唯一索引的
      1. 主键或者非空唯一索引去查询数据库更新时间
         1. 如果数据库中存在以该主键或者非空唯一索引的列
            1. update_at更新，更新整个列
            2. 否则不做任何操作
         2. 如果数据库中不存在以该主键或者非空唯一索引的列
            1. 将该行数据插入
   2. 如果没有主键或者非空唯一索引的
      1. 以该行的除了update_at的其他数据去查询数据库，如果存在
         1. 更新update_at
      2. 如果不存在
         1. 将改行数据插入数据库

流程需要改动，由于batch insert 不会放回结果值，只会放回成功或者没成功

**这个项目对于数据库的操作只有插入和更新两种操作，对数据插入的流程如下：**

1. 有主键或者有非空唯一索引的
   1. 主键或者非空唯一索引去查询数据库更新时间
      1. 如果数据库中存在以该主键或者非空唯一索引的列
         1. update_at更新，更新整个列
         2. 否则不做任何操作
      2. 如果数据库中不存在以该主键或者非空唯一索引的列
         1. 将该行数据插入
2. 如果没有主键或者非空唯一索引的
   1. 以该行的除了update_at的其他数据去查询数据库，如果存在
      1. 更新update_at
   2. 如果不存在
      1. 将改行数据插入数据库

1. 由于机器在和远端机器通信的过程中肯定会有延迟的，如果一条一条查询，效率肯定不高，故利用**batch insert**。但是batch insert效率还没有达到最高。

   ```java
   // Disable auto-commit
   connection.setAutoCommit(false);
   
           // Create a prepared statement
           String sql = "INSERT INTO mytable (xxx), VALUES(?)";
           PreparedStatement pstmt = connection.prepareStatement(sql);
   
           Object[] vals=set.toArray();
           for (int i=0; i<vals.length; i++) {
   pstmt.setString(1, vals[i].toString());
   pstmt.addBatch();
   }
   
   // Execute the batch
   int [] updateCounts = pstmt.executeBatch();
   System.out.append("inserted "+updateCounts.length);
   ```

   可以利用参数设置，将batch发送的数据转换成命令列表和参数，减少发送数据包的体积

   [参考链接1](https://stackoverflow.com/questions/2993251/jdbc-batch-insert-performance)

   I had a similar performance issue with mysql and solved it by setting the *useServerPrepStmts* and the *rewriteBatchedStatements* properties in the connection url.

   ```java
   Connection c = DriverManager.getConnection("jdbc:mysql://host:3306/db?useServerPrepStmts=false&rewriteBatchedStatements=true", "username", "password");
   ```

   `rewriteBatchedStatements=true` is the important parameter. `useServerPrepStmts` is already false by default, and even changing it to true doesn't make much difference in terms of batch insert performance.

   Now I think is the time to write how `rewriteBatchedStatements=true` improves the performance so dramatically. It does so by `rewriting of prepared statements for INSERT into multi-value inserts when executeBatch()` ([Source](http://dev.mysql.com/doc/connector-j/en/connector-j-reference-configuration-properties.html)). That means that instead of sending the following `n` INSERT statements to the mysql server each time `executeBatch()` is called :

   ```mysql
   INSERT INTO X VALUES (A1,B1,C1)
   INSERT INTO X VALUES (A2,B2,C2)
   ...
   INSERT INTO X VALUES (An,Bn,Cn)
   ```

   将这种格式的改成

   ```mysql
   INSERT INTO X VALUES (A1,B1,C1),(A2,B2,C2),...,(An,Bn,Cn)
   ```

insert

```mysql
INSERT INTO table_name
VALUES (value1,value2,value3,...);

INSERT INTO table_name (column1,column2,column3,...)
VALUES (value1,value2,value3,...);
```

delete

```mysql
DELETE FROM table_name
WHERE some_column=some_value;
```

update

```mysql
UPDATE table_name
SET column1=value1,column2=value2,...
WHERE some_column=some_value;
```

select

```mysql
SELECT column_name,column_name
FROM table_name
WHERE column_name operator value;
```



[批处理插入参考链接](https://blog.csdn.net/cunchi4221/article/details/107471675)

[MYSQL的数据类型和JAVA之间的联系和区别](https://blog.csdn.net/u013991521/article/details/80834875)

