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
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ByteProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

public class PacketByteBuf extends ByteBuf {
    private final ByteBuf byteBuf;
    private final Charset charset;

    /**
     * PacketByteBuf constructor
     *
     * @param byteBuf
     * @param charset
     */
    protected PacketByteBuf(ByteBuf byteBuf, Charset charset) {
        Objects.requireNonNull(byteBuf, "byteBuf is null");
        Objects.requireNonNull(charset, "charset is null");

        this.byteBuf = byteBuf;
        this.charset = charset;
    }

    /**
     * Write String into buffer
     * @author DerSimi
     * @param str
     */
    public void writeString(String str) {
        final byte[] bytes = str.getBytes(this.charset);

        writeInt(bytes.length);
        writeBytes(bytes);
    }

    /**
     * Read String from buffer
     * @author DerSimi
     * @return String
     */
    public String readString() {
        byte[] bytes = new byte[this.readInt()];
        this.readBytes(bytes);

        return new String(bytes, this.charset);
    }

    /**
     * Read string array from buffer
     * @author DerSimi
     * @return string array
     */
    public String[] readStrings() {
        String[] array = new String[this.readInt()];

        for (int i = 0; i < array.length; i++)
            array[i] = this.readString();

        return array;
    }

    /**
     * Write string array into buffer
     * @author DerSimi
     * @param array
     */
    public void writeStrings(String[] array) {
        this.writeInt(array.length);

        for (String string : array)
            this.writeString(string);
    }

    /**
     * Read unique id from buffer
     * @author DerSimi
     * @return uuid from buffer
     */
    public UUID readUniqueId() {
        return new UUID(this.readLong(), this.readLong());
    }

    /**
     * Write unique id into buffer
     * @author DerSimi
     * @param uniqueId
     */
    public void writeUniqueId(UUID uniqueId) {
        writeLong(uniqueId.getMostSignificantBits());
        writeLong(uniqueId.getLeastSignificantBits());
    }

    /**
     * Returns the number of bytes (octets) this buffer can contain.
     */
    public int capacity() {
        return this.byteBuf.capacity();
    }

    /**
     * Adjusts the capacity of this buffer.  If the {@code newCapacity} is less than the current
     * capacity, the content of this buffer is truncated.  If the {@code newCapacity} is greater
     * than the current capacity, the buffer is appended with unspecified data whose length is
     * {@code (newCapacity - currentCapacity)}.
     *
     * @param newCapacity
     * @throws IllegalArgumentException if the {@code newCapacity} is greater than {@link #maxCapacity()}
     */
    public ByteBuf capacity(int newCapacity) {
        return this.byteBuf.capacity(newCapacity);
    }

    /**
     * Returns the maximum allowed capacity of this buffer. This value provides an upper
     * bound on {@link #capacity()}.
     */
    public int maxCapacity() {
        return this.byteBuf.maxCapacity();
    }

    /**
     * Returns the {@link ByteBufAllocator} which created this buffer.
     */
    public ByteBufAllocator alloc() {
        return this.byteBuf.alloc();
    }

    /**
     * Returns the <a href="http://en.wikipedia.org/wiki/Endianness">endianness</a>
     * of this buffer.
     *
     * @deprecated use the Little Endian accessors, e.g. {@code getShortLE}, {@code getIntLE}
     * instead of creating a buffer with swapped {@code endianness}.
     */
    @Deprecated public ByteOrder order() {
        return this.byteBuf.order();
    }

    /**
     * Returns a buffer with the specified {@code endianness} which shares the whole region,
     * indexes, and marks of this buffer.  Modifying the content, the indexes, or the marks of the
     * returned buffer or this buffer affects each other's content, indexes, and marks.  If the
     * specified {@code endianness} is identical to this buffer's byte order, this method can
     * return {@code this}.  This method does not modify {@code readerIndex} or {@code writerIndex}
     * of this buffer.
     *
     * @param endianness
     * @deprecated use the Little Endian accessors, e.g. {@code getShortLE}, {@code getIntLE}
     * instead of creating a buffer with swapped {@code endianness}.
     */
    @Deprecated public ByteBuf order(ByteOrder endianness) {
        return this.byteBuf.order(endianness);
    }

    /**
     * Return the underlying buffer instance if this buffer is a wrapper of another buffer.
     *
     * @return {@code null} if this buffer is not a wrapper
     */
    public ByteBuf unwrap() {
        return this.byteBuf.unwrap();
    }

    /**
     * Returns {@code true} if and only if this buffer is backed by an
     * NIO direct buffer.
     */
    public boolean isDirect() {
        return this.byteBuf.isDirect();
    }

    /**
     * Returns {@code true} if and only if this buffer is read-only.
     */
    public boolean isReadOnly() {
        return this.byteBuf.isReadOnly();
    }

    /**
     * Returns a read-only version of this buffer.
     */
    public ByteBuf asReadOnly() {
        return this.byteBuf.asReadOnly();
    }

    /**
     * Returns the {@code readerIndex} of this buffer.
     */
    public int readerIndex() {
        return this.byteBuf.readerIndex();
    }

    /**
     * Sets the {@code readerIndex} of this buffer.
     *
     * @param readerIndex
     * @throws IndexOutOfBoundsException if the specified {@code readerIndex} is
     *                                   less than {@code 0} or
     *                                   greater than {@code this.writerIndex}
     */
    public ByteBuf readerIndex(int readerIndex) {
        return this.byteBuf.readerIndex(readerIndex);
    }

    /**
     * Returns the {@code writerIndex} of this buffer.
     */
    public int writerIndex() {
        return this.byteBuf.writerIndex();
    }

    /**
     * Sets the {@code writerIndex} of this buffer.
     *
     * @param writerIndex
     * @throws IndexOutOfBoundsException if the specified {@code writerIndex} is
     *                                   less than {@code this.readerIndex} or
     *                                   greater than {@code this.capacity}
     */
    public ByteBuf writerIndex(int writerIndex) {
        return this.byteBuf.writerIndex(writerIndex);
    }

    /**
     * Sets the {@code readerIndex} and {@code writerIndex} of this buffer
     * in one shot.  This method is useful when you have to worry about the
     * invocation order of {@link #readerIndex(int)} and {@link #writerIndex(int)}
     * methods.  For example, the following code will fail:
     *
     * <pre>
     * // Create a buffer whose readerIndex, writerIndex and capacity are
     * // 0, 0 and 8 respectively.
     *
     * // IndexOutOfBoundsException is thrown because the specified
     * // readerIndex (2) cannot be greater than the current writerIndex (0).
     * buf.readerIndex(2);
     * buf.writerIndex(4);
     * </pre>
     * <p>
     * The following code will also fail:
     *
     * <pre>
     * // Create a buffer whose readerIndex, writerIndex and capacity are
     * // 0, 8 and 8 respectively.
     *
     * // readerIndex becomes 8.
     * buf.readLong();
     *
     * // IndexOutOfBoundsException is thrown because the specified
     * // writerIndex (4) cannot be less than the current readerIndex (8).
     * buf.writerIndex(4);
     * buf.readerIndex(2);
     * </pre>
     * <p>
     * By contrast, this method guarantees that it never
     * throws an {@link IndexOutOfBoundsException} as long as the specified
     * indexes meet basic constraints, regardless what the current index
     * values of the buffer are:
     *
     * <pre>
     * // No matter what the current state of the buffer is, the following
     * // call always succeeds as long as the capacity of the buffer is not
     * // less than 4.
     * buf.setIndex(2, 4);
     * </pre>
     *
     * @param readerIndex
     * @param writerIndex
     * @throws IndexOutOfBoundsException if the specified {@code readerIndex} is less than 0,
     *                                   if the specified {@code writerIndex} is less than the specified
     *                                   {@code readerIndex} or if the specified {@code writerIndex} is
     *                                   greater than {@code this.capacity}
     */
    public ByteBuf setIndex(int readerIndex, int writerIndex) {
        return this.byteBuf.setIndex(readerIndex, writerIndex);
    }

    /**
     * Returns the number of readable bytes which is equal to
     * {@code (this.writerIndex - this.readerIndex)}.
     */
    public int readableBytes() {
        return this.byteBuf.readableBytes();
    }

    /**
     * Returns the number of writable bytes which is equal to
     * {@code (this.capacity - this.writerIndex)}.
     */
    public int writableBytes() {
        return this.byteBuf.writableBytes();
    }

    /**
     * Returns the maximum possible number of writable bytes, which is equal to
     * {@code (this.maxCapacity - this.writerIndex)}.
     */
    public int maxWritableBytes() {
        return this.byteBuf.maxWritableBytes();
    }

    /**
     * Returns {@code true}
     * if and only if {@code (this.writerIndex - this.readerIndex)} is greater
     * than {@code 0}.
     */
    public boolean isReadable() {
        return this.byteBuf.isReadable();
    }

    /**
     * Returns {@code true} if and only if this buffer contains equal to or more than the specified number of elements.
     *
     * @param size
     */
    public boolean isReadable(int size) {
        return this.byteBuf.isReadable(size);
    }

    /**
     * Returns {@code true}
     * if and only if {@code (this.capacity - this.writerIndex)} is greater
     * than {@code 0}.
     */
    public boolean isWritable() {
        return this.byteBuf.isWritable();
    }

    /**
     * Returns {@code true} if and only if this buffer has enough room to allow writing the specified number of
     * elements.
     *
     * @param size
     */
    public boolean isWritable(int size) {
        return this.byteBuf.isWritable(size);
    }

    /**
     * Sets the {@code readerIndex} and {@code writerIndex} of this buffer to
     * {@code 0}.
     * This method is identical to {@link #setIndex(int, int) setIndex(0, 0)}.
     * <p>
     * Please note that the behavior of this method is different
     * from that of NIO buffer, which sets the {@code limit} to
     * the {@code capacity} of the buffer.
     */
    public ByteBuf clear() {
        return this.byteBuf.clear();
    }

    /**
     * Marks the current {@code readerIndex} in this buffer.  You can
     * reposition the current {@code readerIndex} to the marked
     * {@code readerIndex} by calling {@link #resetReaderIndex()}.
     * The initial value of the marked {@code readerIndex} is {@code 0}.
     */
    public ByteBuf markReaderIndex() {
        return this.byteBuf.markReaderIndex();
    }

    /**
     * Repositions the current {@code readerIndex} to the marked
     * {@code readerIndex} in this buffer.
     *
     * @throws IndexOutOfBoundsException if the current {@code writerIndex} is less than the marked
     *                                   {@code readerIndex}
     */
    public ByteBuf resetReaderIndex() {
        return this.byteBuf.resetReaderIndex();
    }

    /**
     * Marks the current {@code writerIndex} in this buffer.  You can
     * reposition the current {@code writerIndex} to the marked
     * {@code writerIndex} by calling {@link #resetWriterIndex()}.
     * The initial value of the marked {@code writerIndex} is {@code 0}.
     */
    public ByteBuf markWriterIndex() {
        return this.byteBuf.markWriterIndex();
    }

    /**
     * Repositions the current {@code writerIndex} to the marked
     * {@code writerIndex} in this buffer.
     *
     * @throws IndexOutOfBoundsException if the current {@code readerIndex} is greater than the marked
     *                                   {@code writerIndex}
     */
    public ByteBuf resetWriterIndex() {
        return this.byteBuf.resetWriterIndex();
    }

    /**
     * Discards the bytes between the 0th index and {@code readerIndex}.
     * It moves the bytes between {@code readerIndex} and {@code writerIndex}
     * to the 0th index, and sets {@code readerIndex} and {@code writerIndex}
     * to {@code 0} and {@code oldWriterIndex - oldReaderIndex} respectively.
     * <p>
     * Please refer to the class documentation for more detailed explanation.
     */
    public ByteBuf discardReadBytes() {
        return this.byteBuf.discardReadBytes();
    }

    /**
     * Similar to {@link ByteBuf#discardReadBytes()} except that this method might discard
     * some, all, or none of read bytes depending on its internal implementation to reduce
     * overall memory bandwidth consumption at the cost of potentially additional memory
     * consumption.
     */
    public ByteBuf discardSomeReadBytes() {
        return this.byteBuf.discardSomeReadBytes();
    }

    /**
     * Expands the buffer {@link #capacity()} to make sure the number of
     * {@linkplain #writableBytes() writable bytes} is equal to or greater than the
     * specified value.  If there are enough writable bytes in this buffer, this method
     * returns with no side effect.
     *
     * @param minWritableBytes the expected minimum number of writable bytes
     * @throws IndexOutOfBoundsException if {@link #writerIndex()} + {@code minWritableBytes} &gt; {@link #maxCapacity()}.
     * @see #capacity(int)
     */
    public ByteBuf ensureWritable(int minWritableBytes) {
        return this.byteBuf.ensureWritable(minWritableBytes);
    }

    /**
     * Expands the buffer {@link #capacity()} to make sure the number of
     * {@linkplain #writableBytes() writable bytes} is equal to or greater than the
     * specified value. Unlike {@link #ensureWritable(int)}, this method returns a status code.
     *
     * @param minWritableBytes the expected minimum number of writable bytes
     * @param force            When {@link #writerIndex()} + {@code minWritableBytes} &gt; {@link #maxCapacity()}:
     *                         <ul>
     *                         <li>{@code true} - the capacity of the buffer is expanded to {@link #maxCapacity()}</li>
     *                         <li>{@code false} - the capacity of the buffer is unchanged</li>
     *                         </ul>
     * @return {@code 0} if the buffer has enough writable bytes, and its capacity is unchanged.
     * {@code 1} if the buffer does not have enough bytes, and its capacity is unchanged.
     * {@code 2} if the buffer has enough writable bytes, and its capacity has been increased.
     * {@code 3} if the buffer does not have enough bytes, but its capacity has been
     * increased to its maximum.
     */
    public int ensureWritable(int minWritableBytes, boolean force) {
        return this.byteBuf.ensureWritable(minWritableBytes, force);
    }

    /**
     * Gets a boolean at the specified absolute (@code index) in this buffer.
     * This method does not modify the {@code readerIndex} or {@code writerIndex}
     * of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 1} is greater than {@code this.capacity}
     */
    public boolean getBoolean(int index) {
        return this.byteBuf.getBoolean(index);
    }

    /**
     * Gets a byte at the specified absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 1} is greater than {@code this.capacity}
     */
    public byte getByte(int index) {
        return this.byteBuf.getByte(index);
    }

    /**
     * Gets an unsigned byte at the specified absolute {@code index} in this
     * buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 1} is greater than {@code this.capacity}
     */
    public short getUnsignedByte(int index) {
        return this.byteBuf.getUnsignedByte(index);
    }

    /**
     * Gets a 16-bit short integer at the specified absolute {@code index} in
     * this buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public short getShort(int index) {
        return this.byteBuf.getShort(index);
    }

    /**
     * Gets a 16-bit short integer at the specified absolute {@code index} in
     * this buffer in Little Endian Byte Order. This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public short getShortLE(int index) {
        return this.byteBuf.getShortLE(index);
    }

    /**
     * Gets an unsigned 16-bit short integer at the specified absolute
     * {@code index} in this buffer.  This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public int getUnsignedShort(int index) {
        return this.byteBuf.getUnsignedShort(index);
    }

    /**
     * Gets an unsigned 16-bit short integer at the specified absolute
     * {@code index} in this buffer in Little Endian Byte Order.
     * This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public int getUnsignedShortLE(int index) {
        return this.byteBuf.getUnsignedShortLE(index);
    }

    /**
     * Gets a 24-bit medium integer at the specified absolute {@code index} in
     * this buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public int getMedium(int index) {
        return this.byteBuf.getMedium(index);
    }

    /**
     * Gets a 24-bit medium integer at the specified absolute {@code index} in
     * this buffer in the Little Endian Byte Order. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public int getMediumLE(int index) {
        return this.byteBuf.getMediumLE(index);
    }

    /**
     * Gets an unsigned 24-bit medium integer at the specified absolute
     * {@code index} in this buffer.  This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public int getUnsignedMedium(int index) {
        return this.byteBuf.getUnsignedMedium(index);
    }

    /**
     * Gets an unsigned 24-bit medium integer at the specified absolute
     * {@code index} in this buffer in Little Endian Byte Order.
     * This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public int getUnsignedMediumLE(int index) {
        return this.byteBuf.getUnsignedMediumLE(index);
    }

    /**
     * Gets a 32-bit integer at the specified absolute {@code index} in
     * this buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public int getInt(int index) {
        return this.byteBuf.getInt(index);
    }

    /**
     * Gets a 32-bit integer at the specified absolute {@code index} in
     * this buffer with Little Endian Byte Order. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public int getIntLE(int index) {
        return this.byteBuf.getIntLE(index);
    }

    /**
     * Gets an unsigned 32-bit integer at the specified absolute {@code index}
     * in this buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public long getUnsignedInt(int index) {
        return this.byteBuf.getUnsignedInt(index);
    }

    /**
     * Gets an unsigned 32-bit integer at the specified absolute {@code index}
     * in this buffer in Little Endian Byte Order. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public long getUnsignedIntLE(int index) {
        return this.byteBuf.getUnsignedIntLE(index);
    }

    /**
     * Gets a 64-bit long integer at the specified absolute {@code index} in
     * this buffer.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public long getLong(int index) {
        return this.byteBuf.getLong(index);
    }

    /**
     * Gets a 64-bit long integer at the specified absolute {@code index} in
     * this buffer in Little Endian Byte Order. This method does not
     * modify {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public long getLongLE(int index) {
        return this.byteBuf.getLongLE(index);
    }

    /**
     * Gets a 2-byte UTF-16 character at the specified absolute
     * {@code index} in this buffer.  This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public char getChar(int index) {
        return this.byteBuf.getChar(index);
    }

    /**
     * Gets a 32-bit floating point number at the specified absolute
     * {@code index} in this buffer.  This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public float getFloat(int index) {
        return this.byteBuf.getFloat(index);
    }

    /**
     * Gets a 64-bit floating point number at the specified absolute
     * {@code index} in this buffer.  This method does not modify
     * {@code readerIndex} or {@code writerIndex} of this buffer.
     *
     * @param index
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public double getDouble(int index) {
        return this.byteBuf.getDouble(index);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index} until the destination becomes
     * non-writable.  This method is basically same with
     * {@link #getBytes(int, ByteBuf, int, int)}, except that this
     * method increases the {@code writerIndex} of the destination by the
     * number of the transferred bytes while
     * {@link #getBytes(int, ByteBuf, int, int)} does not.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * the source buffer (i.e. {@code this}).
     *
     * @param index
     * @param dst
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + dst.writableBytes} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf getBytes(int index, ByteBuf dst) {
        return this.byteBuf.getBytes(index, dst);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index}.  This method is basically same
     * with {@link #getBytes(int, ByteBuf, int, int)}, except that this
     * method increases the {@code writerIndex} of the destination by the
     * number of the transferred bytes while
     * {@link #getBytes(int, ByteBuf, int, int)} does not.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * the source buffer (i.e. {@code this}).
     *
     * @param index
     * @param dst
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code length} is greater than {@code dst.writableBytes}
     */
    public ByteBuf getBytes(int index, ByteBuf dst, int length) {
        return this.byteBuf.getBytes(index, dst, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex}
     * of both the source (i.e. {@code this}) and the destination.
     *
     * @param index
     * @param dst
     * @param dstIndex the first index of the destination
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if the specified {@code dstIndex} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code dstIndex + length} is greater than
     *                                   {@code dst.capacity}
     */
    public ByteBuf getBytes(int index, ByteBuf dst, int dstIndex, int length) {
        return this.byteBuf.getBytes(index, dst, dstIndex, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer
     *
     * @param index
     * @param dst
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + dst.length} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf getBytes(int index, byte[] dst) {
        return this.byteBuf.getBytes(index, dst);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex}
     * of this buffer.
     *
     * @param index
     * @param dst
     * @param dstIndex the first index of the destination
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if the specified {@code dstIndex} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code dstIndex + length} is greater than
     *                                   {@code dst.length}
     */
    public ByteBuf getBytes(int index, byte[] dst, int dstIndex, int length) {
        return this.byteBuf.getBytes(index, dst, dstIndex, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the specified absolute {@code index} until the destination's position
     * reaches its limit.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer while the destination's {@code position} will be increased.
     *
     * @param index
     * @param dst
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + dst.remaining()} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf getBytes(int index, ByteBuffer dst) {
        return this.byteBuf.getBytes(index, dst);
    }

    /**
     * Transfers this buffer's data to the specified stream starting at the
     * specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param out
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}
     * @throws IOException               if the specified stream threw an exception during I/O
     */
    public ByteBuf getBytes(int index, OutputStream out, int length) throws IOException {
        return this.byteBuf.getBytes(index, out, length);
    }

    /**
     * Transfers this buffer's data to the specified channel starting at the
     * specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param out
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int getBytes(int index, GatheringByteChannel out, int length) throws IOException {
        return this.byteBuf.getBytes(index, out, length);
    }

    /**
     * Transfers this buffer's data starting at the specified absolute {@code index}
     * to the specified channel starting at the given file position.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer. This method does not modify the channel's position.
     *
     * @param index
     * @param out
     * @param position the file position at which the transfer is to begin
     * @param length   the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int getBytes(int index, FileChannel out, long position, int length) throws IOException {
        return this.byteBuf.getBytes(index, out, position, length);
    }

    /**
     * Gets a {@link CharSequence} with the given length at the given index.
     *
     * @param index
     * @param length  the length to read
     * @param charset that should be used
     * @return the sequence
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public CharSequence getCharSequence(int index, int length, Charset charset) {
        return this.byteBuf.getCharSequence(index, length, charset);
    }

    /**
     * Sets the specified boolean at the specified absolute {@code index} in this
     * buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 1} is greater than {@code this.capacity}
     */
    public ByteBuf setBoolean(int index, boolean value) {
        return this.byteBuf.setBoolean(index, value);
    }

    /**
     * Sets the specified byte at the specified absolute {@code index} in this
     * buffer.  The 24 high-order bits of the specified value are ignored.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 1} is greater than {@code this.capacity}
     */
    public ByteBuf setByte(int index, int value) {
        return this.byteBuf.setByte(index, value);
    }

    /**
     * Sets the specified 16-bit short integer at the specified absolute
     * {@code index} in this buffer.  The 16 high-order bits of the specified
     * value are ignored.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public ByteBuf setShort(int index, int value) {
        return this.byteBuf.setShort(index, value);
    }

    /**
     * Sets the specified 16-bit short integer at the specified absolute
     * {@code index} in this buffer with the Little Endian Byte Order.
     * The 16 high-order bits of the specified value are ignored.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public ByteBuf setShortLE(int index, int value) {
        return this.byteBuf.setShortLE(index, value);
    }

    /**
     * Sets the specified 24-bit medium integer at the specified absolute
     * {@code index} in this buffer.  Please note that the most significant
     * byte is ignored in the specified value.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public ByteBuf setMedium(int index, int value) {
        return this.byteBuf.setMedium(index, value);
    }

    /**
     * Sets the specified 24-bit medium integer at the specified absolute
     * {@code index} in this buffer in the Little Endian Byte Order.
     * Please note that the most significant byte is ignored in the
     * specified value.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 3} is greater than {@code this.capacity}
     */
    public ByteBuf setMediumLE(int index, int value) {
        return this.byteBuf.setMediumLE(index, value);
    }

    /**
     * Sets the specified 32-bit integer at the specified absolute
     * {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public ByteBuf setInt(int index, int value) {
        return this.byteBuf.setInt(index, value);
    }

    /**
     * Sets the specified 32-bit integer at the specified absolute
     * {@code index} in this buffer with Little Endian byte order
     * .
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public ByteBuf setIntLE(int index, int value) {
        return this.byteBuf.setIntLE(index, value);
    }

    /**
     * Sets the specified 64-bit long integer at the specified absolute
     * {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public ByteBuf setLong(int index, long value) {
        return this.byteBuf.setLong(index, value);
    }

    /**
     * Sets the specified 64-bit long integer at the specified absolute
     * {@code index} in this buffer in Little Endian Byte Order.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public ByteBuf setLongLE(int index, long value) {
        return this.byteBuf.setLongLE(index, value);
    }

    /**
     * Sets the specified 2-byte UTF-16 character at the specified absolute
     * {@code index} in this buffer.
     * The 16 high-order bits of the specified value are ignored.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 2} is greater than {@code this.capacity}
     */
    public ByteBuf setChar(int index, int value) {
        return this.byteBuf.setChar(index, value);
    }

    /**
     * Sets the specified 32-bit floating-point number at the specified
     * absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 4} is greater than {@code this.capacity}
     */
    public ByteBuf setFloat(int index, float value) {
        return this.byteBuf.setFloat(index, value);
    }

    /**
     * Sets the specified 64-bit floating-point number at the specified
     * absolute {@code index} in this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param value
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   {@code index + 8} is greater than {@code this.capacity}
     */
    public ByteBuf setDouble(int index, double value) {
        return this.byteBuf.setDouble(index, value);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the specified absolute {@code index} until the source buffer becomes
     * unreadable.  This method is basically same with
     * {@link #setBytes(int, ByteBuf, int, int)}, except that this
     * method increases the {@code readerIndex} of the source buffer by
     * the number of the transferred bytes while
     * {@link #setBytes(int, ByteBuf, int, int)} does not.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * the source buffer (i.e. {@code this}).
     *
     * @param index
     * @param src
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + src.readableBytes} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf setBytes(int index, ByteBuf src) {
        return this.byteBuf.setBytes(index, src);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the specified absolute {@code index}.  This method is basically same
     * with {@link #setBytes(int, ByteBuf, int, int)}, except that this
     * method increases the {@code readerIndex} of the source buffer by
     * the number of the transferred bytes while
     * {@link #setBytes(int, ByteBuf, int, int)} does not.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * the source buffer (i.e. {@code this}).
     *
     * @param index
     * @param src
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code length} is greater than {@code src.readableBytes}
     */
    public ByteBuf setBytes(int index, ByteBuf src, int length) {
        return this.byteBuf.setBytes(index, src, length);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex}
     * of both the source (i.e. {@code this}) and the destination.
     *
     * @param index
     * @param src
     * @param srcIndex the first index of the source
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if the specified {@code srcIndex} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code srcIndex + length} is greater than
     *                                   {@code src.capacity}
     */
    public ByteBuf setBytes(int index, ByteBuf src, int srcIndex, int length) {
        return this.byteBuf.setBytes(index, src, srcIndex, length);
    }

    /**
     * Transfers the specified source array's data to this buffer starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param src
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + src.length} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf setBytes(int index, byte[] src) {
        return this.byteBuf.setBytes(index, src);
    }

    /**
     * Transfers the specified source array's data to this buffer starting at
     * the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param src
     * @param srcIndex
     * @param length
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0},
     *                                   if the specified {@code srcIndex} is less than {@code 0},
     *                                   if {@code index + length} is greater than
     *                                   {@code this.capacity}, or
     *                                   if {@code srcIndex + length} is greater than {@code src.length}
     */
    public ByteBuf setBytes(int index, byte[] src, int srcIndex, int length) {
        return this.byteBuf.setBytes(index, src, srcIndex, length);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the specified absolute {@code index} until the source buffer's position
     * reaches its limit.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param src
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + src.remaining()} is greater than
     *                                   {@code this.capacity}
     */
    public ByteBuf setBytes(int index, ByteBuffer src) {
        return this.byteBuf.setBytes(index, src);
    }

    /**
     * Transfers the content of the specified source stream to this buffer
     * starting at the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param in
     * @param length the number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel.
     * {@code -1} if the specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than {@code this.capacity}
     * @throws IOException               if the specified stream threw an exception during I/O
     */
    public int setBytes(int index, InputStream in, int length) throws IOException {
        return this.byteBuf.setBytes(index, in, length);
    }

    /**
     * Transfers the content of the specified source channel to this buffer
     * starting at the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param in
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel.
     * {@code -1} if the specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than {@code this.capacity}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int setBytes(int index, ScatteringByteChannel in, int length) throws IOException {
        return this.byteBuf.setBytes(index, in, length);
    }

    /**
     * Transfers the content of the specified source channel starting at the given file position
     * to this buffer starting at the specified absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer. This method does not modify the channel's position.
     *
     * @param index
     * @param in
     * @param position the file position at which the transfer is to begin
     * @param length   the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel.
     * {@code -1} if the specified channel is closed.
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than {@code this.capacity}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int setBytes(int index, FileChannel in, long position, int length) throws IOException {
        return this.byteBuf.setBytes(index, in, position, length);
    }

    /**
     * Fills this buffer with <tt>NUL (0x00)</tt> starting at the specified
     * absolute {@code index}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param length the number of <tt>NUL</tt>s to write to the buffer
     * @throws IndexOutOfBoundsException if the specified {@code index} is less than {@code 0} or
     *                                   if {@code index + length} is greater than {@code this.capacity}
     */
    public ByteBuf setZero(int index, int length) {
        return this.byteBuf.setZero(index, length);
    }

    /**
     * Writes the specified {@link CharSequence} at the current {@code writerIndex} and increases
     * the {@code writerIndex} by the written bytes.
     *
     * @param index    on which the sequence should be written
     * @param sequence to write
     * @param charset  that should be used.
     * @return the written number of bytes.
     * @throws IndexOutOfBoundsException if {@code this.writableBytes} is not large enough to write the whole sequence
     */
    public int setCharSequence(int index, CharSequence sequence, Charset charset) {
        return this.byteBuf.setCharSequence(index, sequence, charset);
    }

    /**
     * Gets a boolean at the current {@code readerIndex} and increases
     * the {@code readerIndex} by {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public boolean readBoolean() {
        return this.byteBuf.readBoolean();
    }

    /**
     * Gets a byte at the current {@code readerIndex} and increases
     * the {@code readerIndex} by {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public byte readByte() {
        return this.byteBuf.readByte();
    }

    /**
     * Gets an unsigned byte at the current {@code readerIndex} and increases
     * the {@code readerIndex} by {@code 1} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 1}
     */
    public short readUnsignedByte() {
        return this.byteBuf.readUnsignedByte();
    }

    /**
     * Gets a 16-bit short integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public short readShort() {
        return this.byteBuf.readShort();
    }

    /**
     * Gets a 16-bit short integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public short readShortLE() {
        return this.byteBuf.readShortLE();
    }

    /**
     * Gets an unsigned 16-bit short integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public int readUnsignedShort() {
        return this.byteBuf.readUnsignedShort();
    }

    /**
     * Gets an unsigned 16-bit short integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public int readUnsignedShortLE() {
        return this.byteBuf.readUnsignedShortLE();
    }

    /**
     * Gets a 24-bit medium integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public int readMedium() {
        return this.byteBuf.readMedium();
    }

    /**
     * Gets a 24-bit medium integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the
     * {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public int readMediumLE() {
        return this.byteBuf.readMediumLE();
    }

    /**
     * Gets an unsigned 24-bit medium integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public int readUnsignedMedium() {
        return this.byteBuf.readUnsignedMedium();
    }

    /**
     * Gets an unsigned 24-bit medium integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 3} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 3}
     */
    public int readUnsignedMediumLE() {
        return this.byteBuf.readUnsignedMediumLE();
    }

    /**
     * Gets a 32-bit integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public int readInt() {
        return this.byteBuf.readInt();
    }

    /**
     * Gets a 32-bit integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public int readIntLE() {
        return this.byteBuf.readIntLE();
    }

    /**
     * Gets an unsigned 32-bit integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public long readUnsignedInt() {
        return this.byteBuf.readUnsignedInt();
    }

    /**
     * Gets an unsigned 32-bit integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public long readUnsignedIntLE() {
        return this.byteBuf.readUnsignedIntLE();
    }

    /**
     * Gets a 64-bit integer at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public long readLong() {
        return this.byteBuf.readLong();
    }

    /**
     * Gets a 64-bit integer at the current {@code readerIndex}
     * in the Little Endian Byte Order and increases the {@code readerIndex}
     * by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public long readLongLE() {
        return this.byteBuf.readLongLE();
    }

    /**
     * Gets a 2-byte UTF-16 character at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 2} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 2}
     */
    public char readChar() {
        return this.byteBuf.readChar();
    }

    /**
     * Gets a 32-bit floating point number at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 4} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 4}
     */
    public float readFloat() {
        return this.byteBuf.readFloat();
    }

    /**
     * Gets a 64-bit floating point number at the current {@code readerIndex}
     * and increases the {@code readerIndex} by {@code 8} in this buffer.
     *
     * @throws IndexOutOfBoundsException if {@code this.readableBytes} is less than {@code 8}
     */
    public double readDouble() {
        return this.byteBuf.readDouble();
    }

    /**
     * Transfers this buffer's data to a newly created buffer starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code length}).
     * The returned buffer's {@code readerIndex} and {@code writerIndex} are
     * {@code 0} and {@code length} respectively.
     *
     * @param length the number of bytes to transfer
     * @return the newly created buffer which contains the transferred bytes
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public ByteBuf readBytes(int length) {
        return this.byteBuf.readBytes(length);
    }

    /**
     * Returns a new slice of this buffer's sub-region starting at the current
     * {@code readerIndex} and increases the {@code readerIndex} by the size
     * of the new slice (= {@code length}).
     * <p>
     * Also be aware that this method will NOT call {@link #retain()} and so the
     * reference count will NOT be increased.
     *
     * @param length the size of the new slice
     * @return the newly created slice
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public ByteBuf readSlice(int length) {
        return this.byteBuf.readSlice(length);
    }

    /**
     * Returns a new retained slice of this buffer's sub-region starting at the current
     * {@code readerIndex} and increases the {@code readerIndex} by the size
     * of the new slice (= {@code length}).
     * <p>
     * Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link #readSlice(int)}.
     * This method behaves similarly to {@code readSlice(...).retain()} except that this method may return
     * a buffer implementation that produces less garbage.
     *
     * @param length the size of the new slice
     * @return the newly created slice
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public ByteBuf readRetainedSlice(int length) {
        return this.byteBuf.readRetainedSlice(length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} until the destination becomes
     * non-writable, and increases the {@code readerIndex} by the number of the
     * transferred bytes.  This method is basically same with
     * {@link #readBytes(ByteBuf, int, int)}, except that this method
     * increases the {@code writerIndex} of the destination by the number of
     * the transferred bytes while {@link #readBytes(ByteBuf, int, int)}
     * does not.
     *
     * @param dst
     * @throws IndexOutOfBoundsException if {@code dst.writableBytes} is greater than
     *                                   {@code this.readableBytes}
     */
    public ByteBuf readBytes(ByteBuf dst) {
        return this.byteBuf.readBytes(dst);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code length}).  This method
     * is basically same with {@link #readBytes(ByteBuf, int, int)},
     * except that this method increases the {@code writerIndex} of the
     * destination by the number of the transferred bytes (= {@code length})
     * while {@link #readBytes(ByteBuf, int, int)} does not.
     *
     * @param dst
     * @param length
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes} or
     *                                   if {@code length} is greater than {@code dst.writableBytes}
     */
    public ByteBuf readBytes(ByteBuf dst, int length) {
        return this.byteBuf.readBytes(dst, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code length}).
     *
     * @param dst
     * @param dstIndex the first index of the destination
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0},
     *                                   if {@code length} is greater than {@code this.readableBytes}, or
     *                                   if {@code dstIndex + length} is greater than
     *                                   {@code dst.capacity}
     */
    public ByteBuf readBytes(ByteBuf dst, int dstIndex, int length) {
        return this.byteBuf.readBytes(dst, dstIndex, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code dst.length}).
     *
     * @param dst
     * @throws IndexOutOfBoundsException if {@code dst.length} is greater than {@code this.readableBytes}
     */
    public ByteBuf readBytes(byte[] dst) {
        return this.byteBuf.readBytes(dst);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code length}).
     *
     * @param dst
     * @param dstIndex the first index of the destination
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code dstIndex} is less than {@code 0},
     *                                   if {@code length} is greater than {@code this.readableBytes}, or
     *                                   if {@code dstIndex + length} is greater than {@code dst.length}
     */
    public ByteBuf readBytes(byte[] dst, int dstIndex, int length) {
        return this.byteBuf.readBytes(dst, dstIndex, length);
    }

    /**
     * Transfers this buffer's data to the specified destination starting at
     * the current {@code readerIndex} until the destination's position
     * reaches its limit, and increases the {@code readerIndex} by the
     * number of the transferred bytes.
     *
     * @param dst
     * @throws IndexOutOfBoundsException if {@code dst.remaining()} is greater than
     *                                   {@code this.readableBytes}
     */
    public ByteBuf readBytes(ByteBuffer dst) {
        return this.byteBuf.readBytes(dst);
    }

    /**
     * Transfers this buffer's data to the specified stream starting at the
     * current {@code readerIndex}.
     *
     * @param out
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     * @throws IOException               if the specified stream threw an exception during I/O
     */
    public ByteBuf readBytes(OutputStream out, int length) throws IOException {
        return this.byteBuf.readBytes(out, length);
    }

    /**
     * Transfers this buffer's data to the specified stream starting at the
     * current {@code readerIndex}.
     *
     * @param out
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int readBytes(GatheringByteChannel out, int length) throws IOException {
        return this.byteBuf.readBytes(out, length);
    }

    /**
     * Gets a {@link CharSequence} with the given length at the current {@code readerIndex}
     * and increases the {@code readerIndex} by the given length.
     *
     * @param length  the length to read
     * @param charset that should be used
     * @return the sequence
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public CharSequence readCharSequence(int length, Charset charset) {
        return this.byteBuf.readCharSequence(length, charset);
    }

    /**
     * Transfers this buffer's data starting at the current {@code readerIndex}
     * to the specified channel starting at the given file position.
     * This method does not modify the channel's position.
     *
     * @param out
     * @param position the file position at which the transfer is to begin
     * @param length   the maximum number of bytes to transfer
     * @return the actual number of bytes written out to the specified channel
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     * @throws IOException               if the specified channel threw an exception during I/O
     */
    public int readBytes(FileChannel out, long position, int length) throws IOException {
        return this.byteBuf.readBytes(out, position, length);
    }

    /**
     * Increases the current {@code readerIndex} by the specified
     * {@code length} in this buffer.
     *
     * @param length
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public ByteBuf skipBytes(int length) {
        return this.byteBuf.skipBytes(length);
    }

    /**
     * Sets the specified boolean at the current {@code writerIndex}
     * and increases the {@code writerIndex} by {@code 1} in this buffer.
     * If {@code this.writableBytes} is less than {@code 1}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeBoolean(boolean value) {
        return this.byteBuf.writeBoolean(value);
    }

    /**
     * Sets the specified byte at the current {@code writerIndex}
     * and increases the {@code writerIndex} by {@code 1} in this buffer.
     * The 24 high-order bits of the specified value are ignored.
     * If {@code this.writableBytes} is less than {@code 1}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeByte(int value) {
        return this.byteBuf.writeByte(value);
    }

    /**
     * Sets the specified 16-bit short integer at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 2}
     * in this buffer.  The 16 high-order bits of the specified value are ignored.
     * If {@code this.writableBytes} is less than {@code 2}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeShort(int value) {
        return this.byteBuf.writeShort(value);
    }

    /**
     * Sets the specified 16-bit short integer in the Little Endian Byte
     * Order at the current {@code writerIndex} and increases the
     * {@code writerIndex} by {@code 2} in this buffer.
     * The 16 high-order bits of the specified value are ignored.
     * If {@code this.writableBytes} is less than {@code 2}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeShortLE(int value) {
        return this.byteBuf.writeShortLE(value);
    }

    /**
     * Sets the specified 24-bit medium integer at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 3}
     * in this buffer.
     * If {@code this.writableBytes} is less than {@code 3}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeMedium(int value) {
        return this.byteBuf.writeMedium(value);
    }

    /**
     * Sets the specified 24-bit medium integer at the current
     * {@code writerIndex} in the Little Endian Byte Order and
     * increases the {@code writerIndex} by {@code 3} in this
     * buffer.
     * If {@code this.writableBytes} is less than {@code 3}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeMediumLE(int value) {
        return this.byteBuf.writeMediumLE(value);
    }

    /**
     * Sets the specified 32-bit integer at the current {@code writerIndex}
     * and increases the {@code writerIndex} by {@code 4} in this buffer.
     * If {@code this.writableBytes} is less than {@code 4}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeInt(int value) {
        return this.byteBuf.writeInt(value);
    }

    /**
     * Sets the specified 32-bit integer at the current {@code writerIndex}
     * in the Little Endian Byte Order and increases the {@code writerIndex}
     * by {@code 4} in this buffer.
     * If {@code this.writableBytes} is less than {@code 4}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeIntLE(int value) {
        return this.byteBuf.writeIntLE(value);
    }

    /**
     * Sets the specified 64-bit long integer at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 8}
     * in this buffer.
     * If {@code this.writableBytes} is less than {@code 8}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeLong(long value) {
        return this.byteBuf.writeLong(value);
    }

    /**
     * Sets the specified 64-bit long integer at the current
     * {@code writerIndex} in the Little Endian Byte Order and
     * increases the {@code writerIndex} by {@code 8}
     * in this buffer.
     * If {@code this.writableBytes} is less than {@code 8}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeLongLE(long value) {
        return this.byteBuf.writeLongLE(value);
    }

    /**
     * Sets the specified 2-byte UTF-16 character at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 2}
     * in this buffer.  The 16 high-order bits of the specified value are ignored.
     * If {@code this.writableBytes} is less than {@code 2}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeChar(int value) {
        return this.byteBuf.writeChar(value);
    }

    /**
     * Sets the specified 32-bit floating point number at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 4}
     * in this buffer.
     * If {@code this.writableBytes} is less than {@code 4}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeFloat(float value) {
        return this.byteBuf.writeFloat(value);
    }

    /**
     * Sets the specified 64-bit floating point number at the current
     * {@code writerIndex} and increases the {@code writerIndex} by {@code 8}
     * in this buffer.
     * If {@code this.writableBytes} is less than {@code 8}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param value
     */
    public ByteBuf writeDouble(double value) {
        return this.byteBuf.writeDouble(value);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the current {@code writerIndex} until the source buffer becomes
     * unreadable, and increases the {@code writerIndex} by the number of
     * the transferred bytes.  This method is basically same with
     * {@link #writeBytes(ByteBuf, int, int)}, except that this method
     * increases the {@code readerIndex} of the source buffer by the number of
     * the transferred bytes while {@link #writeBytes(ByteBuf, int, int)}
     * does not.
     * If {@code this.writableBytes} is less than {@code src.readableBytes},
     * {@link #ensureWritable(int)} will be called in an attempt to expand
     * capacity to accommodate.
     *
     * @param src
     */
    public ByteBuf writeBytes(ByteBuf src) {
        return this.byteBuf.writeBytes(src);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the current {@code writerIndex} and increases the {@code writerIndex}
     * by the number of the transferred bytes (= {@code length}).  This method
     * is basically same with {@link #writeBytes(ByteBuf, int, int)},
     * except that this method increases the {@code readerIndex} of the source
     * buffer by the number of the transferred bytes (= {@code length}) while
     * {@link #writeBytes(ByteBuf, int, int)} does not.
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param src
     * @param length the number of bytes to transfer
     * @throws IndexOutOfBoundsException if {@code length} is greater then {@code src.readableBytes}
     */
    public ByteBuf writeBytes(ByteBuf src, int length) {
        return this.byteBuf.writeBytes(src, length);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the current {@code writerIndex} and increases the {@code writerIndex}
     * by the number of the transferred bytes (= {@code length}).
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param src
     * @param srcIndex the first index of the source
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0}, or
     *                                   if {@code srcIndex + length} is greater than {@code src.capacity}
     */
    public ByteBuf writeBytes(ByteBuf src, int srcIndex, int length) {
        return this.byteBuf.writeBytes(src, srcIndex, length);
    }

    /**
     * Transfers the specified source array's data to this buffer starting at
     * the current {@code writerIndex} and increases the {@code writerIndex}
     * by the number of the transferred bytes (= {@code src.length}).
     * If {@code this.writableBytes} is less than {@code src.length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param src
     */
    public ByteBuf writeBytes(byte[] src) {
        return this.byteBuf.writeBytes(src);
    }

    /**
     * Transfers the specified source array's data to this buffer starting at
     * the current {@code writerIndex} and increases the {@code writerIndex}
     * by the number of the transferred bytes (= {@code length}).
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param src
     * @param srcIndex the first index of the source
     * @param length   the number of bytes to transfer
     * @throws IndexOutOfBoundsException if the specified {@code srcIndex} is less than {@code 0}, or
     *                                   if {@code srcIndex + length} is greater than {@code src.length}
     */
    public ByteBuf writeBytes(byte[] src, int srcIndex, int length) {
        return this.byteBuf.writeBytes(src, srcIndex, length);
    }

    /**
     * Transfers the specified source buffer's data to this buffer starting at
     * the current {@code writerIndex} until the source buffer's position
     * reaches its limit, and increases the {@code writerIndex} by the
     * number of the transferred bytes.
     * If {@code this.writableBytes} is less than {@code src.remaining()},
     * {@link #ensureWritable(int)} will be called in an attempt to expand
     * capacity to accommodate.
     *
     * @param src
     */
    public ByteBuf writeBytes(ByteBuffer src) {
        return this.byteBuf.writeBytes(src);
    }

    /**
     * Transfers the content of the specified stream to this buffer
     * starting at the current {@code writerIndex} and increases the
     * {@code writerIndex} by the number of the transferred bytes.
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param in
     * @param length the number of bytes to transfer
     * @return the actual number of bytes read in from the specified stream
     * @throws IOException if the specified stream threw an exception during I/O
     */
    public int writeBytes(InputStream in, int length) throws IOException {
        return this.byteBuf.writeBytes(in, length);
    }

    /**
     * Transfers the content of the specified channel to this buffer
     * starting at the current {@code writerIndex} and increases the
     * {@code writerIndex} by the number of the transferred bytes.
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param in
     * @param length the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public int writeBytes(ScatteringByteChannel in, int length) throws IOException {
        return this.byteBuf.writeBytes(in, length);
    }

    /**
     * Transfers the content of the specified channel starting at the given file position
     * to this buffer starting at the current {@code writerIndex} and increases the
     * {@code writerIndex} by the number of the transferred bytes.
     * This method does not modify the channel's position.
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param in
     * @param position the file position at which the transfer is to begin
     * @param length   the maximum number of bytes to transfer
     * @return the actual number of bytes read in from the specified channel
     * @throws IOException if the specified channel threw an exception during I/O
     */
    public int writeBytes(FileChannel in, long position, int length) throws IOException {
        return this.byteBuf.writeBytes(in, position, length);
    }

    /**
     * Fills this buffer with <tt>NUL (0x00)</tt> starting at the current
     * {@code writerIndex} and increases the {@code writerIndex} by the
     * specified {@code length}.
     * If {@code this.writableBytes} is less than {@code length}, {@link #ensureWritable(int)}
     * will be called in an attempt to expand capacity to accommodate.
     *
     * @param length the number of <tt>NUL</tt>s to write to the buffer
     */
    public ByteBuf writeZero(int length) {
        return this.byteBuf.writeZero(length);
    }

    /**
     * Writes the specified {@link CharSequence} at the current {@code writerIndex} and increases
     * the {@code writerIndex} by the written bytes.
     * in this buffer.
     * If {@code this.writableBytes} is not large enough to write the whole sequence,
     * {@link #ensureWritable(int)} will be called in an attempt to expand capacity to accommodate.
     *
     * @param sequence to write
     * @param charset  that should be used
     * @return the written number of bytes
     */
    public int writeCharSequence(CharSequence sequence, Charset charset) {
        return this.byteBuf.writeCharSequence(sequence, charset);
    }

    /**
     * Locates the first occurrence of the specified {@code value} in this
     * buffer. The search takes place from the specified {@code fromIndex}
     * (inclusive) to the specified {@code toIndex} (exclusive).
     * <p>
     * If {@code fromIndex} is greater than {@code toIndex}, the search is
     * performed in a reversed order from {@code fromIndex} (exclusive)
     * down to {@code toIndex} (inclusive).
     * <p>
     * Note that the lower index is always included and higher always excluded.
     * <p>
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param fromIndex
     * @param toIndex
     * @param value
     * @return the absolute index of the first occurrence if found.
     * {@code -1} otherwise.
     */
    public int indexOf(int fromIndex, int toIndex, byte value) {
        return this.byteBuf.indexOf(fromIndex, toIndex, value);
    }

    /**
     * Locates the first occurrence of the specified {@code value} in this
     * buffer.  The search takes place from the current {@code readerIndex}
     * (inclusive) to the current {@code writerIndex} (exclusive).
     * <p>
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param value
     * @return the number of bytes between the current {@code readerIndex}
     * and the first occurrence if found. {@code -1} otherwise.
     */
    public int bytesBefore(byte value) {
        return this.byteBuf.bytesBefore(value);
    }

    /**
     * Locates the first occurrence of the specified {@code value} in this
     * buffer.  The search starts from the current {@code readerIndex}
     * (inclusive) and lasts for the specified {@code length}.
     * <p>
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param length
     * @param value
     * @return the number of bytes between the current {@code readerIndex}
     * and the first occurrence if found. {@code -1} otherwise.
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    public int bytesBefore(int length, byte value) {
        return this.byteBuf.bytesBefore(length, value);
    }

    /**
     * Locates the first occurrence of the specified {@code value} in this
     * buffer.  The search starts from the specified {@code index} (inclusive)
     * and lasts for the specified {@code length}.
     * <p>
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param length
     * @param value
     * @return the number of bytes between the specified {@code index}
     * and the first occurrence if found. {@code -1} otherwise.
     * @throws IndexOutOfBoundsException if {@code index + length} is greater than {@code this.capacity}
     */
    public int bytesBefore(int index, int length, byte value) {
        return this.byteBuf.bytesBefore(index, length, value);
    }

    /**
     * Iterates over the readable bytes of this buffer with the specified {@code processor} in ascending order.
     *
     * @param processor
     * @return {@code -1} if the processor iterated to or beyond the end of the readable bytes.
     * The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public int forEachByte(ByteProcessor processor) {
        return this.byteBuf.forEachByte(processor);
    }

    /**
     * Iterates over the specified area of this buffer with the specified {@code processor} in ascending order.
     * (i.e. {@code index}, {@code (index + 1)},  .. {@code (index + length - 1)})
     *
     * @param index
     * @param length
     * @param processor
     * @return {@code -1} if the processor iterated to or beyond the end of the specified area.
     * The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public int forEachByte(int index, int length, ByteProcessor processor) {
        return this.byteBuf.forEachByte(index, length, processor);
    }

    /**
     * Iterates over the readable bytes of this buffer with the specified {@code processor} in descending order.
     *
     * @param processor
     * @return {@code -1} if the processor iterated to or beyond the beginning of the readable bytes.
     * The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public int forEachByteDesc(ByteProcessor processor) {
        return this.byteBuf.forEachByteDesc(processor);
    }

    /**
     * Iterates over the specified area of this buffer with the specified {@code processor} in descending order.
     * (i.e. {@code (index + length - 1)}, {@code (index + length - 2)}, ... {@code index})
     *
     * @param index
     * @param length
     * @param processor
     * @return {@code -1} if the processor iterated to or beyond the beginning of the specified area.
     * The last-visited index If the {@link ByteProcessor#process(byte)} returned {@code false}.
     */
    public int forEachByteDesc(int index, int length, ByteProcessor processor) {
        return this.byteBuf.forEachByteDesc(index, length, processor);
    }

    /**
     * Returns a copy of this buffer's readable bytes.  Modifying the content
     * of the returned buffer or this buffer does not affect each other at all.
     * This method is identical to {@code buf.copy(buf.readerIndex(), buf.readableBytes())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     */
    public ByteBuf copy() {
        return this.byteBuf.copy();
    }

    /**
     * Returns a copy of this buffer's sub-region.  Modifying the content of
     * the returned buffer or this buffer does not affect each other at all.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param index
     * @param length
     */
    public ByteBuf copy(int index, int length) {
        return this.byteBuf.copy(index, length);
    }

    /**
     * Returns a slice of this buffer's readable bytes. Modifying the content
     * of the returned buffer or this buffer affects each other's content
     * while they maintain separate indexes and marks.  This method is
     * identical to {@code buf.slice(buf.readerIndex(), buf.readableBytes())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * Also be aware that this method will NOT call {@link #retain()} and so the
     * reference count will NOT be increased.
     */
    public ByteBuf slice() {
        return this.byteBuf.slice();
    }

    /**
     * Returns a retained slice of this buffer's readable bytes. Modifying the content
     * of the returned buffer or this buffer affects each other's content
     * while they maintain separate indexes and marks.  This method is
     * identical to {@code buf.slice(buf.readerIndex(), buf.readableBytes())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link #slice()}.
     * This method behaves similarly to {@code slice().retain()} except that this method may return
     * a buffer implementation that produces less garbage.
     */
    public ByteBuf retainedSlice() {
        return this.byteBuf.retainedSlice();
    }

    /**
     * Returns a slice of this buffer's sub-region. Modifying the content of
     * the returned buffer or this buffer affects each other's content while
     * they maintain separate indexes and marks.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * Also be aware that this method will NOT call {@link #retain()} and so the
     * reference count will NOT be increased.
     *
     * @param index
     * @param length
     */
    public ByteBuf slice(int index, int length) {
        return this.byteBuf.slice(index, length);
    }

    /**
     * Returns a retained slice of this buffer's sub-region. Modifying the content of
     * the returned buffer or this buffer affects each other's content while
     * they maintain separate indexes and marks.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link #slice(int, int)}.
     * This method behaves similarly to {@code slice(...).retain()} except that this method may return
     * a buffer implementation that produces less garbage.
     *
     * @param index
     * @param length
     */
    public ByteBuf retainedSlice(int index, int length) {
        return this.byteBuf.retainedSlice(index, length);
    }

    /**
     * Returns a buffer which shares the whole region of this buffer.
     * Modifying the content of the returned buffer or this buffer affects
     * each other's content while they maintain separate indexes and marks.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * The reader and writer marks will not be duplicated. Also be aware that this method will
     * NOT call {@link #retain()} and so the reference count will NOT be increased.
     *
     * @return A buffer whose readable content is equivalent to the buffer returned by {@link #slice()}.
     * However this buffer will share the capacity of the underlying buffer, and therefore allows access to all of the
     * underlying content if necessary.
     */
    public ByteBuf duplicate() {
        return this.byteBuf.duplicate();
    }

    /**
     * Returns a retained buffer which shares the whole region of this buffer.
     * Modifying the content of the returned buffer or this buffer affects
     * each other's content while they maintain separate indexes and marks.
     * This method is identical to {@code buf.slice(0, buf.capacity())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     * <p>
     * Note that this method returns a {@linkplain #retain() retained} buffer unlike {@link #slice(int, int)}.
     * This method behaves similarly to {@code duplicate().retain()} except that this method may return
     * a buffer implementation that produces less garbage.
     */
    public ByteBuf retainedDuplicate() {
        return this.byteBuf.retainedDuplicate();
    }

    /**
     * Returns the maximum number of NIO {@link ByteBuffer}s that consist this buffer.  Note that {@link #nioBuffers()}
     * or {@link #nioBuffers(int, int)} might return a less number of {@link ByteBuffer}s.
     *
     * @return {@code -1} if this buffer has no underlying {@link ByteBuffer}.
     * the number of the underlying {@link ByteBuffer}s if this buffer has at least one underlying
     * {@link ByteBuffer}.  Note that this method does not return {@code 0} to avoid confusion.
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public int nioBufferCount() {
        return this.byteBuf.nioBufferCount();
    }

    /**
     * Exposes this buffer's readable bytes as an NIO {@link ByteBuffer}. The returned buffer
     * either share or contains the copied content of this buffer, while changing the position
     * and limit of the returned NIO buffer does not affect the indexes and marks of this buffer.
     * This method is identical to {@code buf.nioBuffer(buf.readerIndex(), buf.readableBytes())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     * Please note that the returned NIO buffer will not see the changes of this buffer if this buffer
     * is a dynamic buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public ByteBuffer nioBuffer() {
        return this.byteBuf.nioBuffer();
    }

    /**
     * Exposes this buffer's sub-region as an NIO {@link ByteBuffer}. The returned buffer
     * either share or contains the copied content of this buffer, while changing the position
     * and limit of the returned NIO buffer does not affect the indexes and marks of this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     * Please note that the returned NIO buffer will not see the changes of this buffer if this buffer
     * is a dynamic buffer and it adjusted its capacity.
     *
     * @param index
     * @param length
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffers()
     * @see #nioBuffers(int, int)
     */
    public ByteBuffer nioBuffer(int index, int length) {
        return this.byteBuf.nioBuffer(index, length);
    }

    /**
     * Internal use only: Exposes the internal NIO buffer.
     *
     * @param index
     * @param length
     */
    public ByteBuffer internalNioBuffer(int index, int length) {
        return this.byteBuf.internalNioBuffer(index, length);
    }

    /**
     * Exposes this buffer's readable bytes as an NIO {@link ByteBuffer}'s. The returned buffer
     * either share or contains the copied content of this buffer, while changing the position
     * and limit of the returned NIO buffer does not affect the indexes and marks of this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer.
     * Please note that the returned NIO buffer will not see the changes of this buffer if this buffer
     * is a dynamic buffer and it adjusted its capacity.
     *
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     */
    public ByteBuffer[] nioBuffers() {
        return new ByteBuffer[0];
    }

    /**
     * Exposes this buffer's bytes as an NIO {@link ByteBuffer}'s for the specified index and length
     * The returned buffer either share or contains the copied content of this buffer, while changing
     * the position and limit of the returned NIO buffer does not affect the indexes and marks of this buffer.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of this buffer. Please note that the
     * returned NIO buffer will not see the changes of this buffer if this buffer is a dynamic
     * buffer and it adjusted its capacity.
     *
     * @param index
     * @param length
     * @throws UnsupportedOperationException if this buffer cannot create a {@link ByteBuffer} that shares the content with itself
     * @see #nioBufferCount()
     * @see #nioBuffer()
     * @see #nioBuffer(int, int)
     */
    public ByteBuffer[] nioBuffers(int index, int length) {
        return new ByteBuffer[0];
    }

    /**
     * Returns {@code true} if and only if this buffer has a backing byte array.
     * If this method returns true, you can safely call {@link #array()} and
     * {@link #arrayOffset()}.
     */
    public boolean hasArray() {
        return this.byteBuf.hasArray();
    }

    /**
     * Returns the backing byte array of this buffer.
     *
     * @throws UnsupportedOperationException if there no accessible backing byte array
     */
    public byte[] array() {
        return new byte[0];
    }

    /**
     * Returns the offset of the first byte within the backing byte array of
     * this buffer.
     *
     * @throws UnsupportedOperationException if there no accessible backing byte array
     */
    public int arrayOffset() {
        return this.byteBuf.arrayOffset();
    }

    /**
     * Returns {@code true} if and only if this buffer has a reference to the low-level memory address that points
     * to the backing data.
     */
    public boolean hasMemoryAddress() {
        return this.byteBuf.hasMemoryAddress();
    }

    /**
     * Returns the low-level memory address that point to the first byte of ths backing data.
     *
     * @throws UnsupportedOperationException if this buffer does not support accessing the low-level memory address
     */
    public long memoryAddress() {
        return this.byteBuf.memoryAddress();
    }

    /**
     * Decodes this buffer's readable bytes into a string with the specified
     * character set name.  This method is identical to
     * {@code buf.toString(buf.readerIndex(), buf.readableBytes(), charsetName)}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @param charset
     */
    public String toString(Charset charset) {
        return this.byteBuf.toString(charset);
    }

    /**
     * Decodes this buffer's sub-region into a string with the specified
     * character set.  This method does not modify {@code readerIndex} or
     * {@code writerIndex} of this buffer.
     *
     * @param index
     * @param length
     * @param charset
     */
    public String toString(int index, int length, Charset charset) {
        return this.byteBuf.toString(index, length, charset);
    }

    /**
     * Returns a hash code which was calculated from the content of this
     * buffer.  If there's a byte array which is
     * {@linkplain #equals(Object) equal to} this array, both arrays should
     * return the same value.
     */
    public int hashCode() {
        return this.byteBuf.hashCode();
    }

    /**
     * Determines if the content of the specified buffer is identical to the
     * content of this array.  'Identical' here means:
     * <ul>
     * <li>the size of the contents of the two buffers are same and</li>
     * <li>every single byte of the content of the two buffers are same.</li>
     * </ul>
     * Please note that it does not compare {@link #readerIndex()} nor
     * {@link #writerIndex()}.  This method also returns {@code false} for
     * {@code null} and an object which is not an instance of
     * {@link ByteBuf} type.
     *
     * @param obj
     */
    public boolean equals(Object obj) {
        return this.byteBuf.equals(obj);
    }

    /**
     * Compares the content of the specified buffer to the content of this
     * buffer. Comparison is performed in the same manner with the string
     * comparison functions of various languages such as {@code strcmp},
     * {@code memcmp} and {@link String#compareTo(String)}.
     *
     * @param buffer
     */
    public int compareTo(ByteBuf buffer) {
        return this.byteBuf.compareTo(buffer);
    }

    /**
     * Returns the string representation of this buffer.  This method does not
     * necessarily return the whole content of the buffer but returns
     * the values of the key properties such as {@link #readerIndex()},
     * {@link #writerIndex()} and {@link #capacity()}.
     */
    public String toString() {
        return this.byteBuf.toString();
    }

    public ByteBuf retain(int increment) {
        return this.byteBuf.retain(increment);
    }

    /**
     * Returns the reference count of this object.  If {@code 0}, it means this object has been deallocated.
     */
    public int refCnt() {
        return this.byteBuf.refCnt();
    }

    public ByteBuf retain() {
        return this.byteBuf.retain();
    }

    public ByteBuf touch() {
        return this.byteBuf.touch();
    }

    public ByteBuf touch(Object hint) {
        return this.byteBuf.touch(hint);
    }

    /**
     * Decreases the reference count by {@code 1} and deallocates this object if the reference count reaches at
     * {@code 0}.
     *
     * @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
     */
    public boolean release() {
        return this.byteBuf.release();
    }

    /**
     * Decreases the reference count by the specified {@code decrement} and deallocates this object if the reference
     * count reaches at {@code 0}.
     *
     * @param decrement
     * @return {@code true} if and only if the reference count became {@code 0} and this object has been deallocated
     */
    public boolean release(int decrement) {
        return this.byteBuf.release(decrement);
    }
}
