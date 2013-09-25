package uk.org.adamretter.restream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simple version of a ByteArrayInputStream which
 * does not permit mark/reset
 *
 * @author Adam Retter <adam.retter@googlemail.com>
 */
public class NonMarkableByteArrayInputStream extends InputStream {

    final ByteArrayInputStream is;
    
    public NonMarkableByteArrayInputStream(final byte buf[]) {
        is = new ByteArrayInputStream(buf);
    }
    
    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readAheadLimit) {
        //no-op
    }

    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("mark/reset not supported");
    }

    @Override
    public int read() throws IOException {
        return is.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return is.read(b, off, len);
    }
}
