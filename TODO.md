CachingInputStream -

Reading an InputStream via Caching InputStream.

*** mark() determines when the cache is appended to!



Example 1 - Should not read the same 1024 bytes twice! -


InputStream is = ...
CachingFilterInputStream cfis1 = new CachingFilterInputStream(cache1, is);
cfis1.mark(Int.MAX_VALUE);
cfis1.read(new buf[1024]);
CachingFilterInputStream cfis2 = new CachingFilterInputStream(cache2, is);
cfis2.read(new buf[1024]);
*** (Because of different caches and underlying stram has been read)



Will read the same 1024 bytes twice:

InputStream is = ...
CachingFilterInputStream cfis1 = new CachingFilterInputStream(cache1, is);
cfis1.mark(Int.MAX_VALUE);
CachingFilterInputStream cfis2 = new CachingFilterInputStream(cache1, is);
cfis1.read(new buf[1024]);
cfis2.read(new buf[1024]);
*** (Because offset is set to zero in both cfis and underlying cache is empty)

InputStream is = ...
CachingFilterInputStream cfis1 = new CachingFilterInputStream(cache1, is);
cfis1.mark(Int.MAX_VALUE);
cfis1.read(new buf[1024]);
CachingFilterInputStream cfis2 = new CachingFilterInputStream(cache1, is);
cfis2.read(new buf[1024]);
*** (Because offset is set to zero in second cfis and underlying cache is same)


InputStream is = ...
CachingFilterInputStream cfis1 = new CachingFilterInputStream(cache1, is);
CachingFilterInputStream cfis2 = new CachingFilterInputStream(cache1, is);
cfis1.mark(Int.MAX_VALUE);
cfis1.read(new buf[1024]);
cfis2.read(new buf[1024]);


Will read the same 1024 bytes twice:
CachingFilterInputStream cfis1 = new CachingFilterInputStream(cache1, is);
cfis1.mark(Int.MAX_VALUE);
CachingFilterInputStream cfis2 = new CachingFilterInputStream(cache1, is);
cfis1.read(new buf[1024]);
cfis2.read(new buf[1024]);