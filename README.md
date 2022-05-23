# NettyTcpHelper
Java Network API based on netty

## Features
- Intuitive way to set up a tcp based client server connection,
- Easy packet system,
- SSL functionality,
- Epoll & nio,
- Client sided alive sender,
- Client sided auto reconnect functionality
- Timeout functionality for server side
- Extended functionality of ByteBuf, added: write & read String functionality, write & read String list, write & read UUID

## Example
First set up a Packet

```
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
```

After that, you need to build a client or a server. You can do that
directly by using the constructor of Server or Client, or you utilize
the factory classes ClientFactory and ServerFactory:

Let's begin with the server side:
```
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
```

And the client side:
```
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
```

That's it!
