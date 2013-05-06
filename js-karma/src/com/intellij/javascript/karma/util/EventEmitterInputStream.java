package com.intellij.javascript.karma.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.ByteArrayQueue;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class EventEmitterInputStream extends InputStream {

  private static final char NL = '\n';
  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]";
  private static final String SUFFIX_WITH_NL = SUFFIX + NL;

  private final Reader myReader;
  private final Charset myCharset;
  private final ByteArrayQueue myByteQueue = new ByteArrayQueue(8192);
  private final char[] myCharBuffer = new char[8192];
  private final StringBuilder myCurrentLine = new StringBuilder();
  private final List<StreamEventListener> myListeners = ContainerUtil.createEmptyCOWList();
  private boolean skipLF = false;


  public EventEmitterInputStream(@NotNull InputStream inputStream,
                                 @NotNull Charset charset) {
    //noinspection IOResourceOpenedButNotSafelyClosed
    myReader = new InputStreamReader(inputStream, charset);
    myCharset = charset;
  }

  public void addListener(@NotNull StreamEventListener listener) {
    myListeners.add(listener);
  }

  @Override
  public int available() throws IOException {
    readAvailable();
    return myByteQueue.size();
  }

  /**
   * Reads as much data as possible without blocking.
   */
  private void readAvailable() throws IOException {
    while (myReader.ready()) {
      int n = myReader.read(myCharBuffer);
      if (n <= 0) break;

      for (int i = 0; i < n; i++) {
        handleNextChar(myCharBuffer[i]);
      }
    }
  }

  private void handleNextChar(char ch) {
    if (skipLF && ch != NL) {
      myCurrentLine.append('\r');
    }
    if (ch == '\r') {
      skipLF = true;
    }
    else {
      skipLF = false;
      myCurrentLine.append(ch);
    }
    if (ch == NL || ch == -1) {
      String line = myCurrentLine.toString();
      if (!handleLineAsEvent(line)) {
        byte[] buf = line.getBytes(myCharset);
        myByteQueue.addAll(buf);
      }
      myCurrentLine.setLength(0);
    }
  }

  private boolean handleLineAsEvent(@NotNull String line) {
    if (line.startsWith(PREFIX)) {
      final String suffix = StringUtil.endsWithChar(line, NL) ? SUFFIX_WITH_NL : SUFFIX;
      if (line.endsWith(suffix)) {
        String eventText = line.substring(PREFIX.length(), line.length() - suffix.length());
        for (StreamEventListener listener : myListeners) {
          listener.on(eventText);
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public int read() throws IOException {
    while (myByteQueue.isEmpty()) {
      int chCode = myReader.read();
      if (chCode != -1) {
        handleNextChar((char) chCode);
      }
      else {
        handleNextChar(NL);
        break;
      }
    }
    if (myByteQueue.isEmpty()) {
      return -1;
    }
    return myByteQueue.poll();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    readAvailable();
    int i = off;
    int cnt = 0;
    while (cnt < len && !myByteQueue.isEmpty()) {
      b[i] = (byte) myByteQueue.poll();
      i++;
      cnt++;
    }
    return cnt;
  }

  @Override
  public long skip(long n) throws IOException {
    return super.skip(n);
  }

  @Override
  public void close() throws IOException {
    myReader.close();
  }

  @Override
  public synchronized void mark(int readlimit) {}

  @Override
  public synchronized void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  @Override
  public boolean markSupported() {
    return false;
  }

}
