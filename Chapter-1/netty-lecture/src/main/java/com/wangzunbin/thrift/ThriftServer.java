package com.wangzunbin.thrift;

import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.layered.TFramedTransport;

import thrift.generated.PersonService;

public class ThriftServer {

    public static void main(String[] args) throws Exception {

        // 非阻塞的Socket
        TNonblockingServerSocket serverSocket = new TNonblockingServerSocket(8899);

        THsHaServer.Args thServerArgs = new THsHaServer.Args(serverSocket).minWorkerThreads(2).maxWorkerThreads(4);

        PersonService.Processor<PersonServiceImpl> processor = new PersonService.Processor<>(new PersonServiceImpl());

        thServerArgs.protocolFactory(new TCompactProtocol.Factory());
        thServerArgs.transportFactory(new TFramedTransport.Factory());
        thServerArgs.processorFactory(new TProcessorFactory(processor));

        TServer server = new THsHaServer(thServerArgs);

        System.out.println("Thrift Server Started!");

        server.serve();

    }
}
