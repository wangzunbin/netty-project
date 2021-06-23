package com.wangzunbin.netty.firstexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ClassName:NettyServer  <br/>
 * Function:  <br/>
 *
 * @author WangZunBin <br/>
 * @version 1.0 2021/6/23 17:42   <br/>
 */

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
