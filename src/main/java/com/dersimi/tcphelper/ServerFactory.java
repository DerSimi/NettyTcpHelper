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

package com.dersimi.tcphelper;

import com.dersimi.tcphelper.impl.Packet;
import com.dersimi.tcphelper.impl.Server;
import io.netty.channel.ChannelHandler;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ServerFactory {
    private int port, timeout;

    private ChannelHandler channelHandler;

    private boolean epoll, ssl;

    private Charset charset;

    private final Map<Integer, Packet> packets;

    public ServerFactory() {
        packets = new HashMap<>();
    }

    /**
     * Set port
     * @param port Port number
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Set timeout
     * @param timeout 0 to disable
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Set netty channel handler
     * @param channelHandler may be null
     */
    public void setChannelHandler(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
    }

    /**
     * Activate epoll
     * @param epoll True if epoll should be active
     */
    public void setEpoll(boolean epoll) {
        this.epoll = epoll;
    }

    /**
     * Activate ssl
     * @param ssl True if ssl should be active
     */
    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * Set charset
     * @param charset The charset
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Register a packet
     * @param packetId make sure it's only a positive natural number
     * @param packet Packet
     */
    public void registerPacket(int packetId, Packet packet) {
        packets.put(packetId, packet);
    }

    /**
     * Create server instance
     * @return Server
     */
    public Server create() {
        var s = new Server(port, channelHandler, epoll, ssl, charset, timeout);

        for(var entry : packets.entrySet())
            s.registerPacket(entry.getKey(), entry.getValue());

        //is inefficient, however, it makes the checking for invalid arguments much easier.

        return s;
    }
}
