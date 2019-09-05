package drwdrd.ktdev.kengine;

public interface BitReader {

    short readShort(byte[] data, int offset);

    byte readByte(byte[] data, int offset);

    int readInt(byte[] data, int offset);

    long readLong(byte[] data, int offset);

    float readFloat(byte[] data, int offset);

    double readDouble(byte[] data, int offset);

    void swapw(byte[] data, int off, int count);

    void swapdw(byte[] data, int off, int count);

    void swapqw(byte[] data, int off, int count);
}
