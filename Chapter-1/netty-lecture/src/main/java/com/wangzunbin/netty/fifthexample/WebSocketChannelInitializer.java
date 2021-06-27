package com.wangzunbin.netty.fifthexample;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

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
