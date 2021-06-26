package com.wangzunbin.netty.fourthexample;


import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;


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
