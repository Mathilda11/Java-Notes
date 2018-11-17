
## 声明式事务的概述
Spring 的声明式事务管理在底层是建立在 AOP 的基础之上的。其本质是对方法前后进行拦截，然后在目标方法开始之前创建或者加入一个事务，在执行完目标方法之后根据执行情况提交或者回滚事务。

声明式事务最大的优点就是不需要通过编程的方式管理事务，这样就不需要在业务逻辑代码中掺杂事务管理的代码，只需在配置文件中做相关的事务规则声明（或通过等价的基于标注的方式），便可以将事务规则应用到业务逻辑中。因为事务管理本身就是一个典型的横切逻辑，正是 AOP 的用武之地。Spring 开发团队也意识到了这一点，为声明式事务提供了简单而强大的支持。

在开发中使用声明式事务，不仅因为其简单，更主要是因为这样使得纯业务代码不被污染，极大方便后期的代码维护。

和编程式事务相比，声明式事务唯一不足地方是，后者的最细粒度只能作用到方法级别，无法做到像编程式事务那样可以作用到代码块级别。但是即便有这样的需求，也存在很多变通的方法，比如，可以将需要进行事务管理的代码块独立为方法等等。

声明式事务管理也有两种常用的方式：
- 基于\<tx>和\<aop>命名空间的xml配置文件；
- 基于@Transactional注解。

## 声明式事务的初探
spring容器对外提供了了一个接口PlatformTransaction，其中声明了事务操作有关的几个方法，然后又定义了一个抽象类AbstractPlatformTransaction，这个抽象类是实现了接口PlatformTransaction，从而实现接口中的方法，以及具体的有关的事务操作了，不同的技术对应不同的实现，JDBC技术则对应一个DataSourceTransactionManager、Hibernater技术对应HibernateTransactionManager等等。

- 设计接口的原因

在这设计一个接口就可以让我们在客户端编程的时候不用关系采用的是什么技术，例用接口来定义一个参数，来控制事务，避免采和具体的技术相关，看到这也许会有疑问。我们不关心具体的技术，那么Spring容器到底采用哪种事务处理技术呢？JDBC的还是Hibernate的？这其实需要我们在配置文件中引入即可。

如果使用JDBC技术，引入事务管理器的时候就采用了DataSourceTransactionManager，如果我们使用的是Hibernate，则在配置文件中需要配置HibernateTransactionManager。这样就真正做到了面向接口编程。

- 设计抽象类的原因

在不同的技术实现中的commit操作和rollback操作是一样的，开启事务是不一样的。不同的技术有了相同的行为，我们就需要考虑抽象类的作用了，其实在Java面向对象的实现中抽象类的出现就是解决相同行为的问题，因为在抽象类中我们可以实现相同的行为，然后不同的行为继承这个抽象类然后重写不同的行为，这就是这个架构的精髓所在。这我们既做到了面向接口编程，有抽象出了共同的行为。

这个架构处处体现了模板模式，spring已经将模板的骨架设计好了，我们只需要按照这个框架套进去需要的代码即可，当然在事务管理器的引入也存在着很多的技术，spring的强大之处就是把非常复杂的事情封装的非常简单，我们只需要简单的配置一下就可以实现。


## 声明式事务的源码分析
声明式事务结合IoC容器和Spirng已有的FactoryBean来对事务管理进行属性配置，比如传播行为，隔离级别等。其中最简单的方式就是通过配置TransactionProxyFactoryBean来实现声明式事务。

在整个源代码分析中，我们可以大致可以看到Spring实现声明式事物管理有这么几个部分： 

- 对在上下文中配置的属性的处理，这里涉及的类是TransactionAttributeSourceAdvisor，这是一个通知器，用它来对属性值进行处理，属性信息放在TransactionAttribute中来使用,而这些属性的处理往往是和对切入点的处理是结合起来的。对属性的处理放在类TransactionAttributeSource中完成。 
- 创建事物的过程，这个过程是委托给具体的事物管理器来创建的，但Spring通过TransactionStatus来传递相关的信息。 
- 对事物的处理通过对相关信息的判断来委托给具体的事物管理器完成。

我们下面看看具体的实现，在TransactionFactoryBean中： 
```javascript
 public class TransactionProxyFactoryBean extends AbstractSingletonProxyFactoryBean  
        implements FactoryBean, BeanFactoryAware {  
//这里是Spring事务处理而使用的AOP拦截器，中间封装了Spring对事务处理的代码来支持声明式事务处理的实现  
    private final TransactionInterceptor transactionInterceptor = new TransactionInterceptor();  
  
    private Pointcut pointcut;  
  
//这里Spring把TransactionManager注入到TransactionInterceptor中去  
    public void setTransactionManager(PlatformTransactionManager transactionManager) {  
        this.transactionInterceptor.setTransactionManager(transactionManager);  
    }  
  
//这里把在bean配置文件中读到的事务管理的属性信息注入到TransactionInterceptor中去  
    public void setTransactionAttributes(Properties transactionAttributes) {  
        this.transactionInterceptor.setTransactionAttributes(transactionAttributes);  
    }  
  
    .........中间省略了其他一些方法.......  
  
    //这里创建Spring AOP对事务处理的Advisor  
    protected Object createMainInterceptor() {  
        this.transactionInterceptor.afterPropertiesSet();  
        if (this.pointcut != null) {  
            //这里使用默认的通知器  
            return new DefaultPointcutAdvisor(this.pointcut, this.transactionInterceptor);  
        }  
        else {  
            // 使用上面定义好的TransactionInterceptor作为拦截器，同时使用TransactionAttributeSourceAdvisor  
            return new TransactionAttributeSourceAdvisor(this.transactionInterceptor);  
        }  
    }  
}
```
那什么时候Spring的TransactionInterceptor被注入到Spring AOP中成为Advisor中的一部分呢？我们看到在TransactionProxyFactoryBean中，这个方法在IOC初始化bean的时候被执行： 
```javascript

    public void afterPropertiesSet() {  
        .......  
        //TransactionProxyFactoryBean实际上使用ProxyFactory完成AOP的基本功能。  
        ProxyFactory proxyFactory = new ProxyFactory();  
      
        if (this.preInterceptors != null) {  
            for (int i = 0; i < this.preInterceptors.length; i++) {  
                proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(this.preInterceptors[i]));  
            }  
        }  
      
        //这里是Spring加入通知器的地方  
        //有两种通知器可以被加入DefaultPointcutAdvisor或者TransactionAttributeSourceAdvisor  
        //这里把Spring处理声明式事务处理的AOP代码都放到ProxyFactory中去，怎样加入advisor我们可以参考ProxyFactory的父类AdvisedSupport()  
        //由它来维护一个advice的链表，通过这个链表的增删改来抽象我们对整个通知器配置的增删改操作。  
        proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(createMainInterceptor()));  
      
        if (this.postInterceptors != null) {  
            for (int i = 0; i < this.postInterceptors.length; i++) {  
                proxyFactory.addAdvisor(this.advisorAdapterRegistry.wrap(this.postInterceptors[i]));  
            }  
        }  
      
        proxyFactory.copyFrom(this);  
         
        //这里创建AOP的目标源  
        TargetSource targetSource = createTargetSource(this.target);  
        proxyFactory.setTargetSource(targetSource);  
      
        if (this.proxyInterfaces != null) {  
            proxyFactory.setInterfaces(this.proxyInterfaces);  
        }  
        else if (!isProxyTargetClass()) {  
            proxyFactory.setInterfaces(ClassUtils.getAllInterfacesForClass(targetSource.getTargetClass()));  
        }  
      
        this.proxy = getProxy(proxyFactory);  
    }  
```
Spring 已经定义了一个transctionInterceptor作为拦截器或者AOP advice的实现，在IOC容器中定义的其他属性比如transactionManager和事务管理的属性都会传到已经定义好的 TransactionInterceptor那里去进行处理。以上反映了基本的Spring AOP的定义过程，其中pointcut和advice都已经定义好，同时也通过通知器配置到ProxyFactory中去了。 

下面让我们回到TransactionProxyFactoryBean中看看TransactionAttributeSourceAdvisor是怎样定义的，这样我们可以理解具体的属性是怎样起作用，这里我们分析一下类TransactionAttributeSourceAdvisor: 
```javascript
    public class TransactionAttributeSourceAdvisor extends AbstractPointcutAdvisor {  
        //和其他Advisor一样，同样需要定义AOP中的用到的Interceptor和Pointcut  
        //Interceptor使用传进来的TransactionInterceptor  
        //而对于pointcut,这里定义了一个内部类，参见下面的代码    
        private TransactionInterceptor transactionInterceptor;  
      
        private final TransactionAttributeSourcePointcut pointcut = new TransactionAttributeSourcePointcut();  
         
        .........  
        //定义的PointCut内部类  
            private class TransactionAttributeSourcePointcut extends StaticMethodMatcherPointcut implements Serializable {  
           .......  
          //方法匹配的实现，使用了TransactionAttributeSource类  
            public boolean matches(Method method, Class targetClass) {  
                TransactionAttributeSource tas = getTransactionAttributeSource();  
                //这里使用TransactionAttributeSource来对配置属性进行处理  
                return (tas != null && tas.getTransactionAttribute(method, targetClass) != null);  
            }  
        ........省略了equal,hashcode,tostring的代码  
        }  
```
这里我们看看属性值是怎样被读入的：AbstractFallbackTransactionAttributeSource负责具体的属性读入任务，我们可以有两种读入方式，比如annotation和直接配置.我们下面看看直接配置的读入方式，在Spring中同时对读入的属性值进行了缓存处理，这是一个decorator模式：

```javascript
    public final TransactionAttribute getTransactionAttribute(Method method, Class targetClass) {  
        //这里先查一下缓存里有没有事务管理的属性配置，如果有从缓存中取得TransactionAttribute  
        Object cacheKey = getCacheKey(method, targetClass);  
        Object cached = this.cache.get(cacheKey);  
        if (cached != null) {  
            if (cached == NULL_TRANSACTION_ATTRIBUTE) {  
                return null;  
            }  
            else {  
                return (TransactionAttribute) cached;  
            }  
        }  
        else {  
            // 这里通过对方法和目标对象的信息来计算事务缓存属性  
            TransactionAttribute txAtt = computeTransactionAttribute(method, targetClass);  
            //把得到的事务缓存属性存到缓存中，下次可以直接从缓存中取得。  
            if (txAtt == null) {  
                this.cache.put(cacheKey, NULL_TRANSACTION_ATTRIBUTE);  
            }  
            else {  
                ...........  
                this.cache.put(cacheKey, txAtt);  
            }  
            return txAtt;  
        }  
    }  
```
基本的处理在computeTransactionAttribute()中：
```javascript
    private TransactionAttribute computeTransactionAttribute(Method method, Class targetClass) {  
        //这里检测是不是public方法  
        if(allowPublicMethodsOnly() && !Modifier.isPublic(method.getModifiers())) {  
            return null;  
        }  
         
        Method specificMethod = AopUtils.getMostSpecificMethod(method, targetClass);  
         
        // First try is the method in the target class.  
        TransactionAttribute txAtt = findTransactionAttribute(findAllAttributes(specificMethod));  
        if (txAtt != null) {  
            return txAtt;  
        }  
      
        // Second try is the transaction attribute on the target class.  
        txAtt = findTransactionAttribute(findAllAttributes(specificMethod.getDeclaringClass()));  
        if (txAtt != null) {  
            return txAtt;  
        }  
      
        if (specificMethod != method) {  
            // Fallback is to look at the original method.  
            txAtt = findTransactionAttribute(findAllAttributes(method));  
            if (txAtt != null) {  
                return txAtt;  
            }  
            // Last fallback is the class of the original method.  
            return findTransactionAttribute(findAllAttributes(method.getDeclaringClass()));  
        }  
        return null;  
    }  
```
经过一系列的尝试我们可以通过findTransactionAttribute()通过调用findAllAttribute()得到TransactionAttribute的对象，如果返回的是null,这说明该方法不是我们需要事务处理的方法。 
在完成把需要的通知器加到ProxyFactory中去的基础上，我们看看具体的看事务处理代码怎样起作用，在TransactionInterceptor中： 
```javascript
    public Object invoke(final MethodInvocation invocation) throws Throwable {  
        //这里得到目标对象  
        Class targetClass = (invocation.getThis() != null ? invocation.getThis().getClass() : null);  
      
        //这里同样的通过判断是否能够得到TransactionAttribute来决定是否对当前方法进行事务处理，有可能该属性已经被缓存，  
        //具体可以参考上面对getTransactionAttribute的分析，同样是通过TransactionAttributeSource  
        final TransactionAttribute txAttr =  
                getTransactionAttributeSource().getTransactionAttribute(invocation.getMethod(), targetClass);  
        final String joinpointIdentification = methodIdentification(invocation.getMethod());  
      
        //这里判断我们使用了什么TransactionManager  
        if (txAttr == null || !(getTransactionManager() instanceof CallbackPreferringPlatformTransactionManager)) {  
            // 这里创建事务，同时把创建事务过程中得到的信息放到TransactionInfo中去  
            TransactionInfo txInfo = createTransactionIfNecessary(txAttr, joinpointIdentification);  
            Object retVal = null;  
            try {  
                  retVal = invocation.proceed();  
            }  
            catch (Throwable ex) {  
                // target invocation exception  
                completeTransactionAfterThrowing(txInfo, ex);  
                throw ex;  
            }  
            finally {  
                cleanupTransactionInfo(txInfo);  
            }  
            commitTransactionAfterReturning(txInfo);  
            return retVal;  
        }  
      
        else {  
            // 使用的是Spring定义的PlatformTransactionManager同时实现了回调接口,我们通过其回调函数完成事务处理，就像我们使用编程式事务处理一样。  
            try {  
                Object result = ((CallbackPreferringPlatformTransactionManager) getTransactionManager()).execute(txAttr,  
                        new TransactionCallback() {  
                            public Object doInTransaction(TransactionStatus status) {  
                                //同样的需要一个TransactonInfo  
                                TransactionInfo txInfo = prepareTransactionInfo(txAttr, joinpointIdentification, status);  
                                try {  
                                    return invocation.proceed();  
                                }  
                             .....这里省去了异常处理和事务信息的清理代码  
                        });  
             ...........  
        }  
    }  
```
这里面涉及到事务的创建，我们可以在TransactionAspectSupport实现的事务管理代码： 
```javascript
    protected TransactionInfo createTransactionIfNecessary(  
            TransactionAttribute txAttr, final String joinpointIdentification) {  
      
        // If no name specified, apply method identification as transaction name.  
        if (txAttr != null && txAttr.getName() == null) {  
            txAttr = new DelegatingTransactionAttribute(txAttr) {  
                public String getName() {  
                    return joinpointIdentification;  
                }  
            };  
        }  
      
        TransactionStatus status = null;  
        if (txAttr != null) {  
        //这里使用了我们定义好的事务配置信息,有事务管理器来创建事务，同时返回TransactionInfo  
            status = getTransactionManager().getTransaction(txAttr);  
        }  
        return prepareTransactionInfo(txAttr, joinpointIdentification, status);  
    }  
```

首先通过TransactionManager得到需要的事务，事务的创建根据我们定义的事务配置决定，在 AbstractTransactionManager中给出一个标准的创建过程，当然创建什么样的事务还是需要具体的 PlatformTransactionManager来决定，但这里给出了创建事务的模板： 
```javascript
    public final TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {  
        Object transaction = doGetTransaction();  
        ......  
      
        if (definition == null) {  
            //如果事务信息没有被配置，我们使用Spring默认的配置方式  
            definition = new DefaultTransactionDefinition();  
        }  
      
        if (isExistingTransaction(transaction)) {  
            // Existing transaction found -> check propagation behavior to find out how to behave.  
            return handleExistingTransaction(definition, transaction, debugEnabled);  
        }  
      
        // Check definition settings for new transaction.  
        //下面就是使用配置信息来创建我们需要的事务;比如传播属性和同步属性等  
        //最后把创建过程中的信息收集起来放到TransactionStatus中返回；    
        if (definition.getTimeout() < TransactionDefinition.TIMEOUT_DEFAULT) {  
            throw new InvalidTimeoutException("Invalid transaction timeout", definition.getTimeout());  
        }  
      
        // No existing transaction found -> check propagation behavior to find out how to behave.  
        if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {  
            throw new IllegalTransactionStateException(  
                    "Transaction propagation 'mandatory' but no existing transaction found");  
        }  
        else if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||  
                definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||  
            definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {  
            //这里是事务管理器创建事务的地方，并将创建过程中得到的信息放到TransactionStatus中去，包括创建出来的事务  
            doBegin(transaction, definition);  
            boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);  
            return newTransactionStatus(definition, transaction, true, newSynchronization, debugEnabled, null);  
        }  
        else {  
            boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);  
            return newTransactionStatus(definition, null, false, newSynchronization, debugEnabled, null);  
        }  
    }  
```
完成事务创建的准备，创建过程中得到的信息存储在TransactionInfo对象中进行传递同时把信息和当前线程绑定； 
```javascript
    protected TransactionInfo prepareTransactionInfo(  
            TransactionAttribute txAttr, String joinpointIdentification, TransactionStatus status) {  
      
        TransactionInfo txInfo = new TransactionInfo(txAttr, joinpointIdentification);  
        if (txAttr != null) {  
        .....  
            // 同样的需要把在getTransaction中得到的TransactionStatus放到TransactionInfo中来。  
            txInfo.newTransactionStatus(status);  
        }  
        else {  
        .......  
       }  
      
        // 绑定事务创建信息到当前线程  
        txInfo.bindToThread();  
        return txInfo;  
    }  
```
将创建事务的信息返回，然后看到其他的事务管理代码： 
```javascript
    protected void commitTransactionAfterReturning(TransactionInfo txInfo) {  
        if (txInfo != null && txInfo.hasTransaction()) {  
            if (logger.isDebugEnabled()) {  
                logger.debug("Invoking commit for transaction on " + txInfo.getJoinpointIdentification());  
            }  
            this.transactionManager.commit(txInfo.getTransactionStatus());  
        }  
    }  
```
通过transactionManager对事务进行处理，包括异常抛出和正常的提交事务，具体的事务管理器由用户程序设定。 
```javascript
    protected void completeTransactionAfterThrowing(TransactionInfo txInfo, Throwable ex) {  
        if (txInfo != null && txInfo.hasTransaction()) {  
            if (txInfo.transactionAttribute.rollbackOn(ex)) {  
                ......  
                try {  
                    this.transactionManager.rollback(txInfo.getTransactionStatus());  
                }  
                ..........  
      }  
            else {  
                .........  
                try {  
                    this.transactionManager.commit(txInfo.getTransactionStatus());  
                }  
       ...........  
    }  
      
    protected void commitTransactionAfterReturning(TransactionInfo txInfo) {  
        if (txInfo != null && txInfo.hasTransaction()) {  
            ......  
            this.transactionManager.commit(txInfo.getTransactionStatus());  
        }  
    }  
```
Spring通过以上代码对transactionManager进行事务处理的过程进行了AOP包装，到这里我们看到为了方便客户实现声明式的事务处理，Spring还是做了许多工作的。如果说使用编程式事务处理，过程其实比较清楚，我们可以参考书中的例子： 
```javascript
    TransactionDefinition td = new DefaultTransactionDefinition();  
    TransactionStatus status = transactionManager.getTransaction(td);  
    try{  
          ......//这里是我们的业务方法  
    }catch (ApplicationException e) {  
       transactionManager.rollback(status);  
       throw e  
    }  
    transactionManager.commit(status);  
     ........  
```
我们看到这里选取了默认的事务配置DefaultTransactionDefinition，同时在创建事物的过程中得到TransactionStatus,然后通过直接调用事务管理器的相关方法就能完成事务处理。 
声明式事务处理也同样实现了类似的过程，只是因为采用了声明的方法，需要增加对属性的读取处理，并且需要把整个过程整合到Spring AOP框架中和IoC容器中去的过程。 
下面我们选取一个具体的transactionManager - DataSourceTransactionManager来看看其中事务处理的实现。 
同样的通过使用AbstractPlatformTransactionManager使用模板方法，这些都体现了对具体平台相关的事务管理器操作的封装，比如commit： 
```javascript
    public final void commit(TransactionStatus status) throws TransactionException {  
        ......  
        DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;  
        if (defStatus.isLocalRollbackOnly()) {  
            ......  
            processRollback(defStatus);  
            return;  
        }  
             .......  
            processRollback(defStatus);  
        ......  
        }  
      
        processCommit(defStatus);  
    }  
```
通过对TransactionStatus的具体状态的判断，来决定具体的事务处理： 
```javascript
    private void processCommit(DefaultTransactionStatus status) throws TransactionException {  
        try {  
            boolean beforeCompletionInvoked = false;  
            try {  
                triggerBeforeCommit(status);  
                triggerBeforeCompletion(status);  
                beforeCompletionInvoked = true;  
                boolean globalRollbackOnly = false;  
                if (status.isNewTransaction() || isFailEarlyOnGlobalRollbackOnly()) {  
                    globalRollbackOnly = status.isGlobalRollbackOnly();  
                }  
                if (status.hasSavepoint()) {  
                ........  
                   status.releaseHeldSavepoint();  
                }  
                else if (status.isNewTransaction()) {  
                ......  
                    doCommit(status);  
                }  
            .........  
    }  
```
这些模板方法的实现由具体的transactionManager来实现，比如在DataSourceTransactionManager: 
```javascript
    protected void doCommit(DefaultTransactionStatus status) {  
        //这里得到存在TransactionInfo中已经创建好的事务  
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();  
      
        //这里得到和事务绑定的数据库连接  
        Connection con = txObject.getConnectionHolder().getConnection();  
        ........  
        try {  
        //这里通过数据库连接来提交事务  
            con.commit();  
        }  
       .......  
    }  
      
    protected void doRollback(DefaultTransactionStatus status) {  
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();  
        Connection con = txObject.getConnectionHolder().getConnection();  
        if (status.isDebug()) {  
            logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");  
        }  
        try {  
        //这里通过数据库连接来回滚事务  
            con.rollback();  
        }  
        catch (SQLException ex) {  
            throw new TransactionSystemException("Could not roll back JDBC transaction", ex);  
        }  
    }  
```

我们看到在DataSourceTransactionManager中最后还是交给connection来实现事务的提交和rollback。整个声明式事务处理是事务处理在Spring AOP中的应用，我们看到了一个很好的使用Spring AOP的例子。

在Spring声明式事务处理的源代码中我们可以看到： 
1. 怎样封装各种不同平台下的事务处理代码 
2. 怎样读取属性值和结合事务处理代码来完成既定的事务处理策略 
3. 怎样灵活的使用SpringAOP框架。 

