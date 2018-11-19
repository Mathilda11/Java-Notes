## SpringMVC概述
SpringMVC是一个基于MVC的Web框架。
Spring Web MVC和Struts2都属于表现层的框架，它是Spring框架的一部分，我们可以从Spring的整体结构中看得出来：
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181119104410701.png)框架性质的C层要完成的主要工作：封装Web请求为一个数据对象、调用业务层来处理数据对象、 返回处理数据结果及相应的视图给用户。

Spring C层框架的核心是DispatcherServlet，它的作用是将请求分发给不同的后端处理器，也即使用 了一种被称为前端控制器的模式。 Spring 的C层框架使用了后端控制器、映射处理器和视图解析器来共同完成C层框架的主要工作。并且spring 的C层框架还真正地把业务层处理的数据结果和相应的视图拼成一个对象，即我们后面会经常用到的ModelAndView对象。


## SpringMVC框架结构
![在这里插入图片描述](https://img-blog.csdnimg.cn/20181119101140257.png)
## 架构流程
1. 用户发送请求至前端控制器DispatcherServlet
2. DispatcherServlet收到请求调用HandlerMapping处理器映射器。
3. 处理器映射器根据请求url找到具体的处理器，生成处理器对象及处理器拦截器(如果有则生成)一并返回给DispatcherServlet。
4. DispatcherServlet通过HandlerAdapter处理器适配器调用处理器。
5. HandlerAdapter会根据Handler来调用真正的处理器来处理请求，并处理相应的业务逻辑。
6. 处理器处理完业务后，会返回一个ModelAndView对象给处理器适配器，Model是返回的数据对象，View是个逻辑上的View。
7. 处理器适配器返回ModelAndView对象给DispatcherServlet。
8. DispatcherServlet将ModelAndView传给ViewReslover视图解析器。
9. ViewResolver会根据逻辑View解析后返回实际的View。

10. DispaterServlet对View进行渲染视图（即将模型数据Mode填充至视图View中）。
11. DispatcherServlet响应用户。

## 组件说明
DispatcherServlet：作为前端控制器，整个流程控制的中心，控制其它组件执行，统一调度，降低组件之间的耦合性，提高每个组件的扩展性。

HandlerMapping：通过扩展处理器映射器实现不同的映射方式，例如：配置文件方式，实现接口方式，注解方式等。 

HandlerAdapter：通过扩展处理器适配器，支持更多类型的处理器。

Handler：继DispatcherServlet前端控制器的后端控制器，在DispatcherServlet的控制下Handler对具体的用户请求进行处理。

ViewResolver：通过扩展视图解析器，支持更多类型的视图解析，例如：jsp、freemarker、pdf、excel等。

View：是展示给用户的界面，通常需要标签语言展示模型数据。SpringMVC框架提供了很多的View视图类型的支持，包括：jstlView、freemarkerView、pdfView等。我们最常用的视图就是jsp。

在SpringMVC的各个组件中，处理器映射器HandlerMapping、处理器适配器HandlerAdapter、视图解析器ViewResolver称为SpringMVC的三大组件，由框架提供。

下边两个组件通常情况下需要开发：
Handler：处理器。
View：视图。


