package com.dersimi.tcphelper.example;

import com.dersimi.tcphelper.ClientFactory;
import com.dersimi.tcphelper.impl.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ClientExample extends SimpleChannelInboundHandler<Packet> {

    public ClientExample() {
        var clientFactory = new ClientFactory();

        clientFactory.setHost("127.0.0.1");
        clientFactory.setPort(1234);
        clientFactory.setChannelHandler(this);//not required
        clientFactory.setSsl(true);
        clientFactory.setAliveSenderInterval(3);//Client will send every 3 seconds an AlivePacket
        clientFactory.setAutoReconnectTime(5);//Client will attempt to reconnect after 5 seconds

        clientFactory.setConnectionHandler((future, failedAttempts) -> {
            if(future.isSuccess()) {
                System.out.println("Yeah, I connected!");
                future.channel().writeAndFlush(new TestPacket("Cool, super cool!", 111));
            } else {
                System.out.println("Connection attempt " + failedAttempts + " failed! =(");
            }
        });
        //in case of auto reconnect, the connection handler is called over and over again, so implement your
        //own logic.

        //Register packets
        clientFactory.registerPacket(5, new TestPacket());

        //Init client
        try {
            clientFactory.create().init();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        System.out.println("Client receiving packet... ");

        if(msg instanceof TestPacket testPacket)
            System.out.println(testPacket.getName() + " : " + testPacket.getAge());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Connected");

        ctx.channel().writeAndFlush(new TestPacket("Welcome Server!", 271));
    }

    public static void main(String[] args) {
        new ClientExample();
    }
}
