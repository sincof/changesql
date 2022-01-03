# TDSQL数据迁移

## 计划与时间安排 12.16 - 1.18

8个数据源 - 库 - 表

50G数据

扫描全部的数据源，找到每个数据源中对应的数据库，
对每个相对应中的数据库的表建立索引，记录他们的表定义字符串，表数据的位置。
对每个数据库依次执行插入操作。

1.3 上午 完成SQL定义语句的字符串解析任务

1.3 下午 完成createDBaTB修改任务，字符串解析任务和createDBaTB的测试任务

1.3 晚上 完成单线程版插入 & 测试单线程版插入的功能

1.4 上午 线程池的编写

## 相应的分类以及其功能
Entity：一些实体类的定义，例如

1. 数据库实例

2. 数据表实例

   解析SQL语句，

Service:

1. 一些服务代码的编写

2. DBManager：

   1. getDBList：（程序开始时启动）负责搜索给定文件目录下各个源的database，找出相同的database下面有的相同的表，并且用MAP去索引他们database，然后每个database目录下面表格利用list去记录每个table示例。

      **注意去重和精度问题**

   2. createDBaTB：在开始时候调用，负责创建数据库和每个数据库下的表。

   3. insert2TB：向每个表插入数据

3. DBConnection：管理连接的一些服务基于JDBC & Mybatis

3. Thread: 实现线程池的管理

## 具体过程（待修改

### 每个线程干的活

1. ~~为所有的数据库源文件建立index~~

2. ~~按照数据库的顺序去一个个插入~~ 


1. 每个线程负责一个数据库，所以不会存在线程冲突的情况。（第一种方案）

   或者将每个线程负责每个表的插入？这样能不能实现资源的最大利用？（第二种方案）

### 多线程版本

1. 对应给定机器的核心数目，来确定线程的数目。
2. 并行对不同的库进行插入，插入的过程和单线程版本相同

## 注意的问题

1. 相同库下的相同的表的相同的列，可能存在表的精度不一致的情况，要取大的精度作为新的表格。 **建表语句字段的精度不够会导致导入的数据与文件的数据不一致，但和同结构的表里的数据是一致的。不同的比较方式会有不同的比较结果。**

   解决方案：对于每一个不同的表，都解析出他们的列，然后对于

## 程序可能存在的漏洞

1&2都是官方没有给出明确的比较精度更新条件可能导致的情况

1. 在compareColumnType的对于SQLNumberType的比较里面我没有对Decimal的情况进行特判，因为DECIMAL(M, N) 可能会比BIGINT 或者是DOUBLE精度更小的情况。**如果出现了导入数据不一致的情况，对这个判断进行修改** （默认选用了DECIMAL就会储存比BIGINT或者DOUBLE更大的整数）

   **DECIMAL 如果总长度小于等于M，但是小数点的位数超过了N会导致超过精度后的数字被删除！这不会影响插入的结果**

   ```mysql
   create table `a`(
   	id char (32) NOT NULL DEFAULT "9999" primary key,
   	par1 INTEGER default 10,
   	par2 DECIMAL(10, 5) default 10.9
   );
   
   INSERT into a values ('18', 1, 12345.11111111111111111);
   -- 数据库中的结果 12345.11111
   ```

   

2. 在compareColumnType的对于同为DECIMAL的比较我采用了选择使用M大的（可能需要选择N大的来保证小数点精度）

## 开发过程中的疑问

1. 对于之前提到的**精度**问题，如果char(5) varchar(5)应该选择哪一个？或者说直接用第一个遇到的就可以，但是这样系统使用checksum来判断的，checksum会判断表的定义么，那么如果系统选择的是varchar而不是char那不是错了？

   **我先采用字符串类型长度一致就如果两者里面由varchar 就使用varchar否则使用char （1/3）**


## 开发中遇到的问题

1. 创建数据库遇到的问题

   我想使用PreparedStatement 去创建数据库，他提供了可替换选项，但是结果是preparedstatement会在我传递过去的值两端加上‘’，导致插入无法成功 原因是prepared statement只支持值的修改，不应该在表名上修改。[连接](https://stackoverflow.com/questions/26582722/unable-to-create-database-using-prepared-statements-in-mysql) 在创建表和数据库的时候preparedstatement没有优势，实际上应该利用statement来完成创建任务。

   ```java
   String dbName = "first_database";
   Statement createDbStatement = connection.createStatement();
   createDbStatement.execute("CREATE DATABASE " + dbName);
   ```

   ```mysql
   -- create 语句示例
   CREATE TABLE if not exists `1`  (
     `id` bigint(20) unsigned NOT NULL,
     `a` float NOT NULL DEFAULT '0',
     `b` char(32) NOT NULL DEFAULT '',
     `updated_at` datetime NOT NULL DEFAULT '2021-12-12 00:00:00',
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8
   
   -- insert 语句示例
    INSERT INTO table_name values (column1, column2, column3, ...)
   ```
   
   
   
2. Create statement 的最后一个变量声明的末尾不应该包含逗号。。

## 项目中使用到的一些JAVA开源 / 非开源库 （感谢这些开源库 / 非开源库）

[mysql connector](https://dev.mysql.com/downloads/connector/j/) ：用来完成对数据库的连接（**非开源库**， 这个应该是没有开源的）

[HikariCP](https://github.com/brettwooldridge/HikariCP) ：数据库的连接池

[SLF4J](https://www.slf4j.org/) ：记录日志

[JUnit5](https://junit.org/junit5/) ：用来完成单元测试

[JSqlParser](https://github.com/JSQLParser/JSqlParser) : 解析create table statement由于在这个要求中，每个不同源的数据库可能存在表的列精度不一致的情况，为了解决这个问题，需要对SQL create statement进行解析获得不同列的不同的数据精度，然后依据精度情况对其进行更新