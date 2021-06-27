package com.wangzunbin.protobuf;

/**
 * ClassName:ProtoBufTest
 * Function:
 *
 * @author WangZunBin
 * @version 1.0 2021/6/27 16:17
 */
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

