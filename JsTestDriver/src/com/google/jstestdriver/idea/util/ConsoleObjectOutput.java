package com.google.jstestdriver.idea.util;

import java.io.IOException;
import java.io.ObjectOutput;

import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;

public class ConsoleObjectOutput implements ObjectOutput {
  @Override
  public void writeObject(Object obj) throws IOException {
    if (obj instanceof TestResultProtocolMessage) {
      TestResultProtocolMessage message = (TestResultProtocolMessage) obj;
      System.out.println(message.phase + " " + message.result + " " + message.message);
    }
  }

  @Override
  public void write(int b) throws IOException {
  }

  @Override
  public void write(byte[] b) throws IOException {
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
  }

  @Override
  public void writeBoolean(boolean v) throws IOException {
  }

  @Override
  public void writeByte(int v) throws IOException {
  }

  @Override
  public void writeShort(int v) throws IOException {
  }

  @Override
  public void writeChar(int v) throws IOException {
  }

  @Override
  public void writeInt(int v) throws IOException {
  }

  @Override
  public void writeLong(long v) throws IOException {
  }

  @Override
  public void writeFloat(float v) throws IOException {
  }

  @Override
  public void writeDouble(double v) throws IOException {
  }

  @Override
  public void writeBytes(String s) throws IOException {
  }

  @Override
  public void writeChars(String s) throws IOException {
  }

  @Override
  public void writeUTF(String s) throws IOException {
  }

  @Override
  public void flush() throws IOException {
  }

  @Override
  public void close() throws IOException {
  }
}
