/*
 * Copyright 2000-2006 JetBrains s.r.o.
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

package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.util.ThrowableConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.StreamGobbler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ExecResult {
  private int myExitCode = -1;
  private String myStdout = "";
  private String myStderr = "";
  private StreamGobbler myOutputGobbler;
  private StreamGobbler myErrorGobbler;
  private Throwable myException = null;
  private String myErrorString;
  private Charset myCharset = StandardCharsets.UTF_8;

  @Override
  @SuppressWarnings({"HardCodedStringLiteral"})
  public String toString() {
    final StringBuilder buf = new StringBuilder();
    buf.append("ErrCode=");
    buf.append(getExitCode());
    buf.append("\nStdOut:------------\n");
    appendStreamData(myStdout, myOutputGobbler, buf);
    buf.append("\nStdErr:------------\n");
    appendStreamData(myStderr, myErrorGobbler, buf);
    if (getException() != null) {
      buf.append("\nException----------\n");
      buf.append(getException());
    }
    buf.append("\nEnd----------------\n");
    return buf.toString();
  }

  private static void appendStreamData(final String data, final StreamGobbler gobbler, final StringBuilder buf) {
    if (gobbler != null) {
      // hope that line separators are not very important for toString
      buf.append(gobbler.toString());
    }
    else {
      buf.append(data);
    }
  }

  public int getExitCode() {
    return myExitCode;
  }

  public void setExitCode(final int exitCode) {
    myExitCode = exitCode;
  }

  public void setStdout(final String stdout) {
    myStdout = stdout;
  }

  public void setStderr(final String stderr) {
    myStderr = stderr;
  }

  public void allowSafeStdoutUsage(final ThrowableConsumer<InputStream, IOException> consumer) throws IOException {
    if (myOutputGobbler != null) {
      myOutputGobbler.allowSafeStreamUsage(consumer);
    } else {
      consumer.consume(new ByteArrayInputStream(myStdout.getBytes(myCharset)));
    }
  }

  public @NotNull @NlsSafe String getStdout() {
    if (myOutputGobbler != null) {
      return readStreamConvertingLineSeparators(myOutputGobbler, myCharset);
    }
    return myStdout;
  }

  public void setOutputGobbler(final StreamGobbler outputGobbler) {
    myOutputGobbler = outputGobbler;
  }

  public void setErrorGobbler(final StreamGobbler errorGobbler) {
    myErrorGobbler = errorGobbler;
  }

  public @NotNull @NlsSafe String getStderr() {
    if (myErrorGobbler != null) {
      if (myErrorString == null) {
        // when temp file is used, we can NOT read stream several times (it is destroyed after first read)
        myErrorString = readStreamConvertingLineSeparators(myErrorGobbler, myCharset);
      }
      return myErrorString;
    }
    return myStderr;
  }

  public Throwable getException() {
    return myException;
  }

  public void setException(final Throwable exception) {
    myException = exception;
  }

  public Charset getCharset() {
    return myCharset;
  }

  public void setCharset(Charset charset) {
    myCharset = charset;
  }

  private static String readStreamConvertingLineSeparators(final StreamGobbler gobbler, Charset charset) {
    try {
      final StringBuilder result = new StringBuilder(gobbler.getResultLength());
      gobbler.allowSafeStreamUsage(inputStream -> {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
        appendDataConvertingLineSeparators(reader, result);
      });
      return result.toString();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void appendDataConvertingLineSeparators(final BufferedReader reader, final StringBuilder result) throws IOException {
    do {
      String line = reader.readLine();
      if (line == null) break;
      result.append(line);
      result.append('\n');
    }
    while (true);
  }

  public void cleanup() {
    if (myOutputGobbler != null) {
      myOutputGobbler.deleteTempFile();
    }
    if (myErrorGobbler != null) {
      myErrorGobbler.deleteTempFile();
    }
  }
}
