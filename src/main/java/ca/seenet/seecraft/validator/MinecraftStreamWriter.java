package ca.seenet.seecraft.validator;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MinecraftStreamWriter implements Closeable
{
    private OutputStream stream;

    public MinecraftStreamWriter(OutputStream stream)
    {
        this.stream = stream;
    }

    public void close() throws IOException
    {
        this.stream.close();
    }

    /**
     * Write a disconnect message. Does not support messages longer than 244
     * bytes in length.
     * 
     * @param message
     *            the disconnect message
     * @throws IOException
     */
    public void writeDisconnect(String message) throws IOException
    {
        byte[] messageBytes = String.format("{text:\"%s\"}", message).getBytes(StandardCharsets.UTF_8);

        /* Packet length including the length of the packet ID and message. */
        this.stream.write(messageBytes.length + 2);
        /* Packet ID. */
        this.stream.write(0);
        /* String length. */
        this.stream.write(messageBytes.length);
        /* Message. */
        this.stream.write(messageBytes);
    }
}
