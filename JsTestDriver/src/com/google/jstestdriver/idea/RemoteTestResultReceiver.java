package com.google.jstestdriver.idea;

import com.google.jstestdriver.idea.execution.tree.JstdTestRunnerFailure;
import com.google.jstestdriver.idea.execution.tree.RemoteTestListener;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import com.google.jstestdriver.idea.util.CastUtils;
import com.intellij.openapi.application.ApplicationManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

/**
 * The IDE end of the socket communication from the TestRunner. Should be run in a background thread. When data is
 * available on the socket, it will be read and trigger an event on the RemoteTestListener (on the AWT event thread),
 * passing the deserialized test result.
 *
 * @author alexeagle@google.com (Alex Eagle)
 */
public class RemoteTestResultReceiver implements Runnable {
  private final RemoteTestListener listener;
  private final int port;
  private final CountDownLatch serverStarted;

  public RemoteTestResultReceiver(RemoteTestListener listener, int port, CountDownLatch serverStarted) {
    this.listener = listener;
    this.port = port;
    this.serverStarted = serverStarted;
  }

  /**
   * Create a socket, and read all the TestResultProtocolMessage objects which the TestRunner process has written to us.
   * When we reach the end of the communication, notify the listener that it may shutdown.
   */
  @Override public void run() {
    Socket client = null;
    Exception savedException = null;
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(port);
      serverStarted.countDown();
      client = serverSocket.accept();
      ObjectInputStream in = new ObjectInputStream(client.getInputStream());
      readTestResults(in);
    } catch (Exception e) {
      savedException = e;
    } finally {
      if (client != null) {
        try {
          // also closes client socket input stream
          client.close();
        } catch (Exception e) {
          if (savedException != null) {
            savedException = e;
          }
        }
      }
      if (serverSocket != null) {
        try {
          serverSocket.close();
        } catch (Exception e) {
          if (savedException != null) {
            savedException = e;
          }
        }
      }
    }
    if (savedException != null) {
      savedException.printStackTrace();
      throw new RuntimeException(savedException);
    }
  }


  private void readTestResults(ObjectInputStream in) throws IOException {
    while (true) {
      try {
        Object obj = in.readObject();
        final TestResultProtocolMessage message = CastUtils.tryCast(obj, TestResultProtocolMessage.class);
        if (message != null) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              if (message.isDryRun()) {
                listener.onTestStarted(message);
              } else {
                listener.onTestFinished(message);
              }
            }
          });
        }
        final JstdTestRunnerFailure testRunnerFailure = CastUtils.tryCast(obj, JstdTestRunnerFailure.class);
        if (testRunnerFailure != null) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              listener.onTestRunnerFailed(testRunnerFailure);
            }
          });
        }
      } catch (EOFException e) {
        break;
      } catch (Exception e) {
        throw new RuntimeException("Problem in communication with TestRunner process", e);
      }
    }
  }
}
