package com.dersimi.tcphelper.example;

import com.dersimi.tcphelper.ServerFactory;
import com.dersimi.tcphelper.impl.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerExample extends SimpleChannelInboundHandler<Packet> {

    public ServerExample() {
        var serverFactory = new ServerFactory();

        serverFactory.setPort(1234);
        serverFactory.setChannelHandler(this);//not required
        serverFactory.setSsl(true);
        serverFactory.setTimeout(10);//Server will terminate connection after 10sec => ReadTimeout

        //Register packets
        serverFactory.registerPacket(5, new TestPacket());

        //init server
        try {
            serverFactory.create().init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        System.out.println("Server receiving packet... ");

        if(msg instanceof TestPacket testPacket)
            System.out.println(testPacket.getName() + " : " + testPacket.getAge());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Someone connected");

        ctx.channel().writeAndFlush(new TestPacket("Welcome Client!", 314));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        System.out.println("Someone disconnected");
    }

    public static void main(String[] args) {
        new ServerExample();
    }
}
