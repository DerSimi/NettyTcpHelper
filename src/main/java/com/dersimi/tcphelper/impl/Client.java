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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;

public class Client extends Partner {
    private EventLoopGroup eventLoopGroup;

    protected final String host;

    private final ConnectionHandler connectionHandler;

    protected int failedAttempts, reconnectTime;

    /**
     * Client constructor
     *
     * @param host              Host address
     * @param port              Port number
     * @param channelHandler    Netty ChannelHandler, may be null
     * @param epoll             Epoll
     * @param ssl               Ssl
     * @param charset           Charset
     * @param connectionHandler A connection handler delivers a ChannelFuture and is always required.
     * @param timeout           After this time, the client send its alive packet. Disable it with 0
     * @param reconnectTime     Reconnect time in seconds. Disable auto reconnection with 0
     */
    public Client(String host, int port, ChannelHandler channelHandler, boolean epoll, boolean ssl, Charset charset, ConnectionHandler connectionHandler, int timeout, int reconnectTime) {
        super(port, channelHandler, epoll, ssl, charset, timeout);

        this.host = host;
        this.connectionHandler = connectionHandler;
        this.failedAttempts = 0;
        this.reconnectTime = reconnectTime;
    }

    /**
     * Initialize Client
     */
    @Override
    public void init() throws InterruptedException {
        shutdown();//in case of reinitialization

        eventLoopGroup = createGroup(0);

        var b = new Bootstrap();

        b.group(eventLoopGroup).channel(epoll ? EpollSocketChannel.class : NioSocketChannel.class).handler(new Initializer(this));

        b.connect(host, port).addListener((ChannelFuture f) -> {
            if (connectionHandler != null) {
                failedAttempts = f.isSuccess() ? 0 : failedAttempts + 1;

                connectionHandler.run(f, failedAttempts);
            }
        }).sync().channel().closeFuture().syncUninterruptibly();
    }

    /**
     * Terminate client
     */
    @Override
    public void shutdown() {
        if (eventLoopGroup != null)
            eventLoopGroup.shutdownGracefully();

        eventLoopGroup = null;
    }

    @FunctionalInterface
    public interface ConnectionHandler {
        /**
         * Implement method to control actions after connection or in case of reconnect
         *
         * @param future         ChannelFuture
         * @param failedAttempts counts failed attempts, is reset to zero after successful reconnection
         */
        void run(ChannelFuture future, int failedAttempts);
    }
}
