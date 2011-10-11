package com.google.jstestdriver.idea.util;

import com.google.jstestdriver.idea.execution.tree.JstdTestRunnerFailure;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;

import java.io.IOException;
import java.io.ObjectOutput;

public class ConsoleObjectOutput implements ObjectOutput {

  public static String messageToString(TestResultProtocolMessage message) {
    Object[][] args = {
        {"phase", message.phase},
        {"result", message.result},
        {"message", message.message},
        {"isDryRun()", message.isDryRun()},
        {"stack", message.stack},
        {"duration", message.duration},
        {"log", message.log},
        {"testCase", message.testCase},
        {"testName", message.testName},
    };
    String s = "";
    for (Object[] a : args) {
      s += a[0] + ": " + (a[1] == null ? a[1] : a[1].toString()) + ",\t";
    }
    return s;
  }
  @Override
  public void writeObject(Object obj) throws IOException {
    if (obj instanceof TestResultProtocolMessage) {
      TestResultProtocolMessage message = (TestResultProtocolMessage) obj;
      System.out.println(messageToString(message));
    } else if (obj instanceof JstdTestRunnerFailure) {
      JstdTestRunnerFailure failure = (JstdTestRunnerFailure) obj;
      System.out.println("JstdTestRunnerFailure: " + failure.getFailureType() + ", " + failure.getMessage());
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
