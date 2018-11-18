
## 事务
事务：处于同一个事务中的操作是一个工作单元,要么全部执行成功,要么全部执行失败。
在企业级应用程序开发中，事务管理必不可少的技术，用来确保数据的完整性和一致性。 

事务有四个特性：ACID
- 原子性（Atomicity）：事务是一个原子操作，由一系列动作组成。事务的原子性确保动作要么全部完成，要么完全不起作用。
- 一致性（Consistency）：一旦事务完成（不管成功还是失败），系统必须确保它所建模的业务处于一致的状态，而不会是部分完成部分失败。在现实中的数据不应该被破坏。
- 隔离性（Isolation）：可能有许多事务会同时处理相同的数据，因此每个事务都应该与其他事务隔离开来，防止数据损坏。
- 持久性（Durability）：一旦事务完成，无论发生什么系统错误，它的结果都不应该受到影响，这样就能从任何系统崩溃中恢复过来。通常情况下，事务的结果被写到持久化存储器中。

在企业级应用中，多用户访问数据库是常见的场景，这就是所谓的事务的并发。事务并发所可能存在的问题：
- 脏读：一个事务读到另一个事务未提交的更新数据。
- 不可重复读：一个事务两次读同一行数据，可是这两次读到的数据不一样。
- 幻读：一个事务执行两次查询，但第二次查询比第一次查询多出了一些数据行。

**不可重复读与幻读的区别**  

不可重复读的重点是修改：同样的条件, 第1次和第2次读取出来的值不一样了 ;
幻读的重点在于新增或者删除： 同样的条件, 第1次和第2次读取出来的记录数不一样 。

从总的结果来看, 似乎不可重复读和幻读都表现为两次读取的结果不一致。但如果你从控制的角度来看, 两者的区别就比较大。 
对于前者, 只要锁住满足条件的记录。 
对于后者, 要锁住满足条件及其相近的记录。


## Spring事务管理
Spring事务管理涉及的接口的联系如下：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181118212720779.png)
### Spring事务管理器
Spring并不直接管理事务，而是提供了多种事务管理器，他们将事务管理的职责委托给Hibernate或者JTA等持久化机制所提供的相关平台框架的事务来实现。

Spring事务管理器的接口是org.springframework.transaction.PlatformTransactionManager，通过这个接口，Spring为各个平台如JDBC、Hibernate等都提供了对应的事务管理器，但是具体的实现就是各个平台自己的事情了。此接口的内容如下：
```javascript
Public interface PlatformTransactionManager()...{  
    // 由TransactionDefinition得到TransactionStatus对象
    TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException; 
    // 提交
    Void commit(TransactionStatus status) throws TransactionException;  
    // 回滚
    Void rollback(TransactionStatus status) throws TransactionException;  
    } 
```
具体的事务管理机制对Spring来说是透明的，Spring事务管理为不同的事务API提供一致的编程模型，如JTA、JDBC、Hibernate、JPA。下面分别介绍各个平台框架实现事务管理的机制。

###  Spring事务属性
事务管理器接口PlatformTransactionManager通过getTransaction(TransactionDefinition definition)方法来得到事务，这个方法里面的参数是TransactionDefinition类，这个类定义了一些基本的事务属性。 
事务属性可以理解成事务的一些基本配置，描述了事务策略如何应用到方法上。
```javascript
TransactionDefinition接口内容如下：

public interface TransactionDefinition {
    int getPropagationBehavior(); // 返回事务的传播行为
    int getIsolationLevel(); // 返回事务的隔离级别，事务管理器根据它来控制另外一个事务可以看到本事务内的哪些数据
    int getTimeout();  // 返回事务必须在多少秒内完成
    boolean isReadOnly(); // 事务是否只读，事务管理器能够根据这个返回值进行优化，确保事务是只读的
} 
```
#### 1. 事务的传播行为
当事务方法被另一个事务方法调用时，必须指定事务应该如何传播。Spring定义了七种传播行为：
名称 	|值 |	解释
-------- |-------- |-------- |
PROPAGATION_REQUIRED 	|0 	|支持当前事务，如果当前没有事务，就新建一个事务。这是最常见的选择，也是Spring默认的事务的传播。
PROPAGATION_SUPPORTS 	|1 	|支持当前事务，如果当前没有事务，就以非事务方式执行。
PROPAGATION_MANDATORY |	2 	|支持当前事务，如果当前没有事务，就抛出异常。
PROPAGATION_REQUIRES_NEW 	|3 	|新建事务，如果当前存在事务，把当前事务挂起。
PROPAGATION_NOT_SUPPORTED |	4 	|以非事务方式执行操作，如果当前存在事务，就把当前事务挂起。
PROPAGATION_NEVER 	|5 	|以非事务方式执行，如果当前存在事务，则抛出异常。
#### 2. 事务的隔离级别
名称 	 |值 	 |解释
-------- | ------------- | -----
ISOLATION_DEFAULT 	 |-1 	 |这是一个PlatfromTransactionManager默认的隔离级别，使用数据库默认的事务隔离级别。
ISOLATION_READ_UNCOMMITTED 	 |1 	 |这是事务最低的隔离级别，它充许另外一个事务可以看到这个事务未提交的数据。这种隔离级别会产生脏读，不可重复读和幻读。
ISOLATION_READ_COMMITTED 	 |2 | 	保证一个事务修改的数据提交后才能被另外一个事务读取。可以阻止脏读，但是幻读或不可重复读仍有可能发生。
ISOLATION_REPEATABLE_READ 	 |4 	 |对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改，可以阻止脏读和不可重复读，但幻读仍有可能发生。
ISOLATION_SERIALIZABLE 	 |8 	 |最高的隔离级别，完全服从ACID的隔离级别，确保阻止脏读、不可重复读以及幻读，也是最慢的事务隔离级别，事务被处理为顺序执行。

#### 3. 只读

事务的第三个特性是它是否为只读事务。如果事务只对后端的数据库进行该操作，数据库可以利用事务的只读特性来进行一些特定的优化。通过将事务设置为只读，你就可以给数据库一个机会，让它应用它认为合适的优化措施。

#### 4. 事务超时
为了使应用程序很好地运行，事务不能运行太长的时间。因为事务可能涉及对后端数据库的锁定，所以长时间的事务会不必要的占用数据库资源。事务超时就是事务的一个定时器，在特定时间内事务如果没有执行完毕，那么就会自动回滚，而不是一直等待其结束。

#### 5. 回滚规则
回滚规则定义了哪些异常会导致事务回滚而哪些不会。默认情况下，事务只有遇到运行期异常时才会回滚，而在遇到检查型异常时不会回滚（这一行为与EJB的回滚行为是一致的）。
但是你可以声明事务在遇到特定的检查型异常时像遇到运行期异常那样回滚。同样，你还可以声明事务遇到特定的异常不回滚，即使这些异常是运行期异常。

### Spring事务状态
调用PlatformTransactionManager接口的getTransaction()的方法得到的是TransactionStatus接口的一个实现，这个接口的内容如下：
```javascript
public interface TransactionStatus{
    boolean isNewTransaction(); // 是否是新的事物
    boolean hasSavepoint(); // 是否有恢复点
    void setRollbackOnly();  // 设置为只回滚
    boolean isRollbackOnly(); // 是否为只回滚
    boolean isCompleted; // 是否已完成
}
```
可以看出返回的结果是一些事务的状态，可用来检索事务的状态信息。

## 编程式事务
### 编程式和声明式事务的区别
Spring提供了对编程式事务和声明式事务的支持，编程式事务允许用户在代码中精确定义事务的边界，而声明式事务（基于AOP）有助于用户将操作与事务规则进行解耦。 
简单地说，编程式事务侵入到了业务代码里面，但是提供了更加详细的事务管理；而声明式事务由于基于AOP，所以既能起到事务管理的作用，又可以不影响业务代码的具体实现。

Spring提供两种方式的编程式事务管理，分别是：
- 使用PlatformTransactionManager
- 使用Spring提供的模板类TransactionTemplate

## 声明式事务
声明式事务管理有两种常用的方式，分别是：
- 基于tx和aop命名空间的xml配置文件
- 基于@Transactional注解
