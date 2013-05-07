package com.intellij.javascript.karma.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.CharArrayQueue;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
* @author Sergey Simonchik
*/
public class EventEmitterReader extends Reader {

  private static final char NL = '\n';
  private static final String PREFIX = "##intellij-event[";
  private static final String SUFFIX = "]";
  private static final String SUFFIX_WITH_NL = SUFFIX + NL;

  private final Reader myReader;
  private final CharArrayQueue myCharQueue = new CharArrayQueue(8192);
  private final char[] myCharBuffer = new char[8192];
  private final StringBuilder myCurrentLine = new StringBuilder();
  private final List<StreamEventListener> myListeners = ContainerUtil.createEmptyCOWList();
  private boolean mySkipLF = false;

  public EventEmitterReader(@NotNull Reader reader) {
    myReader = reader;
  }

  public void addListener(@NotNull StreamEventListener listener) {
    myListeners.add(listener);
  }

  @Override
  public boolean ready() throws IOException {
    readAvailable();
    return !myCharQueue.isEmpty();
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
    if (mySkipLF && ch != NL) {
      myCurrentLine.append('\r');
    }
    if (ch == '\r') {
      mySkipLF = true;
    }
    else {
      mySkipLF = false;
      myCurrentLine.append(ch);
    }
    if (ch == NL) {
      String line = myCurrentLine.toString();
      if (!handleLineAsEvent(line)) {
        myCharQueue.add(line);
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
    while (myCharQueue.isEmpty()) {
      int chCode = myReader.read();
      if (chCode != -1) {
        handleNextChar((char) chCode);
      }
      else {
        handleNextChar(NL);
        break;
      }
    }
    if (myCharQueue.isEmpty()) {
      return -1;
    }
    return myCharQueue.poll();
  }

  @Override
  public int read(char[] cbuf) throws IOException {
    return read(cbuf, 0, cbuf.length);
  }

  @Override
  public int read(char[] cbuf, int off, int len) throws IOException {
    readAvailable();
    int i = off;
    int cnt = 0;
    while (cnt < len && !myCharQueue.isEmpty()) {
      cbuf[i] = (char) myCharQueue.poll();
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
  public void mark(int readlimit) {}

  @Override
  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  @Override
  public boolean markSupported() {
    return false;
  }

}
