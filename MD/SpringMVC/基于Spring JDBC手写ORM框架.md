## ORM
对象关系映射（Object Relational Mapping，简称ORM）模式是一种为了解决面向对象与关系数据库存在的互不匹配的现象的技术。简单的说，ORM是通过使用描述对象和数据库之间映射的元数据，将程序中的对象自动持久化到关系数据库中。

## 基本框架结构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181119100300816.png)
- 配置文件 

db.properties
- Bean

ColumnInfo：封装表中一个字段的信息

Configuration：管理配置信息

JavaFieldGetSet：封装java属性和set/get源码信息

TableInfo：封装表结构信息

- 连接池

DBconnPool

- 核心类/接口

CallBack：回调

DBManager：根据配置信息，维持连接对象的管理

Query：负责查询，对外提供服务的核心类 

QueryFactory：Query工厂类

TableContext：负责获取管理数据库所有表结构和类结构的关系，并可以根据表结构生成类结构

TypeConvertor：负责java类型和数据库类型的相互转换
　　
MySqlQuery：负责mysql数据库的操作

MysqlTypeConvertor：mysql数据库类型和java类型的转换

- 工具类

JavaFileUtil：封装生成java源文件的常用操作

JDBCUtil：封装jdbc常用的操作

ReflectUtil：封装反射的常用操作

StringUtil： 封装String类型的常用操作
