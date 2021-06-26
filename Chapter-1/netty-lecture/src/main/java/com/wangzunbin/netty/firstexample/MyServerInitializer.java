package com.wangzunbin.netty.firstexample;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

/**
 * ClassName:ServerInitializer  <br/>
 * Function:  <br/>
 *  连接一旦创建的时候, 就会调用这个初始化器
 * @author WangZunBin <br/>
 * @version 1.0 2021/6/23 19:32   <br/>
 */

public class MyServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 参数1, 如果为null, netty自动给这个类加上名称, 如果有的话, 就不会自动加上名称
        // 参数2: 不能搞成单例的, 要new出来
        // HttpServerCodec分为HttpRequestDecoder和HttpResponseEncoder, Netty一般来说分为解码和编码, 这个类已经合二为一了
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        pipeline.addLast("httpServerHandler", new MyHttpServerHandler());
    }

}
