/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wuman.twolevellrucache;

import java.io.File;

import junit.framework.TestCase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

public class TwoLevelLruCacheTest extends TestCase {

    private final int appVersion = 100;
    private String javaTmpDir;
    private File cacheDir;
    private TwoLevelLruCache<String> cache;
    private Gson gson;
    private GsonConverter<String> converter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        /* EP: `java.io.tmpdir`
           A standard Java system property that determines where to write temporary files.
           The default value is typically "/tmp" on Unix-like platforms.
         */
        javaTmpDir = System.getProperty("java.io.tmpdir");

        // EP: put the cache file in the tmpdir
        cacheDir = new File(javaTmpDir, "TwoLevelLruCacheTest");

        // EP: ensure it exists
        cacheDir.mkdir();

        // EP: delete anything already in there
        for (File file : cacheDir.listFiles())
            file.delete();


        gson = new GsonBuilder().create();

        /* EP:
         GsonConverter is defined by 'wuman' in its own class file.
         It takes a Gson object and a Class<T> to convert to/from,
           and it is able to turn arbitrary byte-arrays into json objects.
         That's all I know about it at this point.
        */
        converter = new GsonConverter<String>(gson, String.class);

        /* EP: the whole MAX_VALUE thing would need to be changed in any real
           environment if we don't literally have that many bytes available */
        cache = new TwoLevelLruCache<String>(cacheDir, appVersion,
                Integer.MAX_VALUE, Long.MAX_VALUE, converter);
    }

    @Override
    protected void tearDown() throws Exception {
        cache.close();      // close the disk cache
        super.tearDown();   // I believe this doesn't do anything in this case
    }

    @Test
    public void testWriteAndReadEntry() throws Exception {
        cache.put("k1", "ABC");
        String value = cache.get("k1");
        assertEquals("ABC", value);
    }

    @Test
    public void testReadAndWriteEntryAcrossCacheOpenAndClose() throws Exception {
        cache.put("k1", "A");
        cache.close();

        cache = new TwoLevelLruCache<String>(cacheDir, appVersion,
                Integer.MAX_VALUE, Long.MAX_VALUE, converter);
        String value = cache.get("k1");
        assertEquals("A", value);
    }

}
