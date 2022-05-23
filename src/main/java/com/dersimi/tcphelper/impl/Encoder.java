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
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Encoder extends MessageToByteEncoder<Packet> {

    private final Logger LOGGER = LogManager.getLogger(Encoder.class);

    private final Partner partner;

    protected Encoder(Partner partner) {
        this.partner = partner;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
        int packetId = partner.getIdByPacket(msg);

        LOGGER.debug(String.format("Start encoding of %s with id = %d", msg.getClass().getSimpleName(), packetId));

        if (packetId == -2) {
            LOGGER.error(new IllegalStateException("Unregistered packet inserted: " + msg.getClass().getSimpleName()));
            return;
        }

        out.writeInt(packetId);

        if(packetId == -1)//Alive packet
            return;

        try {
            msg.write(new PacketByteBuf(out, partner.charset));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(String.format("Exception while encoding packet %s(id = %d) occurred:", msg.getClass().getSimpleName(), packetId), e);
        }

        LOGGER.debug(String.format("Sending packet %s(id = %d)", msg.getClass().getSimpleName(), packetId));
    }
}
