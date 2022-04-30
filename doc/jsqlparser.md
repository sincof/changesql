## JSQLParser

这东西用来解析create table 语句，只具有解析功能，和将sql语句管理功能

主要是利用它自带的类型去解析，

```java
CreateTable createTable = (CreateTable) CCJSqlParserUtil.parse(statement);
```



