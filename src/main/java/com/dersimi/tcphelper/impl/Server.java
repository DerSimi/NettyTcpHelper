/*
 * Copyright 2022 DerSimi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dersimi.tcphelper.impl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.nio.charset.Charset;

public class Server extends Partner {
    private EventLoopGroup bossGroup, workerGroup;

    /**
     * @param port           Port number
     * @param channelHandler ChannelHandler, {@link ChannelHandler}, may be null
     * @param epoll          True if epoll is permitted.
     * @param ssl            True if ssl should be used.
     * @param charset        Charset, in case of null, the default charset US_ASCII is used.
     * @param timeout        read timeout in seconds, is disabled in case of 0
     */
    public Server(int port, ChannelHandler channelHandler, boolean epoll, boolean ssl, Charset charset, int timeout) {
        super(port, channelHandler, epoll, ssl, charset, timeout);
    }

    /**
     * Initialize server
     */
    @Override
    public void init() throws InterruptedException {
        bossGroup = createGroup(1);
        workerGroup = createGroup(0);

        var b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class).childHandler(new Initializer(this));

        try {
            b.bind(port).sync().channel().closeFuture().syncUninterruptibly();
        } finally {
            shutdown();
        }
    }

    /**
     * Terminate server
     */
    @Override
    public void shutdown() {
        if (bossGroup != null)
            bossGroup.shutdownGracefully();

        if (workerGroup != null)
            workerGroup.shutdownGracefully();

        bossGroup = null;
        workerGroup = null;
    }
}
