package com.wangzunbin.netty.secondexample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * ClassName:MyServer
 * Function:
 *
 * @author WangZunBin
 * @version 1.0 2021/6/26 19:26
 */

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

/*
public interface EventLoopGroup extends EventExecutorGroup {

     * Return the next {@link EventLoop} to use
     * 一个EventLoopGroup 有多个EventLoop ，方法得到下一个EventLoop

    @Override
    EventLoop next();


     * Register a {@link Channel} with this {@link EventLoop}. The returned {@link ChannelFuture}
     * will get notified once the registration was complete.
     * 将参数channel 注册到EventLoop当中，然后注册完毕之后会异步的ChannelFuture 返回到ChannelFuture 当中。

    ChannelFuture register(Channel channel);


     * Register a {@link Channel} with this {@link EventLoop} using a {@link ChannelFuture}. The passed
     * {@link ChannelFuture} will get notified once the registration was complete and also will get returned.
     * 也是讲channel注册到EventLoop当中，当时我们发现 参数是ChannelPromise 类型的，不是Channel 类型的，那只有一种可能，
     * ChannelPromise 里边包含Channel 的引用，后续会展开讲解。

    ChannelFuture register(ChannelPromise promise);
     * Register a {@link Channel} with this {@link EventLoop}. The passed {@link ChannelFuture}
     * will get notified once the registration was complete and also will get returned.
     *
     * @deprecated Use {@link #register(ChannelPromise)} instead.
     * 废弃的注册，在 ChannelFuture register(ChannelPromise promise);方法当中ChannelPromise 已经包含了Channel 的引用，那么这个
     * 方法把Channel 也作为参数，是一种功能上的重复，因此被Deprecated，不推荐使用。

    @Deprecated
    ChannelFuture register(Channel channel, ChannelPromise promise);
}
 */

/*
//ChannelFuture 的父类Future继承了java.util.concurrent.Future,是对结果的一些判断或者监听的操作。
public interface ChannelFuture extends Future<Void> {

}
 */
