# Netty实战

## **一、学习大纲**

- Netty介绍
- Netty架构实现
- Netty模块分析
- Netty HTTP Tunnel
- Netty对Socket的实现
- Netty压缩与解压缩
- Netty对于RPC的支援
- WebSocket实现与原理分析
- Websocket连接建立方式与生命周期分解
- Websocket服务端与客户端开发
- RPC框架分析
- Google Protobuf使用方式分析
- Apache Thrift使用方式与文件编写方式分析
- Netty大文件传送支持
- 可扩展的时间模型
- Netty统一通信API
- 零拷贝在Netty中的实现与支持
- TCP粘包与拆包分析
- NIO模型在Netty中的实现
- Netty编解码开发技术
- Netty重要类与接口源代码剖析
- Channel分析
- 序列化详解



## **二、Netty介绍** 

[Netty官网]: https://netty.io/	"Netty官网"
[Netty的Github官网]: https://github.com/netty/netty	"Netty的Github官网"



> Netty is a NIO client server framework which enables quick and easy development of network applications such as protocol servers and clients. It greatly simplifies and streamlines network programming such as TCP and UDP socket server.
>
> 'Quick and easy' doesn't mean that a resulting application will suffer from a maintainability or a performance issue. Netty has been designed carefully with the experiences earned from the implementation of a lot of protocols such as FTP, SMTP, HTTP, and various binary and text-based legacy protocols. As a result, Netty has succeeded to find a way to achieve ease of development, performance, stability, and flexibility without a compromise.
>
> ## Features
>
> ### Design
>
> - Unified API for various transport types - blocking and non-blocking socket
> - Based on a flexible and extensible event model which allows clear separation of concerns
> - Highly customizable thread model - single thread, one or more thread pools such as SEDA
> - True connectionless datagram socket support (since 3.1)
>
> ### Ease of use
>
> - Well-documented Javadoc, user guide and examples
> - No additional dependencies, JDK 5 (Netty 3.x) or 6 (Netty 4.x) is enough
>   - Note: Some components such as HTTP/2 might have more requirements. Please refer to [the Requirements page](https://netty.io/wiki/requirements.html) for more information.
>
> ### Performance
>
> - Better throughput, lower latency
> - Less resource consumption
> - Minimized unnecessary memory copy
>
> ### Security
>
> - Complete SSL/TLS and StartTLS support
>
> ### Community
>
> - Release early, release often
> - The author has been writing similar frameworks since 2003 and he still finds your feed back precious!

![img](https://netty.io/images/components.png)

注: 本次分享是基于Netty的4.1版本分享.

1. 市面上很多书都是基于Netty5.0来写的, 而这里为什么会采用Netty4.1?

   原因: 

   [Netty5.0的master分支被删除的原因]: https://github.com/netty/netty/issues/4466

2. 学习Netty推荐的纸质版书籍?

   1)  Netty in Action

   2)  Netty权威指南(可以看理论, 这本书不建议看代码, 可以研究理论即可)

## 二、项目环境搭建

1. 如何搭建Maven项目, 网上很多教程, 这里就不这里讲解;

2. 如何搜索maven依赖?

   - 进到https://search.maven.org/

   - 在搜索框里面输入 

     ```
     g:"io.netty" AND a:"netty-all"
     ```

     ![image-20210623173145345](img\2-1.png)

   - 接着左上角的下拉框可以看到相应的版本, 一般来说最新版本号是在最上面, 如果你的项目是用gradle构建的, Maven依赖换成下图的gradle依赖

     ![image-20210623173319157](img\2-2.png)

   - 找到之后, 在项目增加依赖

     ![image-20210623173659029](img\2-3.png)

3. 找到之后, 就在我们的项目依赖进来

