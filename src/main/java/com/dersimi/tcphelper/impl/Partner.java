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

import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class Partner {
    private final Map<Integer, Packet> packets;

    protected final ChannelHandler channelHandler;

    protected final boolean epoll, ssl;

    protected final int port, timeout;

    protected final Charset charset;

    protected Partner(int port, ChannelHandler channelHandler, boolean epoll, boolean ssl, Charset charset, int timeout) {
        if (epoll && !Epoll.isAvailable())
            throw new UnsupportedOperationException("epoll isn't available");

        packets = new HashMap<>();
        this.channelHandler = channelHandler;
        this.port = port;
        this.epoll = epoll;
        this.ssl = ssl;
        this.charset = charset == null ? StandardCharsets.US_ASCII : charset;
        this.timeout = timeout;
    }

    /**
     * Init client/server
     *
     */
    protected void init() throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    /**
     * Terminate client/server
     */
    protected void shutdown() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates needed event loop group
     *
     * @param threads See netty documentation {@link EventLoopGroup}
     * @return the created EventLoopGroup
     */
    protected EventLoopGroup createGroup(int threads) {
        var factory = new DefaultThreadFactory("Network");
        return epoll ? new EpollEventLoopGroup(threads, factory) : new NioEventLoopGroup(threads, factory);
    }

    /**
     * Register a packet
     *
     * @param id     Packet id, must be greater than 0
     * @param packet Packet
     * @throws IllegalStateException If id is in valid
     */
    public void registerPacket(int id, Packet packet) {
        if (id < 0)
            throw new IllegalStateException("id is invalid");

        Objects.requireNonNull(packet, "packet is null");

        if (packets.containsKey(id))
            throw new IllegalStateException("packet with id " + id + " already exists");

        packets.put(id, packet);
    }

    /**
     * Returns the id of a given packet.
     *
     * @param packet The packet
     * @return Packet id, in case of unknown packet, 1 will be returned
     */
    protected int getIdByPacket(Packet packet) {
        Objects.requireNonNull(packet, "packet can not be null");

        if (packet instanceof Initializer.AlivePacket)
            return -1;

        for (Map.Entry<Integer, Packet> entry : packets.entrySet())
            if (entry.getValue().getClass().equals(packet.getClass()))
                return entry.getKey();

        return -2;
    }

    /**
     * Returns packet by id
     *
     * @param id the packet id
     * @return The packet
     */
    protected Packet getPacketById(int id) {
        var packet = packets.get(id);

        if (packet == null)
            throw new IllegalStateException("there is no packet with id " + id);

        return packet;
    }
}
