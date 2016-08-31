package ca.seenet.seecraft.validator;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class MinecraftStreamReader implements Closeable
{
    private static int CONTINUE_FLAG = 0b1000_0000;
    private static int CONTINUE_MASK = ~CONTINUE_FLAG;
    private static short BYTE_SIZE = 8;
    private static short BLOCK_SIZE = 7;

    private InputStream stream;

    public MinecraftStreamReader(InputStream stream)
    {
        this.stream = stream;
    }

    public void close() throws IOException
    {
        this.stream.close();
    }

    /**
     * Read a 16-bit short from the stream.
     * 
     * @return the short from the stream
     * @throws IOException
     */
    public short readShort() throws IOException
    {
        short value = 0;
        value |= this.stream.read() << BYTE_SIZE;
        value |= this.stream.read();
        return value;
    }

    /**
     * Read a Protobuf Varint from the stream.
     * 
     * @return the integer value from the stream
     * @throws IOException
     */
    public long readVarint() throws IOException
    {
        long value = 0;
        short read = 0;
        boolean more = false;

        do
        {
            int next = this.stream.read();
            more = (next & CONTINUE_FLAG) > 0;

            next &= CONTINUE_MASK;
            value += next << (BLOCK_SIZE * read++);
        } while (more);

        return value;
    }

    /**
     * Read a string from the stream.
     * 
     * @return the string from the stream
     * @throws IOException
     */
    public String readString() throws IOException
    {
        int length = (int) this.readVarint();
        byte[] string = new byte[length];
        this.stream.read(string);

        return new String(string);
    }
}
