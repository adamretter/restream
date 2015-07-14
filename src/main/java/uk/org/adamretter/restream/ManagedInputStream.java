package uk.org.adamretter.restream;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of ManagedInputStream references to an Underlying InputStream
 * only allows the underlying InputStream to be closed when all
 * over-arching ManagedInputStreams are closed
 */
public class ManagedInputStream extends FilterInputStream {

    private static Map<InputStream, Integer> inputStreams = new HashMap<InputStream, Integer>();

    private boolean closed = false;

    public ManagedInputStream(final InputStream is) {
       super(is);
       synchronized(inputStreams) {
           final Integer refs = inputStreams.get(is);
           final int newRefs;
           if(refs == null) {
               newRefs = 1;
           } else {
               newRefs = refs + 1;
           }
           inputStreams.put(is, newRefs);
       }
    }

    @Override
    public void close() throws IOException {
        synchronized(inputStreams) {
            if(!closed) {
                final Integer refs = inputStreams.get(this.in);
                if(refs == null) {
                    return;
                }

                final int newRefs = refs - 1;
                inputStreams.put(this.in, newRefs);

                if(newRefs < 1) {
                    try {
                        this.in.close();
                    } finally {
                        closed = true;
                    }
                }
            }
        }
    }
}
