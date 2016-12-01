响应式编程实践
============

## 1 响应式介绍

### 1.1 什么是响应式编程(Reactive Programming)

- 响应式编程 是一种 基于对变化的响应的一种编程范式;
- RxJava响应式编程 = 观察者模式(Observer Pattern) + 迭代器模式(Iterator Pattern) + 函数式编程


### 1.2 响应式宣言（[Reactive Manifesto](https://github.com/reactivemanifesto/reactivemanifesto)）：

来自不同领域的组织正在不约而同地发现一些看起来如出一辙的软件构建模式。它们的系统更加稳健，更加有可回复性，更加灵活，并且以更好的定位来满足现代的需求。

响应式宣言针对一个系统而言，并不等同于 响应式编程规范，响应式系统应该满足如下特点：

- 反应灵敏的[Responsive]：只要有可能，系统就会及时响应。 
- 有回复性的[Resilient]：系统在面临故障时也能保持及时响应。
- 可伸缩的[Elastic]：系统在变化的工作负载下保持及时响应。
- 消息驱动的[Message Driven]：响应式系统依赖异步消息传递来建立组件之间的界限，这一界限确保了松耦合，隔离，位置透明性等特性的实现，还提供了以消息的形式把故障委派出去的手段。

![reactive-feature.png](reactive-traits.svg)

## 1.3 响应式编程规范

### 1.3.1 响应式流规范（[Reactive Streams](http://www.reactive-streams.org/)）

- Reactive Streams 规范提供一个非堵塞的异步流处理的抗压(breakpressure)标准；Reactive Streams的目标是增加抽象层，而不是进行底层的流处理，规范将这些问题留给了库实现来解决。

- 对于JVM，目前已经有多个库实现该标准，RxJava2, akka-streams,Reactor[[5]](https://github.com/reactor/reactor) 等；
- 统一标准的好处就是 各个实现产生的数据可以方便的转换和消费；
> 示例
```java 
   @Test
    public void rxjavaAndReactor()
    {
        Path filePath = Paths.get("build.gradle");
        // RxJava2 to Reactor
        Flowable<String> flowable = Flowable
                .fromCallable(() -> Files.readAllLines(filePath))
                .flatMap(x -> Flowable.fromIterable(x));
        Flux.from(flowable).count().subscribe(System.out::println);

        // Reactor to RxJava2
        try
        {
            Flux<String> flux = Flux.fromIterable(Files.readAllLines(filePath));
            Flowable.fromPublisher(flux).count()
                    .subscribe(System.out::println);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
```
- Reactive Streams JVM接口由以下四个interface 组成：
    - Publisher ： 

    - Subscriber ：

    - Subscription ：

    - Processor ：

- Reactive Streams 规范主要目标[[2]](https://www.infoq.com/news/2015/09/reactive-streams-introduction)：
    
    - 通过异步边界(Asynchronous Boundary)[[3]](https://github.com/reactive-streams/reactive-streams-jvm/issues/46)来解耦系统组件。 解偶的先决条件，分离事件/数据流的发送方和接收方的资源使用;
    - 为背压（ back pressure ) 处理定义一种模型。流处理的理想范式是将数据从发布者推送到订阅者，这样发布者就可以快速发布数据，同时通过压力处理来确保速度更快的发布者不会对速度较慢的订阅者造成过载。背压处理通过使用流控制来确保操作的稳定性并能实现优雅降级，从而提供弹性能力。

- 适用范围： 适合于流处理的系统有ETL（Extract、Transform、Load）与复杂事件处理（CEP）系统，此外还有报表与分析系统[[4]](https://medium.com/@kvnwbbr/a-journey-into-reactive-streams-5ee2a9cd7e29)。

### 1.3.2 响应式扩展（Reactive Extensions, ReactiveX,Rx）
- ReactiveX是Reactive Extensions的缩写，一般简写为Rx，最初是LINQ的一个扩展，由微软的架构师Erik Meijer领导的团队开发，在2012年11月开源[[1]](http://download.microsoft.com/download/4/E/4/4E4999BA-BC07-4D85-8BB1-4516EC083A42/Rx%20Design%20Guidelines.pdf)；

- Rx是一个编程模型，目标是提供一致的编程接口，帮助开发者更方便的处理异步数据流，Rx库支持.NET、JavaScript和C++，Rx近几年越来越流行了，现在已经支持几乎全部的流行编程语言了，Rx的大部分语言库由ReactiveX这个组织负责维护，比较流行的有RxJava/RxJS/Rx.NET，社区网站是 reactivex.io。

- Rx = Observables + LINQ + Schedulers。Rx 让开发者可以利用可观察序列和LINQ风格查询操作符来编写异步和基于事件的程序;

- RxJava 是 在Java虚拟机上实现的Reactive Extensions（响应式扩展)库;  



### 1.4 为什么需要响应式编程


|| 单个数据 	|  多个数据 | 
|:--|:--|:--|
| 同步	|T getData()        |	Iterable<T> getData()   |
| 异步	|Future<T> getData()|	Observable<T> getData() |

https://www.lightbend.com/blog/7-ways-washing-dishes-and-message-driven-reactive-systems


## 2 RxJava 基础

### 2.1 RxJava 1 vs RxJava 2
- RxJava 先于 Reactive Streams 出现；
- RxJava 2.0 已经按照Reactive-Streams specification规范完全的重写, 基于Java8+;
- 2.0 已经独立于RxJava 1.x而存在。
- RxJava 项目地址 <https://github.com/ReactiveX/RxJava>

### 2.2 RxJava2中的函子

#### 2.3.1 Flowable & Observable

Observable : 不支持背压（在RxJava1.x中的Observable 部分支持背压）；

Flowable : Observable新的实现，支持背压，同时实现Reactive Streams 的 Publisher 接口。



#### 2.3.2  Single & Completable & Maybe



## 3 RxJava 编程实践

这这一节，结合函子的一些通用操作来实践RxJava2的主要功能 

### 3.1 函数式编程

#### 3.1.1 与Java 8 Streams 类似操作及对比

#### 3.1.2 其它实用操作

### 3.2 数据的产生与消费(Publisher & Subscriber)

### 3.3 异步与并发（Asynchronized & Concurrency）

### 3.4 异常处理 (Error Handleing)

### 3.5 背压(back pressure)

### 



## 再谈函数式编程

### Functor 和 Monads

http://ifeve.com/java%E4%B8%AD%E7%9A%84functor%E4%B8%8Emonad/



## Reference

1. 响应式宣言.https://github.com/reactivemanifesto/reactivemanifesto/blob/master/README.zh-cn.md









