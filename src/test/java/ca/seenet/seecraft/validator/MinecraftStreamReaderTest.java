package ca.seenet.seecraft.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MinecraftStreamReaderTest
{
    private PipedOutputStream out;
    private PipedInputStream in;

    private MinecraftStreamReader reader;

    @BeforeEach
    public void before() throws IOException
    {
        this.out = new PipedOutputStream();
        this.in = new PipedInputStream(this.out);

        this.reader = new MinecraftStreamReader(this.in);
    }

    @Test
    public void readsShort() throws IOException
    {
        this.out.write(new byte[] {(byte) 0b0110_0011, (byte) 0b1101_1101});
        assertEquals(25565, this.reader.readShort());
    }

    @Test
    public void readsVarintOne() throws IOException
    {

        this.out.write((byte) 0b0000_0001);
        assertEquals(1, this.reader.readVarint());
    }

    @Test
    public void readsVarintThreeHundred() throws IOException
    {
        this.out.write(new byte[] {(byte) 0b1010_1100, (byte) 0b0000_0010});
        assertEquals(300, this.reader.readVarint());
    }

    @Test
    public void readsString() throws IOException
    {
        this.out.write(new byte[] {0b00000101, 0b01101110, 0b01100001, 0b01101110,
                                   0b01100011, 0b01111001});
        assertEquals("nancy", this.reader.readString());
    }
}
