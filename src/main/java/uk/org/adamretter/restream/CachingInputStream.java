/**
 * Copyright © 2013, Adam Retter
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package uk.org.adamretter.restream;

import uk.org.adamretter.restream.cache.FilterInputStreamCache;

import java.io.IOException;
import java.io.InputStream;

/**
 * TODO a 2nd attempt at this - fill this in later
 */
public class CachingInputStream extends InputStream {

    private final static int END_OF_STEAM = -1;

    private final FilterInputStreamCache cache;
    private final ManagedInputStream src;

    private int markOffset = -1;
    private int markReadLimit = 0;

    private int srcOffset = 0;
    private int offset = 0;

    private boolean closed = false;

    public CachingInputStream(final FilterInputStreamCache cache, final InputStream src) {
        this.cache = cache;
        this.src = new ManagedInputStream(src);
    }

    public CachingInputStream(final CachingInputStream src) {
        this.cache = src.cache;
        this.src = src.src;
        this.srcOffset = src.srcOffset;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void mark(final int readLimit) {
        markReadLimit = readLimit;
        markOffset = offset;
    }

    @Override
    public synchronized void reset() throws IOException {
        if(markOffset < 0) {
            throw new IOException("Resetting to invalid mark");
        }
        offset = markOffset;
    }

    @Override
    public int available() throws IOException {
        final int available;
        if(closed) {
            available = 0;
        } else {
            available = srcOffset - offset + src.available();
        }
        return available;
    }

    @Override
    public int read() throws IOException {
        throwIfClosed();

        if(offset < srcOffset) {
            //read from cache
            return getCache().get(offset++);
        } else {
            final int read = src.read();
            if(read > END_OF_STEAM) {
                srcOffset++;
                offset++;
                getCache().write(read);
            }
            return read;
        }
    }

    @Override
    public long skip(final long n) throws IOException {
        throwIfClosed();

        if(n < 1) {
            return 0;
        }

        final long skipped;
        if(offset < srcOffset) {
            //can take all or some data from cache

            final int availableFromCache = srcOffset - offset;

            //what about if not enough data in cache, because still data in src?
            final long needToReadFromSrc = n - availableFromCache;
            if(needToReadFromSrc > 0) {
                final int readFromSrc = read(new byte[needToReadFromSrc < Integer.MAX_VALUE ? (int)needToReadFromSrc : Integer.MAX_VALUE]);
                skipped = availableFromCache + readFromSrc;
            } else {
                skipped = n;
            }
        } else {
            //have to read from src
            final int readFromSrc = read(new byte[n < Integer.MAX_VALUE ? (int)n : Integer.MAX_VALUE]);
            skipped = readFromSrc;
        }

        return skipped;
    }

    protected FilterInputStreamCache getCache() {
        return cache;
    }

    @Override
    public void close() throws IOException {
        closed = true;
        src.close();
    }

    private void throwIfClosed() throws IOException {
        if(closed) {
            throw new IOException("Stream closed");
        }
    }
}
