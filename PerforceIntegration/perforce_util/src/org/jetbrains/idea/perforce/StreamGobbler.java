/*
 * Copyright 2000-2005 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.idea.perforce;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vcs.impl.CancellableRunnable;
import com.intellij.util.ThrowableConsumer;

import java.io.*;

public final class StreamGobbler implements CancellableRunnable {
  private static final Logger LOG = Logger.getInstance(StreamGobbler.class);
  private static final int IN_MEMORY_RESULT_THRESHOULD = 48 * 1024; // 48K

  private OutputStream myByteContents;
  private final ByteArrayOutputStream myInMemoryContents;
  private final InputStream is;
  private File myTempFile = null;
  private int myOutputLength = 0;
  private boolean myResultUsedOnce;
  private volatile boolean myMasterExited;

  public StreamGobbler(final InputStream is) {
    this.is = is;
    myInMemoryContents = new ByteArrayOutputStream();
    myByteContents = myInMemoryContents;
    myResultUsedOnce = true;
  }

  @Override
  public void run() {
    byte[] buffer = new byte[8 * 1024];
    try {
      int read;
      while (((read = is.read(buffer, 0, buffer.length)) != -1)) {
        myByteContents.write(buffer, 0, read);
        myOutputLength += read;

        if (myTempFile == null && myOutputLength > IN_MEMORY_RESULT_THRESHOULD) {
          switchToTemporaryFileContent();
        }
      }
    }
    catch (IOException ioe) {
      if (myMasterExited) {
        // stream is already closed by parent process; read was blocking so ok here, we expected that
      } else {
        LOG.error(myInMemoryContents.toString(), ioe);
      }
    } finally {
      try {
        myByteContents.close();
      }
      catch (IOException e) {
        //
      }
    }
  }

  private boolean switchToTemporaryFileContent() {
    try {
      myTempFile = FileUtil.createTempFile("idea_p4", "command.output");
    }
    catch (IOException e) {
      LOG.info(e);
      myTempFile = null;
      return false;
    }
    try {
      myByteContents = new BufferedOutputStream(new FileOutputStream(myTempFile));
      myInMemoryContents.writeTo(myByteContents);
      return true;
    }
    catch (IOException e) {
      LOG.error(e);
      if (myByteContents != null) {
        try {
          myByteContents.close();
        }
        catch (IOException e1) {
          //
        }
      }
      FileUtil.delete(myTempFile);
      myTempFile = null;
      return false;
    }
  }

  public int getResultLength() {
    return myOutputLength;
  }

  @Override
  public String toString() {
    return myInMemoryContents.toString();
  }

  /**
   * Should be called only once, since it might be backed by temp file which is immediately deleted
   */
  public void allowSafeStreamUsage(final ThrowableConsumer<InputStream, IOException> consumer) throws IOException {
    if (! myResultUsedOnce) {
      LOG.warn("Result taken from StreamGobbler more than once. " +
               "An error, since the result might have been backed by a temp file which is already deleted.", new Throwable());
    }
    myResultUsedOnce = false;

    final InputStream is;
      if (myTempFile != null) {
        is = new BufferedInputStream(new FileInputStream(myTempFile));
      }
      else {
        is = new ByteArrayInputStream(myInMemoryContents.toByteArray());
      }
    try {
      consumer.consume(is);
    } finally {
      try {
        is.close();
      }
      catch (IOException e) {
        //
      }
      if (myTempFile != null) {
        FileUtil.delete(myTempFile);
        myTempFile = null;
      }
    }
  }

  public void deleteTempFile() {
    if (myTempFile != null) {
      FileUtil.delete(myTempFile);
      myTempFile = null;
    }
  }

  @Override
  public void cancel() {
    myMasterExited = true;
  }
}

