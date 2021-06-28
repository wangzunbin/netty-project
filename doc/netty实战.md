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

3. 开发一个hello的Netty项目

   ```java
   public class MyNettyServer {
   
       /**
        * 1. 为什么要创建两个NioEventLoopGroup的呢?
        * 答: 一个也可以, 即处理与客户端的连接, 又要处理业务逻辑, 但是这样子很不友好, Netty比较推荐两个线程
        */
       public static void main(String[] args) throws Exception {
           // Nio死循环, 这个是处理与客户端的请求, 主要是获取连接
           EventLoopGroup bossGroup = new NioEventLoopGroup();
           // NIO死循环  这个接受到客户端数据, 进行业务代码处理, 并返回给客户端
           EventLoopGroup workerGroup = new NioEventLoopGroup();
           try {
               // ServerBootstrap可以理解为服务器启动的工厂类，我们可以通过它来完成服务器端的 Netty 参数初始化。
               // 作用职责:EventLoop初始化,channel的注册过程 ,关于pipeline的初始化,handler的添加过程,服务端连接分析。
               ServerBootstrap serverBootstrap = new ServerBootstrap();
               serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                       // 添加初始化器
                       .childHandler(new MyServerInitializer());
               ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
               channelFuture.channel().closeFuture().sync();
           } finally {
               // 优雅关闭
               bossGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
           }
       }
   }
   
   ```

   ```Java
   //  连接一旦创建的时候, 就会调用这个初始化器
   public class MyServerInitializer extends ChannelInitializer<SocketChannel> {
   
       @Override
       protected void initChannel(SocketChannel ch) throws Exception {
           ChannelPipeline pipeline = ch.pipeline();
           // 参数1, 如果为null, netty自动给这个类加上名称, 如果有的话, 就不会自动加上名称
           // 参数2: 不能搞成单例的, 要new出来
           // HttpServerCodec是关键类, 这是一个http解码器, HttpServerCodec分为HttpRequestDecoder和HttpResponseEncoder, Netty一般来说分为解码和编码, 这个类已经合二为一了
           pipeline.addLast("httpServerCodec", new HttpServerCodec());
           pipeline.addLast("httpServerHandler", new MyHttpServerHandler());
       }
   
   }
   
   ```

   ```Java
   // 获取客户端的数据
   public class MyHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
           // 解决异常 An exceptionCaught() event was fired, and it reached at the tail of the pipeline. It usually means the last handler in the pipeline did not handle the exception.
           if (msg instanceof HttpRequest) {
               ByteBuf content = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
               FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                       HttpResponseStatus.OK, content);
               response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
               response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
               // 刷新到客户端
               ctx.writeAndFlush(response);
           }
       }
   
   }
   
   ```

   运行结果如下:

   ```shell
   90519@DESKTOP-1JGUKEL MINGW64 ~/Desktop
   $ curl 'http://localhost:8899'
     % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                    Dload  Upload   Total   Spent    Left  Speed
   100    11  100    11    0     0    154      0 --:--:-- --:--:-- --:--:--   157Hello World
   
   90519@DESKTOP-1JGUKEL MINGW64 ~/Desktop
   $
   
   ```

4. TestHttpServerHandler自定义类的详解

   ```Java
   public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {
   
       // 这个在数据读取使用到
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
           if (msg instanceof HttpRequest) {
               HttpRequest request = (HttpRequest) msg;
   
               System.out.println("请求方法名：" + request.method().name());
   
               URI uri = new URI(request.uri());
               if("/favicon.ico".equals(uri.getPath())) {
                   System.out.println("请求favicon.ico");
                   return;
               }
   
               ByteBuf content = Unpooled.copiedBuffer("Hello World", CharsetUtil.UTF_8);
               FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                       HttpResponseStatus.OK, content);
               response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
               response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
   
               ctx.writeAndFlush(response);
               // 这个类是可以手动关闭
   //            ctx.channel().close();
           }
       }
   
       // 这类是连接的时候, 比如说我们可以在这个回调方法, 进行如下的逻辑: 客户端上线, 或者是初始化的操作
       @Override
       public void channelActive(ChannelHandlerContext ctx) throws Exception {
           System.out.println("channel active");
           super.channelActive(ctx);
       }
      
   	// 这个方法是通道注册
       @Override
       public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
           System.out.println("channel registered");
           super.channelRegistered(ctx);
       }
       
   	// 这个是处理方法的添加
       @Override
       public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
           System.out.println("handler added");
           super.handlerAdded(ctx);
       }
   
       // 这个回调是失去回调, 比如我们在这个方法做一些业务: 客户端离线, 或者是失去连接客户端的一些操作
       @Override
       public void channelInactive(ChannelHandlerContext ctx) throws Exception {
           System.out.println("channel inactive");
           super.channelInactive(ctx);
       }
   
       // 这个方法是通道失去注册
       @Override
       public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
           System.out.println("channel unregistered");
           super.channelUnregistered(ctx);
       }
   }
   
   ```

   上面的运行结果如下:

   ```Java
   handlerAdded
   channelRead0
   channelActive
   class io.netty.handler.codec.http.DefaultHttpRequest
   /0:0:0:0:0:0:0:1:64581
   channelRead0
   class io.netty.handler.codec.http.LastHttpContent$1
   /0:0:0:0:0:0:0:1:64581
   channelInactive
   channelUnregistered
   ```

## **三、Socket编程**

1. 简单的Socket编程:

     1) 服务器代码:

```Java
public class MyServer {

    //EventLoopGroup 首先是一个接口，继承了EventExecutorGroup ，主要的功能是在时间循环对Channel的注册，

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();//第一步建立bossGroup 接受数据然后转发给workerGroup ，是一个死循环
        EventLoopGroup workGroup = new NioEventLoopGroup();//第二步 workerGroup 完成实际数据的处理，也是一个死循环

        try {

            ServerBootstrap serverBootstrap = new ServerBootstrap();//第三步。启动bossGroup和workerGroup
            serverBootstrap.group(bossGroup, workGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.WARN))
                    .childHandler(new MyServerInitializer());

            ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();//第四部，指定服务端的端口。
            channelFuture.channel().closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully().sync();
            workGroup.shutdownGracefully().sync();
        }

    }
}
```

```Java
public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {

        ChannelPipeline channelPipeline = ch.pipeline();
        
       /**
         *
         * @param maxFrameLength  帧的最大长度
         * @param lengthFieldOffset length字段偏移的地址
         * @param lengthFieldLength length字段所占的字节长
         * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
         * @param initialBytesToStrip 解析时候跳过多少个长度
         * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
         */
        channelPipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        channelPipeline.addLast(new LengthFieldPrepender(4));
        channelPipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        channelPipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        channelPipeline.addLast(new MyServerHandler());

    }
}
```

```Java
public class MyServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {

        System.out.println(ctx.channel().remoteAddress() + " , " + msg);
        ctx.channel().writeAndFlush("from server: " + UUID.randomUUID());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
```

2) 客户端代码:

```Java
public class MyClient {

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(bossGroup).channel(NioSocketChannel.class).handler(new MyClientInitializer());
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8899).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
        }

    }
}

```

```java
public class MyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        /**
         *
         * @param maxFrameLength  帧的最大长度
         * @param lengthFieldOffset length字段偏移的地址
         * @param lengthFieldLength length字段所占的字节长
         * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
         * @param initialBytesToStrip 解析时候跳过多少个长度
         * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
         */
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        // 编码器
        pipeline.addLast(new LengthFieldPrepender(4));
        pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast(new MyClientHandler());
    }
}

```

```Java
public class MyClientHandler extends SimpleChannelInboundHandler<String> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress());
        System.out.println("client output: " + msg);
        ctx.writeAndFlush("from client: " + LocalDateTime.now());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush("来自于客户端的问候！");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}

```

2. 以聊天为例的Socket编程:

   1) 服务器代码:

   ```java
   public class MyChatServer {
   
       public static void main(String[] args) throws Exception {
   
           EventLoopGroup bossGroup = new NioEventLoopGroup();
           EventLoopGroup workGroup = new NioEventLoopGroup();
   
           try {
   
               ServerBootstrap bootstrap = new ServerBootstrap();
               bootstrap.group(bossGroup,workGroup).channel(NioServerSocketChannel.class).childHandler(new MyChatServerInitializer());
   
               ChannelFuture channelFuture = bootstrap.bind(8899).sync();
               channelFuture.channel().closeFuture().sync();
   
           }finally {
               bossGroup.shutdownGracefully().sync();
               workGroup.shutdownGracefully().sync();
   
           }
   
       }
   }
   
   ```

   ```Java
   public class MyChatServerInitializer extends ChannelInitializer<SocketChannel> {
   
       @Override
       protected void initChannel(SocketChannel ch) throws Exception {
           ChannelPipeline pipeline = ch.pipeline();
           // 参数1: 最大数据长度, 参数2: 使用系统的默认分隔符数组, 在这里分隔符有两种:  \r, \n或者\n
           pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
           pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
           pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
           pipeline.addLast(new MyChatServerHandler());
   
       }
   }
   
   ```

   ```Java
   public class MyChatServerHandler extends SimpleChannelInboundHandler<String> {
   
       private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
   
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
   
           Channel channel = ctx.channel();
   
           channelGroup.forEach(ch -> {
               if (channel != ch) {
                   ch.writeAndFlush(channel.remoteAddress() + " 发送的消息：" + msg + "\n");
               } else {
                   ch.writeAndFlush("【自己】" + msg + "\n");
               }
           });
       }
   
       /// handler被添加到channel的时候执行，这个动作就是由pipeline的添加handler方法完成的。对于服务端，在客户端连接进来的时候，就通过ServerBootstrapAcceptor的read方法，为每一个channel添加了handler。该方法对于handler而言是第一个触发的方法。
       @Override
       public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
           super.handlerAdded(ctx);
   
           Channel channel = ctx.channel();
           channelGroup.writeAndFlush("【服务器】- " + channel.remoteAddress() + " 加入\n");
           channelGroup.add(channel);
       }
   
       @Override
       public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
           super.handlerAdded(ctx);
   
           Channel channel = ctx.channel();
           channelGroup.writeAndFlush("【服务器】- " + channel.remoteAddress() + " 加入\n");
           channelGroup.add(channel);
       }
   
       @Override
       public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
           super.handlerRemoved(ctx);
   
           Channel channel = ctx.channel();
           channelGroup.writeAndFlush("【服务器】- " + channel.remoteAddress() + " 离开\n");
   
           System.out.println("channelGroup size:" + channelGroup.size());
       }
   
   
       @Override
       public void channelActive(ChannelHandlerContext ctx) throws Exception {
           super.channelActive(ctx);
           Channel channel = ctx.channel();
           System.out.println(channel.remoteAddress() + " 上线");
       }
   
       @Override
       public void channelInactive(ChannelHandlerContext ctx) throws Exception {
           super.channelInactive(ctx);
           Channel channel = ctx.channel();
           System.out.println(channel.remoteAddress() + " 下线");
       }
   
       @Override
       public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           super.exceptionCaught(ctx, cause);
           cause.printStackTrace();
           ctx.close();
       }
   
   }
   
   ```

   2)  客户端代码:

   ```Java
   public class MyChatClient {
   
       public static void main(String[] args) throws Exception {
   
           EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
   
           try {
   
               Bootstrap bootstrap = new Bootstrap();
               bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new MyChatClientInitializer());
   
               Channel channel = bootstrap.connect("localhost", 8899).sync().channel();
   
               BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
               // 通过死循环不停地地在客户端获取键盘的数据
               for (; ; ) {
                   channel.writeAndFlush(br.readLine() + "\r\n");
               }
   
           } finally {
               eventLoopGroup.shutdownGracefully().sync();
           }
   
       }
   }
   
   ```

   ```Java
   public class MyChatClientInitializer extends ChannelInitializer<SocketChannel> {
   
       @Override
       protected void initChannel(SocketChannel ch) throws Exception {
           ChannelPipeline pipeline = ch.pipeline();
           // 分隔符解码器, 分隔符为\n(可以查看源码)
           // 参数1: 最大数据长度, 参数2: 使用系统的默认分隔符数组, 在这里分隔符有两种:  \r, \n或者\n
           pipeline.addLast(new DelimiterBasedFrameDecoder(4096, Delimiters.lineDelimiter()));
           pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
           pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));
   
           pipeline.addLast(new MyChatClientHandler());
       }
   }
   
   ```

   ```Java
   public class MyChatClientHandler extends SimpleChannelInboundHandler<String> {
       @Override
       protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
   
           System.out.println(msg);
   
       }
   }
   
   ```

   

3. 心跳编程:

   为什么需要心跳的呢?

   如果正常TCP退出的话, 服务器和客户端都会感知4此挥手的退出机制, 如果是非正常的TCP退出的话, 服务器和客户端都是无法感知对方的退出, 这个时候就需要心跳机制来检测对方是否还在存活, 然后再继续双方通信.

   1) SimpleChannelInboundHandler和ChannelInboundHandlerAdapter的区别

   ######   源码分析:

     SimpleChannelInboundHandler

   ```Java
       @Override
       public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
           boolean release = true;
           try {
               if (acceptInboundMessage(msg)) {
                   @SuppressWarnings("unchecked")
                   I imsg = (I) msg;
                   channelRead0(ctx, imsg);
               } else {
                   release = false;
                   ctx.fireChannelRead(msg);
               }
           } finally {
               if (autoRelease && release) {
                   ReferenceCountUtil.release(msg);
               }
           }
       }
   ```

   ChannelInboundHandlerAdapter

   ```Java
       @Override
       public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
           ctx.fireChannelRead(msg);
       }
   ```

   从源码上上面，我们可以看出，当方法返回时，SimpleChannelInboundHandler会负责释放指向保存该消息的ByteBuf的内存引用。而ChannelInboundHandlerAdapter在其时间节点上不会释放消息，而是将消息传递给下一个ChannelHandler处理。

   ###### 从类定义看:

   SimpleChannelInboundHandler

   ```Java
   public abstract class SimpleChannelInboundHandler<I> extends ChannelInboundHandlerAdapter {
   ```

   ChannelInboundHandlerAdapter

   ```java
   public class ChannelInboundHandlerAdapter extends ChannelHandlerAdapter implements ChannelInboundHandler
   ```

   从类的定义中，我们可以看出:

   - SimpleChannelInboundHandler<T>是抽象类，而ChannelInboundHandlerAdapter是普通类;
   - SimpleChannelInboundHandler支持泛型的消息处理，而ChannelInboundHandlerAdapter不支持泛型

   2) 代码编写如下:

   服务器代码如下:

   ```Java
   public class MyServer {
   
       public static void main(String[] args) throws Exception {
           EventLoopGroup bossGroup = new NioEventLoopGroup();
           EventLoopGroup workerGroup = new NioEventLoopGroup();
   
           try {
               ServerBootstrap serverBootstrap = new ServerBootstrap();
               serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                       // 日志LoggingHandler是针对bossGroup, childHandler是针对workerGroup
                       handler(new LoggingHandler(LogLevel.INFO)).
                       childHandler(new MyServerInitializer());
   
               ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
               channelFuture.channel().closeFuture().sync();
           } finally {
               bossGroup.shutdownGracefully();
               workerGroup.shutdownGracefully();
           }
       }
   }
   
   ```

   ```java
   public class MyServerHandler extends ChannelInboundHandlerAdapter {
   
       @Override
       public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
           if(evt instanceof IdleStateEvent) {
               IdleStateEvent event = (IdleStateEvent)evt;
   
               String eventType = null;
   
               switch (event.state()) {
                   case READER_IDLE:
                       eventType = "读空闲";
                       break;
                   case WRITER_IDLE:
                       eventType = "写空闲";
                       break;
                   case ALL_IDLE:
                       eventType = "读写空闲";
                       break;
               }
   
               System.out.println(ctx.channel().remoteAddress() + "超时事件： " + eventType);
               ctx.channel().close();
           }
       }
   }
   
   ```

   ```java
   public class MyServerInitializer extends ChannelInitializer<SocketChannel> {
   
       @Override
       protected void initChannel(SocketChannel ch) throws Exception {
           ChannelPipeline pipeline = ch.pipeline();
           // 很多Handler, 可以看出这个是责任链设计模式
           // 参数1: 当读操作超过5秒还没有数据过来, 就出发READER_IDLE, 如果写超过7秒, 就触动WRITER_IDLE, 如果读写空闲超过3秒, 就会触动ALL_IDLE
           pipeline.addLast(new IdleStateHandler(5, 7, 10, TimeUnit.SECONDS));
           pipeline.addLast(new MyServerHandler());
       }
   }
   
   ```

## 四、Netty对webSocket的支援

   1. 服务器代码:

      ```Java
      public class MyServer {
      
          public static void main(String[] args) throws Exception {
      
              EventLoopGroup bossGroup = new NioEventLoopGroup();
              EventLoopGroup workLoopGroup = new NioEventLoopGroup();
      
              try {
      
                  ServerBootstrap serverBootstrap = new ServerBootstrap();
                  serverBootstrap.group(bossGroup, workLoopGroup).channel(NioServerSocketChannel.class)
                          .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new WebSocketChannelInitializer());
      
                  ChannelFuture channelFuture =  serverBootstrap.bind(8899).sync();
                  channelFuture.channel().closeFuture().sync();
              } finally {
                  bossGroup.shutdownGracefully().sync();
                  workLoopGroup.shutdownGracefully().sync();
              }
      
      
          }
      }
      
      ```

      ```
      public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
      
          /*
          WebSocketServerProtocolHandler：参数是访问路径，这边指定的是ws，服务客户端访问服务器的时候指定的url是：ws://localhost:8899/ws。
          它负责websocket握手以及处理控制框架（Close，Ping（心跳检检测request），Pong（心跳检测响应））。 文本和二进制数据帧被传递到管道中的下一个处理程序进行处理。
          */
          @Override
          protected void initChannel(SocketChannel ch) throws Exception {
      
              ChannelPipeline channelPipeline = ch.pipeline();
              // websocket协议本身是基于http协议的，所以这边也要使用http解编码器
              channelPipeline.addLast(new HttpServerCodec());
              // 以块的方式来写的处理器
              channelPipeline.addLast(new ChunkedWriteHandler());
              // netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
              channelPipeline.addLast(new HttpObjectAggregator(8192));
              // ws://server:port/context_path
              // ws://localhost:9999/ws
              // 参数指的是contex_path
              channelPipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
              // websocket定义了传递数据的6中frame类型
              channelPipeline.addLast(new TextWebSocketFrameHandler());
      
          }
      }
      ```

      ```Java
      /**
       * 桢：
       * WebSocket规范中定义了6种类型的桢，netty为其提供了具体的对应的POJO实现。
       * WebSocketFrame：所有桢的父类，所谓桢就是WebSocket服务在建立的时候，在通道中处理的数据类型。本列子中客户端和服务器之间处理的是文本信息。所以范型参数是TextWebSocketFrame。
       */
      public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
      
          @Override
          protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
              System.out.println("收到消息： " + msg.text());
              ctx.channel().writeAndFlush(new TextWebSocketFrame("服务器时间： " + LocalDateTime.now()));
      
          }
      
      
          @Override
          public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
              super.handlerAdded(ctx);
              System.out.println("handlerAdded: " + ctx.channel().id().asLongText());
          }
      
      
          @Override
          public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
              super.handlerRemoved(ctx);
              System.out.println("handlerRemoved: " + ctx.channel().id().asLongText());
          }
      
          @Override
          public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
              super.exceptionCaught(ctx, cause);
              ctx.close();
          }
      }
      
      ```

## 五、Netty集成Protobuf

  1. 官方文档:

     [protocol-buffers官网]: https://developers.google.com/protocol-buffers	"https://developers.google.com/protocol-buffers"

  2. 简单的编程:

     1)  安装ProtoBuf编译器:

     protobuf的github发布地址： https://github.com/protocolbuffers/protobuf/releases

     protobuf的编译器叫protoc，在上面的网址中找到最新版本的安装包，下载安装。

     这里下载的是：protoc-3.9.1-win64.zip ， windows 64位系统版本的编译器，下载后，解压到你想要的安装目录即可。

     > 提示：安装完成后，将 [protoc安装目录]/bin 路径添加到PATH环境变量中

     打开cmd，命令窗口执行protoc命令，没有报错的话，就已经安装成功。

     2) 引入jar包:

     ```xml
       <dependency>
           <groupId>com.google.protobuf</groupId>
           <artifactId>protobuf-java</artifactId>
           <version>3.17.3</version>
           </dependency>
       <dependency>
           <groupId>com.google.protobuf</groupId>
           <artifactId>protobuf-java-util</artifactId>
           <version>3.17.3</version>
       </dependency>
     ```

     3)  创建 .com.wangzunbin.proto 文件(Student.com.wangzunbin.proto)，定义数据结构:

     ```java
     syntax = "proto2";
     
     package com.shengsiyuan.protobuf;
     
     
     option optimize_for = SPEED;
     option java_package = "com.wangzunbin.protobuf";
     option java_outer_classname = "DataInfo";
     
     message Student {
       optional string name = 1;
       optional int32 age = 2;
       optional string address = 3;
     }
     ```

     4)   在根项目的控制台执行以下命令:

     ```
     protoc --java_out=src/main/java src/protobuf/Student.com.wangzunbin.proto
     ```

     就可以在src下面可以看到生产的文件:

     ![image-20210627163341273](img\5-1.png)

     5)  测试:

     ```java
     public class ProtoBufTest {
     
         public static void main(String[] args) throws Exception {
     
             DataInfo.Student student = DataInfo.Student.newBuilder().setName("张三").setAddress("杭州").setAge(24).build();
             // 编码
             byte[] student2ByteArray = student.toByteArray();
             // 解码
             DataInfo.Student student2 = DataInfo.Student.parseFrom(student2ByteArray);
     
             System.out.println(student2.getAddress());
             System.out.println(student2.getName());
             System.out.println(student2.getAge());
         }
     }
     ```

  3. Netty集成Protobuf:

     1) 定义proto文件:

     ```java
     syntax = "proto2";
     
     package com.shengsiyuan.protobuf;
     
     option optimize_for = SPEED;
     option java_package = "com.wangzunbin.netty.sixthexample";
     option java_outer_classname = "MyDataInfo";
     
     message MyMessage {
     
         enum DataType{
             PersonType = 1;
             DogType = 2;
             CatType = 3;
         }
     
         required DataType data_type = 1;
     
         oneof dataBody {
             Person person = 2;
             Dog dog = 3;
             Cat cat = 4;
         }
     }
     
     message Person {
         optional string name = 1;
         optional int32 age = 2;
         optional string address = 3;
     }
     
     message Dog {
         optional string name = 1;
         optional int32 age = 2;
     }
     
     message Cat {
         optional string name = 1;
         optional string city = 2;
     }
     ```

     2) 服务器编程:

     ```java
     public class TestServer {
     
         public static void main(String[] args) throws Exception {
             EventLoopGroup bossGroup = new NioEventLoopGroup();
             EventLoopGroup workerGroup = new NioEventLoopGroup();
     
             try {
                 ServerBootstrap serverBootstrap = new ServerBootstrap();
                 serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).
                         handler(new LoggingHandler(LogLevel.INFO)).
                         childHandler(new TestServerInitializer());
     
                 ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
                 channelFuture.channel().closeFuture().sync();
             } finally {
                 bossGroup.shutdownGracefully();
                 workerGroup.shutdownGracefully();
             }
         }
     }
     ```

     ```java
     public class TestServerInitializer extends ChannelInitializer<SocketChannel> {
     
         @Override
         protected void initChannel(SocketChannel ch) throws Exception {
             ChannelPipeline pipeline = ch.pipeline();
     
             pipeline.addLast(new ProtobufVarint32FrameDecoder());
             pipeline.addLast(new ProtobufDecoder(MyDataInfo.MyMessage.getDefaultInstance()));
             pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
             pipeline.addLast(new ProtobufEncoder());
     
             pipeline.addLast(new TestServerHandler());
         }
     }
     ```

     ```java
     public class TestServerHandler extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {
     
         @Override
         protected void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {
             MyDataInfo.MyMessage.DataType dataType = msg.getDataType();
     
             if (dataType == MyDataInfo.MyMessage.DataType.PersonType) {
                 MyDataInfo.Person person = msg.getPerson();
     
                 System.out.println(person.getName());
                 System.out.println(person.getAge());
                 System.out.println(person.getAddress());
             } else if (dataType == MyDataInfo.MyMessage.DataType.DogType) {
                 MyDataInfo.Dog dog = msg.getDog();
     
                 System.out.println(dog.getName());
                 System.out.println(dog.getAge());
             } else {
                 MyDataInfo.Cat cat = msg.getCat();
     
                 System.out.println(cat.getName());
                 System.out.println(cat.getCity());
             }
         }
     }
     
     ```

     3) 客户端代码:

     ```java
     public class TestClient {
     
         public static void main(String[] args) throws Exception {
             EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
     
             try {
                 Bootstrap bootstrap = new Bootstrap();
                 bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).
                         handler(new TestClientInitializer());
     
                 ChannelFuture channelFuture = bootstrap.connect("localhost", 8899).sync();
                 channelFuture.channel().closeFuture().sync();
     
             } finally {
                 eventLoopGroup.shutdownGracefully();
             }
         }
     }
     
     ```

     ```java
     public class TestClientInitializer extends ChannelInitializer<SocketChannel>{
     
         @Override
         protected void initChannel(SocketChannel ch) throws Exception {
             ChannelPipeline pipeline = ch.pipeline();
     
             pipeline.addLast(new ProtobufVarint32FrameDecoder());
             pipeline.addLast(new ProtobufDecoder(MyDataInfo.MyMessage.getDefaultInstance()));
             pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
             pipeline.addLast(new ProtobufEncoder());
     
             pipeline.addLast(new TestClientHandler());
         }
     }
     ```

     ```java
     public class TestClientHandler extends SimpleChannelInboundHandler<MyDataInfo.MyMessage> {
     
         @Override
         protected void channelRead0(ChannelHandlerContext ctx, MyDataInfo.MyMessage msg) throws Exception {
     
         }
     
         @Override
         public void channelActive(ChannelHandlerContext ctx) throws Exception {
             int randomInt = new Random().nextInt(3);
     
             MyDataInfo.MyMessage myMessage = null;
     
             if (0 == randomInt) {
                 myMessage = MyDataInfo.MyMessage.newBuilder().
                         setDataType(MyDataInfo.MyMessage.DataType.PersonType).
                         setPerson(MyDataInfo.Person.newBuilder().
                                 setName("张三").setAge(20).
                                 setAddress("北京").build()).
                         build();
             } else if (1 == randomInt) {
                 myMessage = MyDataInfo.MyMessage.newBuilder().
                         setDataType(MyDataInfo.MyMessage.DataType.DogType).
                         setDog(MyDataInfo.Dog.newBuilder().
                                 setName("一只狗").setAge(2).
                                 build()).
                         build();
             } else {
                 myMessage = MyDataInfo.MyMessage.newBuilder().
                         setDataType(MyDataInfo.MyMessage.DataType.CatType).
                         setCat(MyDataInfo.Cat.newBuilder().
                                 setName("一只猫").setCity("上海").
                                 build()).
                         build();
             }
     
             ctx.channel().writeAndFlush(myMessage);
         }
     }
     
     ```

     注: 采用git submodule(旧方法)和git subtree(新方法)

## 六、Netty继承Thrift

   1. Thrift的官网: 

      [Thrift官网]: https://thrift.apache.org/	"https://thrift.apache.org/"
      [Thrift中文教程]: https://juejin.cn/post/6844903622380093447	"https://juejin.cn/post/6844903622380093447"

      Thrift主要是实现RPC

   2. 引入Jar包:

      ```xml
        <dependency>
           <groupId>org.apache.thrift</groupId>
           <artifactId>libthrift</artifactId>
           <version>0.14.2</version>
        </dependency>
      
      ```
      
   3. 编写一个thrift简单Socket通信:

      1) 定义一个data.thrift文件

      ```java
      // 定义控件. 相当于java的包
      namespace java thrift.generated
      namespace py py.thrift.generated
      
      //  定义java别名
      typedef i16 short
      typedef i32 int
      typedef i64 long
      typedef bool boolean
      typedef string String
      
      struct Person {
          1: optional String username,
          2: optional int age,
          3: optional boolean married
      }
      
      exception DataException {
          1: optional String message,
          2: optional String callStack,
          3: optional String date
      }
      
      service PersonService {
      
          Person getPersonByUsername(1: required String username) throws (1: DataException dataException),
      
          void savePerson(1: required Person person) throws (1: DataException dataException)
      }
      ```

      生成java代码命令如下:

      ```shell
      thrift --gen java src/thrift/data.thrift
      ```

      2) 实现idl服务类:

      ```java
      public class PersonServiceImpl implements PersonService.Iface {
          @Override
          public Person getPersonByUsername(String username) throws DataException, TException {
              System.out.println("Got Client Param: " + username);
              Person person = new Person();
              person.setUsername(username);
              person.setAge(22);
              person.setMarried(false);
      
              System.out.println("getPersonByUsername success:"+person.getUsername());
              return person;
          }
      
          @Override
          public void savePerson(Person person) throws DataException, TException {
              System.out.println("Got Client Param: ");
      
              System.out.println(person.getUsername());
              System.out.println(person.getAge());
              System.out.println(person.isMarried());
      
              System.out.println("Got Client savePerson success!! ");
          }
      }
      
      ```

      3) 服务器代码:

      ```java
      public class ThriftServer {
      
          public static void main(String[] args) throws Exception {
      
              // 非阻塞的Socket(半读半写)
              TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(8899);
      
              THsHaServer.Args thServerArgs = new THsHaServer.Args(serverSocket).minWorkerThreads(2).maxWorkerThreads(4);
      
              PersonService.Processor<PersonServiceImpl> processor = new PersonService.Processor<>(new PersonServiceImpl());
      
              // 协议层和传输层在服务器和客户端申明必须是一致的
              // 协议层
              thServerArgs.protocolFactory(new TCompactProtocol.Factory());
              // 传输层
              thServerArgs.transportFactory(new TFramedTransport.Factory());
              thServerArgs.processorFactory(new TProcessorFactory(processor));
      
              TServer server = new THsHaServer(thServerArgs);
      
              System.out.println("Thrift Server Started!");
              //  死循环
              server.serve();
      
          }
      }
      
      ```

      4) 客户端代码:

      ```java
      public class ThriftClient {
      
          public static void main(String[] args) throws TTransportException {
      
              TTransport transport = new TFramedTransport(new TSocket("localhost", 8899), 600);
              TProtocol protocol = new TCompactProtocol(transport);
              PersonService.Client client = new PersonService.Client(protocol);
      
              try {
      
                  transport.open();
      
                  Person person = client.getPersonByUsername("战三");
                  System.out.println(person.getUsername());
                  System.out.println(person.getAge());
                  System.out.println(person.isMarried());
      
                  System.out.println("-------");
      
                  Person person2 = new Person();
      
                  person2.setUsername("李四");
                  person2.setAge(30);
                  person2.setMarried(true);
      
                  client.savePerson(person2);
      
              } catch (Exception ex) {
                  throw new RuntimeException(ex.getMessage(), ex);
              } finally {
                  transport.close();
              }
          }
      }
      ```

## 七、Netty使用GRPC

  1. 官网: 

     [GRPC官网]: https://grpc.io/	"https://grpc.io/"

  2. GRPC  VS  Thrift 

​			1)  GRPC 优点如下:

​            需要良好的文档、示例
​			喜欢、习惯HTTP/2、ProtoBuf
​			对网络传输带宽敏感

​            2)  Thrift 优点如下:

​           需要在非常多的语言间进行数据交换

​           对CPU敏感

​           协议层、传输层有多种控制要求

​           需要稳定的版本

​          不需要良好的文档和示例

3. Github官方教程

   [GRPC的Github官方教程]: https://github.com/grpc/grpc-java	"https://github.com/grpc/grpc-java"

4. GRPC编程:

   1) 引入Jar包:

   ```xml
   <dependency>
     <groupId>io.grpc</groupId>
     <artifactId>grpc-netty-shaded</artifactId>
     <version>1.38.0</version>
   </dependency>
   <dependency>
     <groupId>io.grpc</groupId>
     <artifactId>grpc-protobuf</artifactId>
     <version>1.38.0</version>
   </dependency>
   <dependency>
     <groupId>io.grpc</groupId>
     <artifactId>grpc-stub</artifactId>
     <version>1.38.0</version>
   </dependency>
   <dependency> <!-- necessary for Java 9+ -->
     <groupId>org.apache.tomcat</groupId>
     <artifactId>annotations-api</artifactId>
     <version>6.0.53</version>
     <scope>provided</scope>
   </dependency>
   ```

   2) 引入GRPC和Proto的编译插件:

   ```xml
   <build>
     <extensions>
       <extension>
         <groupId>kr.motd.maven</groupId>
         <artifactId>os-maven-plugin</artifactId>
         <version>1.6.2</version>
       </extension>
     </extensions>
     <plugins>
       <plugin>
         <groupId>org.xolstice.maven.plugins</groupId>
         <artifactId>protobuf-maven-plugin</artifactId>
         <version>0.6.1</version>
         <configuration>
           <protocArtifact>com.google.protobuf:protoc:3.12.0:exe:${os.detected.classifier}</protocArtifact>
           <pluginId>grpc-java</pluginId>
           <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.38.0:exe:${os.detected.classifier}</pluginArtifact>
         </configuration>
         <executions>
           <execution>
             <goals>
               <goal>compile</goal>
               <goal>compile-custom</goal>
             </goals>
           </execution>
         </executions>
       </plugin>
     </plugins>
   </build>
   
   ```

   3) 如何把proto文件生成为java代码:

   请参考这个文章:  

   [maven集成Protobuff并创建GRpc示例]: https://blog.csdn.net/mbshqqb/article/details/79584949

   4) 服务器代码:

   ```java
   public class GrpcServer {
   
       private Server server;
   
       private void start() throws IOException {
           this.server = ServerBuilder.forPort(8899).addService(new StudentServiceImpl()).build().start();
   
           System.out.println("server started!");
   
           // 优雅地退出
           Runtime.getRuntime().addShutdownHook(new Thread(() -> {
               System.out.println("关闭jvm");
               GrpcServer.this.stop();
           }));
       }
   
       private void stop() {
           if(null != this.server) {
               this.server.shutdown();
           }
       }
   
       private void awaitTermination() throws InterruptedException {
           if(null != this.server) {
               this.server.awaitTermination();
           }
       }
   
       public static void main(String[] args) throws IOException, InterruptedException {
           GrpcServer server = new GrpcServer();
   
           server.start();
           // 让服务器等待（否则启动完就会退出）
           server.awaitTermination();
       }
   }
   
   ```

   ```java
   public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {
   
       @Override
       public void getRealNameByUsername(MyRequest request, StreamObserver<MyResponse> responseObserver) {
           System.out.println("接受到客户端信息： " + request.getUsername());
   
           responseObserver.onNext(MyResponse.newBuilder().setRealname("张三").build());
           responseObserver.onCompleted();
       }
   
       @Override
       public void getStudentsByAge(StudentRequest request, StreamObserver<StudentResponse> responseObserver) {
   
           System.out.println("接受到客户端信息： " + request.getAge());
   
           responseObserver.onNext(StudentResponse.newBuilder().setName("张三").setAge(20).setCity("北京").build());
           responseObserver.onNext(StudentResponse.newBuilder().setName("李四").setAge(30).setCity("天津").build());
           responseObserver.onNext(StudentResponse.newBuilder().setName("王五").setAge(40).setCity("成都").build());
           responseObserver.onNext(StudentResponse.newBuilder().setName("赵六").setAge(50).setCity("深圳").build());
   
           responseObserver.onCompleted();
       }
   
       @Override
       public StreamObserver<StudentRequest> getStudentsWrapperByAges(StreamObserver<StudentResponseList> responseObserver) {
           return new StreamObserver<StudentRequest>() {
   
               @Override
               public void onNext(StudentRequest value) {
                   System.out.println("onNext: " + value.getAge());
               }
   
               @Override
               public void onError(Throwable t) {
                   System.out.println(t.getMessage());
               }
   
               @Override
               public void onCompleted() {
                   StudentResponse studentResponse = StudentResponse.newBuilder().setName("张三").setAge(20).setCity("西安").build();
                   StudentResponse studentResponse2 = StudentResponse.newBuilder().setName("李四").setAge(30).setCity("广州").build();
   
                   StudentResponseList studentResponseList = StudentResponseList.newBuilder().
                           addStudentResponse(studentResponse).addStudentResponse(studentResponse2).build();
   
                   // 把结果返回给客户端（考虑到会有stream情况）
                   responseObserver.onNext(studentResponseList);
                   // 告知客户端服务端，服务端已执行完毕
                   responseObserver.onCompleted();
               }
           };
       }
   
       @Override
       public StreamObserver<StreamRequest> biTalk(StreamObserver<StreamResponse> responseObserver) {
           return new StreamObserver<StreamRequest>() {
               @Override
               public void onNext(StreamRequest value) {
                   System.out.println(value.getRequestInfo());
   
                   responseObserver.onNext(StreamResponse.newBuilder().setResponseInfo(UUID.randomUUID().toString()).build());
               }
   
               @Override
               public void onError(Throwable t) {
                   System.out.println(t.getMessage());
               }
   
               @Override
               public void onCompleted() {
                   responseObserver.onCompleted();
               }
           };
       }
   }
   
   ```

   5) 客户端代码:

   ```java
   public class GrpcClient {
   
       public static void main(String[] args) {
           ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8899).
                   usePlaintext().build();
           StudentServiceGrpc.StudentServiceBlockingStub blockingStub = StudentServiceGrpc.
                   newBlockingStub(managedChannel);
           StudentServiceGrpc.StudentServiceStub stub = StudentServiceGrpc.newStub(managedChannel);
   
           MyResponse myResponse = blockingStub.
                   getRealNameByUsername(MyRequest.newBuilder().setUsername("zhangsan").build());
   
           System.out.println(myResponse.getRealname());
   
           System.out.println("---------------------------------");
           Iterator<StudentResponse> iterator = blockingStub.getStudentsByAge(StudentRequest.newBuilder().setAge(20).buildPartial());
   
           while (iterator.hasNext()) {
               StudentResponse studentResponse = iterator.next();
               System.out.println(studentResponse.getName() + ", " + studentResponse.getAge() + ", " + studentResponse.getCity());
           }
   
   
           System.out.println("--------------------");
           StreamObserver<StudentResponseList> studentResponseListStreamObserver = new StreamObserver<StudentResponseList>() {
               @Override
               public void onNext(StudentResponseList value) {
                   value.getStudentResponseList().forEach(studentResponse -> {
                       System.out.println(studentResponse.getName());
                       System.out.println(studentResponse.getAge());
                       System.out.println(studentResponse.getCity());
                       System.out.println("******");
                   });
               }
   
               @Override
               public void onError(Throwable t) {
                   System.out.println(t.getMessage());
               }
   
               @Override
               public void onCompleted() {
                   System.out.println("completed!");
               }
           };
   
           StreamObserver<StudentRequest> studentRequestStreamObserver = stub.getStudentsWrapperByAges(studentResponseListStreamObserver);
   
           studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(20).build());
           studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(30).build());
           studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(40).build());
           studentRequestStreamObserver.onNext(StudentRequest.newBuilder().setAge(50).build());
   
           studentRequestStreamObserver.onCompleted();
   
   
           System.out.println("--------------------*********");
   
           StreamObserver<StreamRequest> requestStreamObserver = stub.biTalk(new StreamObserver<StreamResponse>() {
               @Override
               public void onNext(StreamResponse value) {
                   System.out.println(value.getResponseInfo());
               }
   
               @Override
               public void onError(Throwable t) {
                   System.out.println(t.getMessage());
               }
   
               @Override
               public void onCompleted() {
                   System.out.println("onCompleted");
               }
           });
   
           for(int i = 0; i < 10; ++i) {
               requestStreamObserver.onNext(StreamRequest.newBuilder().setRequestInfo(LocalDateTime.now().toString()).build());
   
               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
           System.out.println("--------------------*********");
           try {
               Thread.sleep(15000);
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
   
   
       }
   }
   ```

5. 

