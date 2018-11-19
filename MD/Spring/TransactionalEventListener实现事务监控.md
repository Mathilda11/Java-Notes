
## 问题背景
在项目中，往往需要执行数据库操作后，发送消息或事件来异步调用其他组件执行相应的操作，例如：  
用户注册后发送激活码；  
配置修改后发送更新事件等。  
但是，数据库的操作如果还未完成，此时异步调用的方法查询数据库发现没有数据，这就会出现问题。

为了解决上述问题，Spring为我们提供了两种方式：  
(1) @TransactionalEventListener注解  
(2) 事务同步管理器TransactionSynchronizationManager  
以便我们可以在事务提交后再触发某一事件。

## 1. @TransactionalEventListener注解
在Spring4.2+，有一种叫做TransactionEventListener的方式，能够控制在事务的时候Event事件的处理方式。
我们知道，Spring的发布订阅模型实际上并不是异步的，而是同步的来将代码进行解耦。而TransactionEventListener仍是通过这种方式，只不过加入了回调的方式来解决，这样就能够在事务进行Commited，Rollback...等的时候才会去进行Event的处理。
代码如下：
```javascript
@Service("fooService") 
public class FooServiceImpl implements FooService {
    private static final Logger LOGGER = Logger.getLogger(FooServiceImpl.class); 
    @Override 
    public void insertFoo(Foo foo) throws MyTransactionException { 
    	LOGGER.info("[fooService] start insert foo"); 
    	ApplicationEventPublisher eventPublisher = EventPublisher.getApplicationEventPublisher(); 
    	if (null != eventPublisher) { 
    	    eventPublisher.publishEvent(new MyTransactionEvent("test", this)); 
    	} 
        LOGGER.info("[fooServive] finish insert foo"); 
    } 
} 

public class MyTransactionEvent extends ApplicationEvent { 
    private String name; 
    public MyTransactionEvent(String name, Object source) { 
        super(source); 
        this.name = name; 
    } 
    public String getName() { 
        return this.name; 
    } 
} 

@Component 
public class MyTransactionListener { 
    private static final Logger LOGGER = Logger.getLogger(MyTransactionListener.class);       
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) 
    public void hanldeOrderCreatedEvent(MyTransactionEvent event) { 
        LOGGER.info("transactionEventListener start"); 
        // do transaction event 
        LOGGER.info("event : " + event.getName());
        // finish transaction event
        LOGGER.info("transactionEventListener finish"); 
    } 
}
```

这样，只有当前事务提交之后，才会执行事件监听器的方法。其中参数phase默认为AFTER_COMMIT，共有四个枚举：
```javascript
/** 
* Fire the event before transaction commit. 
* @see TransactionSynchronization#beforeCommit(boolean) */ 
BEFORE_COMMIT, 
/** 
* Fire the event after the commit has completed successfully. 
* <p>Note: This is a specialization of {@link #AFTER_COMPLETION} and 
* therefore executes in the same after-completion sequence of events, 
* (and not in {@link TransactionSynchronization#afterCommit()}). 
* @see TransactionSynchronization#afterCompletion(int) 
* @see TransactionSynchronization#STATUS_COMMITTED */ AFTER_COMMIT, 
/** 
* Fire the event if the transaction has rolled back. 
* <p>Note: This is a specialization of {@link #AFTER_COMPLETION} and 
* therefore executes in the same after-completion sequence of events. 
* @see TransactionSynchronization#afterCompletion(int) 
* @see TransactionSynchronization#STATUS_ROLLED_BACK 
*/ 
AFTER_ROLLBACK, 
/** 
* Fire the event after the transaction has completed. 
* <p>For more fine-grained events, use {@link #AFTER_COMMIT} or 
* {@link #AFTER_ROLLBACK} to intercept transaction commit 
* or rollback, respectively. 
* @see TransactionSynchronization#afterCompletion(int) */ 
AFTER_COMPLETION
```
内部实现就是包装@TransactionalEventListener注解的方法，添加了一个适配器， ApplicationListenerMethodTransactionalAdapter，内部通过TransactionSynchronizationManager.registerSynchronization 注册一个同步器发布事务时,，记下event，然后注册一个同步器TransactionSynchronizationEventAdapter，当事务提交后， TransactionSynchronizationManager会回调上面注册的同步适配器，这里注册就是放入到一个ThreadLocal里面，通过它来传递参数。这时，TransactionSynchronizationEventAdapter内部才会真正的去调用hanldeOrderCreatedEvent方法。

## 2. TransactionSynchronizationManager方法
这种方法是通过手动的来注册回调来实现的。
```javascript
@EventListener 
public void afterRegisterSendMail(MessageEvent event) { 
    // Spring 4.2 之前 
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronizationAdapter() { 
        @Override 
        public void afterCommit() { 
            internalSendMailNotification(event); 
        } 
    }); 
}
```

@TransactionalEventListener底层也是这样实现的。

