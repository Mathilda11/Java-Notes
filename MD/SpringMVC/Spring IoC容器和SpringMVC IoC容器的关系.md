要想很好理解这两个容器的关系，需要先熟悉Spring是怎样在web容器中启动起来的。Spring的启动过程其实就是其IoC容器的启动过程。

### Spring的启动过程：
1. 首先，对于一个web应用，其部署在web容器中，web容器提供其一个全局的上下文环境，这个上下文就是ServletContext，其为后面的Spring IoC容器提供宿主环境；

2. 其次，在web.xml中会提供有ContextLoaderListener。在web容器启动时，会触发容器初始化事件，此时ContextLoaderListener会监听到这个事件，其contextInitialized方法会被调用，在这个方法中，Spring会初始化一个启动上下文，这个上下文被称为根上下文，即WebApplicationContext，这是一个接口类，确切的说，其实际的实现类是XmlWebApplicationContext。这个就是Spring的IoC容器，其对应的Bean定义的配置由web.xml中的context-param标签指定。在这个IoC容器初始化完毕后，Spring以WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE为属性Key，将其存储到ServletContext中，便于获取；

3. 再次，ContextLoaderListener监听器初始化完毕后，开始初始化web.xml中配置的Servlet，这个servlet可以配置多个，以最常见的DispatcherServlet为例，这个servlet实际上是一个标准的前端控制器，用以转发、匹配、处理每个servlet请求。DispatcherServlet上下文在初始化的时候会建立自己的IoC上下文，用以持有Spring mvc相关的bean。在建立DispatcherServlet自己的IoC上下文时，会利用WebApplicationContext.ROOTWEBAPPLICATIONCONTEXTATTRIBUTE先从ServletContext中获取之前的根上下文(即WebApplicationContext)作为自己上下文的parent上下文。有了这个parent上下文之后，再初始化自己持有的上下文。这个DispatcherServlet初始化自己上下文的工作在其initStrategies方法中可以看到，大概的工作就是初始化处理器映射、视图解析等。这个servlet自己持有的上下文默认实现类也是mlWebApplicationContext。初始化完毕后，Spring以与servlet的名字相关(此处不是简单的以servlet名为Key，而是通过一些转换，具体可自行查看源码)的属性为属性Key，也将其存到ServletContext中，以便后续使用。这样每个servlet就持有自己的上下文，即拥有自己独立的bean空间，同时各个servlet共享相同的bean，即根上下文(第2步中初始化的上下文)定义的那些bean。


### 父容器
使用listener监听器来加载配置文件，如下：
```javascript
 　　<context-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>/WEB-INF/court-service.xml</param-value>
    </context-param>

　　　<listener>
       　<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
   　</listener>
```
 Spring 会创建一个WebApplicationContext上下文，称为父上下文（父容器），保存在 ServletContext中，key是WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE的值。

可以使用Spring提供的工具类取出上下文对象：WebApplicationContextUtils.getWebApplicationContext(ServletContext);


### 子容器
使用Spring MVC 来处理拦截相关的请求时，会配置DispatchServlet：
```javascript
   <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
      <init-param>
          <param-name>contextConfigLocation</param-name>
          <param-value>classpath:spring.xml</param-value>
      </init-param>
      <load-on-startup>1</load-on-startup>
   </servlet>
　　<servlet-mapping> 
　　　　<servlet-name>xxx</servlet-name> 
　　　　<url-pattern>/</url-pattern> 
　　</servlet-mapping>
```
每个DispatchServlet会有一个自己的上下文，称为子上下文，它也保存在 ServletContext中，key 是"org.springframework.web.servlet.FrameworkServlet.CONTEXT"+Servlet名称。当一 个Request对象产生时，会把这个子上下文对象（WebApplicationContext）保存在Request对象中，key是 DispatcherServlet.class.getName() + ".CONTEXT"。

可以使用工具类取出上下文对象：RequestContextUtils.getWebApplicationContext(request);


### 父容器和子容器的访问权限

SpringMVC 的 IOC 容器中的 bean 可以引用Spring IOC 容器中的 bean，反之不行，原因是：
1. Spring MVC是Spring的子类，子类可以引用父类，父类不能引用子类。

2. 从软件层面上来说，Spring MVC是展示层可以调用业务层，业务层不能调用展示层。
