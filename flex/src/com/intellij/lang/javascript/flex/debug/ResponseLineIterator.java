package com.intellij.lang.javascript.flex.debug;

import java.util.*;

class ResponseLineIterator implements Iterator<String> {
  private final List<String> lines;
  private int current;

  ResponseLineIterator(String commandOutput) {
    lines = new ArrayList<>();
    StringTokenizer tokenizer = new StringTokenizer(commandOutput, "\r\n");
    while (tokenizer.hasMoreElements()) {
      String s = tokenizer.nextToken().trim();
      if (s.length() == 0) continue;
      lines.add(s);
    }
  }

  @Override
  public boolean hasNext() {
    return current < lines.size();
  }

  @Override
  public String next() {
    String result = next_(current);
    ++current;
    return result;
  }

  private String next_(int current) {
    String result;
    if (current < lines.size()) {
      result = lines.get(current);
    } else {
      throw new NoSuchElementException();
    }
    return result;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public String getNext() {
    return next_(current);
  }

  public void retreat() {
    if (current == 0) throw new NoSuchElementException();
    --current;
  }
}
