/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.file.cache.store;

import static org.junit.Assert.assertEquals;

import alluxio.client.file.cache.PageId;
import alluxio.client.file.cache.PageStore;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LocalPageStoreTest {

  @Rule
  public TemporaryFolder mTemp = new TemporaryFolder();

  private LocalPageStoreOptions mOptions;

  @Before
  public void before() {
    mOptions = new LocalPageStoreOptions();
    mOptions.setRootDir(mTemp.getRoot().getAbsolutePath());
  }

  @Test
  public void testPutGetDefault() throws Exception {
    LocalPageStore pageStore = new LocalPageStore(mOptions);
    helloWorldTest(pageStore);
  }

  @Test
  public void testSmallBuffer() throws Exception {
    mOptions.setBufferSize(1).setBufferPoolSize(1);
    LocalPageStore pageStore = new LocalPageStore(mOptions);
    helloWorldTest(pageStore);
  }

  @Test
  public void testSingleFileBucket() throws Exception {
    mOptions.setFileBuckets(1);
    LocalPageStore pageStore = new LocalPageStore(mOptions);
    long numFiles = 100;
    for (int i = 0; i < numFiles; i++) {
      PageId id = new PageId(Integer.toString(i), 0);
      pageStore.put(id, "test".getBytes());
    }
    assertEquals(1, Files.list(
        Paths.get(mOptions.getRootDir(), Long.toString(mOptions.getPageSize()))).count());
  }

  @Test
  public void testMultiFileBucket() throws Exception {
    int numBuckets = 10;
    mOptions.setFileBuckets(numBuckets);
    LocalPageStore pageStore = new LocalPageStore(mOptions);
    long numFiles = numBuckets * 10;
    for (int i = 0; i < numFiles; i++) {
      PageId id = new PageId(Integer.toString(i), 0);
      pageStore.put(id, "test".getBytes());
    }
    assertEquals(10, Files.list(
        Paths.get(mOptions.getRootDir(), Long.toString(mOptions.getPageSize()))).count());
  }

  private void helloWorldTest(PageStore store) throws Exception {
    String msg = "Hello, World!";
    PageId id = new PageId("0", 0);
    store.put(id, msg.getBytes());
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    ByteBuffer buf = ByteBuffer.allocate(1024);
    store.get(id).read(buf);
    buf.flip();
    String read = StandardCharsets.UTF_8.decode(buf).toString();
    assertEquals(msg, read);
  }
}
