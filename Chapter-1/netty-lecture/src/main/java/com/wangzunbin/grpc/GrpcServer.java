package com.wangzunbin.grpc;


import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

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
