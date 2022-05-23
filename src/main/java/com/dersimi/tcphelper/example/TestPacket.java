package com.dersimi.tcphelper.example;

import com.dersimi.tcphelper.impl.Packet;
import com.dersimi.tcphelper.impl.PacketByteBuf;
import io.netty.channel.Channel;

public class TestPacket implements Packet {
    private String name;
    private int age;

    public TestPacket() {}

    public TestPacket(String name, int age) {
        this.name = name;
        this.age = age;
    }
    @Override
    public void read(Channel channel, PacketByteBuf in) {
        this.name = in.readString();
        this.age = in.readInt();
    }

    @Override
    public void write(PacketByteBuf out) {
        out.writeString(name);
        out.writeInt(age);
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }
}
