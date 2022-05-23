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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Decoder extends ByteToMessageDecoder {

    private final Logger LOGGER = LogManager.getLogger(Decoder.class);

    private final Partner partner;

    protected Decoder(Partner partner) {
        this.partner = partner;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        var packetId = in.readInt();

        if(packetId == -1) {
            LOGGER.debug("Alive packet from " + ctx.channel().remoteAddress().toString() + " received.");
            return;
        }

        if (packetId < 0)
            LOGGER.warn("Data corruption happened! Packet id: " + packetId);

        var packet = partner.getPacketById(packetId).getClass().getDeclaredConstructor().newInstance();

        LOGGER.debug(String.format("Reading packet %s(id = %d)", packet.getClass().getSimpleName(), packetId));

        try {
            packet.read(ctx.channel(), new PacketByteBuf(in, partner.charset));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("Exception while reading packet %s(id = %d) occurred:", packet.getClass().getSimpleName(), packetId), e);
        }

        ctx.fireChannelRead(packet);
        ctx.fireChannelReadComplete();
    }
}
