package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: Maxim
 * Date: 04.09.2010
 * Time: 23:38:08
 */
public class ProfilingManager {
  private static final Logger LOG = Logger.getInstance(ProfilingManager.class.getName());
  private int myPort;
  private ProfilingConnection myConnection;

  private final LinkedBlockingQueue<Runnable> myAsyncExecutionQueue = new LinkedBlockingQueue<Runnable>();

  public ProfilingManager(int port) {
    myPort = port;
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      public void run() {
        try {
          while (true) {
            myAsyncExecutionQueue.take().run();
          }
        } catch (InterruptedException e) {
        }
      }
    });
  }

  public void initializeProfiling(final ProfilerDataConsumer sampleProcessor, final Callback ioExceptionProcessor) {
    myAsyncExecutionQueue.offer(new Runnable() {
      public void run() {
        myConnection = new ProfilingConnection(myPort, sampleProcessor, ioExceptionProcessor);
        myConnection.connect();
      }
    });
  }

  public interface Callback extends ProfilingConnection.Callback {
  }

  public void stopCpuProfiling(final Callback finished) {
    myAsyncExecutionQueue.offer(new Runnable() {
      public void run() {
        try {
          myConnection.stopCpuProfiling(finished);
        } catch (IOException ex) {
          finished.finished(null, ex);
        }
      }
    });
  }

  public void startCpuProfiling(final Callback finished) {
    myAsyncExecutionQueue.offer(new Runnable() {
      public void run() {
        try {
          myConnection.startCpuProfiling(finished);
        } catch (IOException ex) {
          finished.finished(null, ex);
        }
      }
    });
  }

  public void captureMemorySnapshot(final Callback finished) {
    myAsyncExecutionQueue.offer(new Runnable() {
      public void run() {
        try {
          myConnection.captureMemorySnapshot(finished);
        } catch (IOException ex) {
          finished.finished(null, ex);
        }
      }
    });
  }

  public void doGc(final Callback finished) {
    myAsyncExecutionQueue.offer(new Runnable() {
      public void run() {
        try {
          myConnection.doGc(finished);
        } catch (IOException e) {
          finished.finished(null, e);
        }
      }
    });
  }

  public void dispose() {
    try {
      myConnection.dispose();
    } catch (IOException ex) {
      LOG.warn(ex);
    }
  }
}
