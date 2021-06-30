package com.wangzunbin.nio;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NioTest4 {

    public static void main(String[] args) throws Exception {
        FileInputStream inputStream = new FileInputStream("input.txt");
        FileOutputStream outputStream = new FileOutputStream("output.txt");

        FileChannel inputChannel = inputStream.getChannel();
        FileChannel outputChannel = outputStream.getChannel();

        ByteBuffer buffer = ByteBuffer.allocate(512);

        while(true) {
            //注释掉buffer.clear()后， 在while循环的最后一句outputChannel.write执行后
            // buffer的position等于limit，这样read返回为0
            // while循环无法结束，flip会导致每次把buffer的内容重复写入
            buffer.clear();

            int read = inputChannel.read(buffer);

            System.out.println("read: " + read);

            if(-1 == read) {
                break;
            }

            buffer.flip();

            outputChannel.write(buffer);
        }

        inputStream.close();
        outputStream.close();
    }
}
