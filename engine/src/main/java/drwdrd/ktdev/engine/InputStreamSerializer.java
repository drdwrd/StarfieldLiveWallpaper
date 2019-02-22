package drwdrd.ktdev.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class InputStreamSerializer extends InputStream {

    public InputStreamSerializer(InputStream in, ByteOrder order, int bufferSize) {
        this.inputStream = in;
        dataBuffer = new byte[bufferSize];
        serializer = new DataSerializer(order);
    }

    public ByteOrder getByteOrder() {
        return serializer.getByteOrder();
    }

    public void setByteOrder(ByteOrder byteOrder) {
        serializer.setByteOrder(byteOrder);
    }

    public int available() throws IOException {
        return inputStream.available();
    }

    public final byte readByte() throws IOException {
        if (checkGet(1) == 1) {
            byte c = serializer.readByte(dataBuffer, dataMarker);
            dataMarker += 1;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final short readShort() throws IOException {
        if (checkGet(2) == 2) {
            short c = serializer.readShort(dataBuffer, dataMarker);
            dataMarker += 2;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final int readInt() throws IOException {
        if (checkGet(4) == 4) {
            int c = serializer.readInt(dataBuffer, dataMarker);
            dataMarker += 4;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final long readLong() throws IOException {
        if (checkGet(8) == 8) {
            long c = serializer.readLong(dataBuffer, dataMarker);
            dataMarker += 8;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final float readFloat() throws IOException {
        if (checkGet(4) == 4) {
            float c = serializer.readFloat(dataBuffer, dataMarker);
            dataMarker += 4;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final double readDouble() throws IOException {
        if (checkGet(8) == 8) {
            double c = serializer.readDouble(dataBuffer, dataMarker);
            dataMarker += 8;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final int read(byte b[], int off, int len) throws IOException {
        int nbytes;
        if ((nbytes = checkGet(len)) > 0) {
            System.arraycopy(dataBuffer, dataMarker, b, off, nbytes);
            dataMarker += nbytes;
        }
        return nbytes;
    }

    public int read(ByteBuffer byteBuffer, int offset, int length) throws IOException {
        int nbytes;
        if ((nbytes = checkGet(length)) > 0) {
            byteBuffer.position(offset);
            byteBuffer.put(dataBuffer, dataMarker, nbytes);
            dataMarker += nbytes;
        }
        return nbytes;
    }

    public int read() throws IOException {
        if (checkGet(1) == 1) {
            int c = (int) (dataBuffer[dataMarker] & 0xff);
            dataMarker += 1;
            return c;
        }
        throw new IOException("Cannot read data.");
    }

    public final void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    //TODO: not very efficient
    public final int skip(int byteCount) throws IOException {
        int nbytes = checkGet(byteCount);
        dataMarker += nbytes;
        return nbytes;
    }

    private int checkGet(int size) throws IOException {
        if (size > dataBuffer.length)                            //if size is bigger than buffer, realloc buffer
        {
            byte[] newDataBuffer = null;
            newDataBuffer = new byte[size];
            System.arraycopy(dataBuffer, 0, newDataBuffer, 0, dataBuffer.length);
            dataBuffer = newDataBuffer;
            newDataBuffer = null;
        }
        if (dataMarker == dataLength) {
            dataMarker = 0;
            dataLength = 0;
        }
        //check buffer
        if ((dataMarker + size) > dataLength) {
            if (dataMarker < dataLength) {
                dataLength -= dataMarker;
                System.arraycopy(dataBuffer, dataMarker, dataBuffer, 0, dataLength);
                dataMarker = 0;
            }
            //fill buffer
            int bytesRead;
            if ((bytesRead = inputStream.read(dataBuffer, dataLength, size)) == -1) {
                throw new IOException("Cannot read data.");
            }
            dataLength += bytesRead;
        }
        if (dataLength < size) {
            return dataLength;
        }
        return size;
    }

    private InputStream inputStream = null;
    private DataSerializer serializer = null;
    private byte[] dataBuffer = null;
    private int dataLength = 0;
    private int dataMarker = 0;
}
