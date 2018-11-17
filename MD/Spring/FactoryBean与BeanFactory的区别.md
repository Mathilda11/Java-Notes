## 简介
- BeanFactory
BeanFactory是接口，提供了IoC容器最基本的形式，给具体的IoC容器的实现提供了规范。

- FactoryBean
FactoryBean也是接口，为IoC容器中Bean的实现提供了更加灵活的方式，FactoryBean在IoC容器的基础上给Bean的实现加上了一个简单工厂模式和装饰模式。

## 区别
BeanFactory与FactoryBean的作用是不同的。
- BeanFactory
BeanFactory是负责生产和管理Bean的一个工厂。在Spring中，BeanFactory是IoC容器的核心接口，它的职责包括：实例化、定位、配置应用程序中的对象及建立这些对象间的依赖。

- FactoryBean
一般情况下，Spring通过反射机制利用\<bean>的class属性指定实现类实例化Bean，在某些情况下，实例化Bean过程比较复杂，如果按照传统的方式，则需要在\<bean>中提供大量的配置信息。配置方式的灵活性是受限的，这时采用编码的方式可能会得到一个简单的方案。Spring为此提供了FactoryBean的工厂类接口，可以通过实现该接口定制实例化Bean的逻辑，它们隐藏了实例化一些复杂Bean的细节，给上层应用带来了便利。


## 联系

`BeanFactory`的实现类通过getBean(String BeanName)方法获取Bean的实例。当在IoC容器中的Bean实现了`FactoryBean`后，通过getBean(String BeanName)获取到的Bean对象是这个`FactoryBean`实现类中的getObject()方法返回的对象。

注：要想获取FactoryBean的实现类，就要getBean(&BeanName)，在BeanName之前加上&。
