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

import io.netty.channel.*;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

public class Initializer extends ChannelInitializer {

    private final Logger LOGGER = LogManager.getLogger(Initializer.class);

    private final Partner partner;

    protected Initializer(Partner partner) {
        this.partner = partner;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        var cp = ch.pipeline();

        //ssl stuff
        if (partner.ssl) {
            if (partner instanceof Client client) {
                var sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();

                cp.addLast(sslCtx.newHandler(ch.alloc(), client.host, partner.port));
            } else {//Server
                var ssc = new SelfSignedCertificate();
                var sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
                cp.addLast(sslCtx.newHandler(ch.alloc()));
            }
        }

        //timeout handler
        if (partner.timeout != 0) {
            if (partner instanceof Client) {
                cp.addLast(new IdleStateHandler(0, partner.timeout, 0));

                cp.addLast(new ChannelDuplexHandler() {
                    @Override
                    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                        if (evt instanceof IdleStateEvent e) {
                            if (e.state() == IdleState.WRITER_IDLE) {
                                ctx.channel().writeAndFlush(new AlivePacket());

                                LOGGER.debug("Alive packet");
                            }
                        }
                    }
                });
            } else//Server
                cp.addLast(new ReadTimeoutHandler(partner.timeout));
        }

        //client auto reconnect handler
        if (partner instanceof Client client && client.reconnectTime != 0) {
            cp.addLast(new SimpleChannelInboundHandler<>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                }

                @Override
                public void channelUnregistered(ChannelHandlerContext ctx) {
                    ctx.channel().eventLoop().schedule(() -> {
                        try {
                            client.init();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, client.reconnectTime, TimeUnit.SECONDS);
                }
            });
        }

        cp.addLast(new Decoder(partner), new Encoder(partner));

        if (partner.channelHandler != null) //channel handler is not always required
            cp.addLast(partner.channelHandler);
    }

    /**
     * Internal AlivePacket, send by client, packetId = -1
     */
    protected static class AlivePacket implements Packet {
        @Override
        public void read(Channel channel, PacketByteBuf byteBuf) {
        }

        @Override
        public void write(PacketByteBuf byteBuf) {
        }
    }
}
