package com.jetbrains.actionscript.profiler.model;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class ProfilingManager {
  private static final Logger LOG = Logger.getInstance(ProfilingManager.class.getName());
  private final int myPort;
  private ProfilingConnection myConnection;

  private final LinkedBlockingQueue<Runnable> myAsyncExecutionQueue = new LinkedBlockingQueue<>();

  public ProfilingManager(int port) {
    myPort = port;
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      try {
        while (true) {
          myAsyncExecutionQueue.take().run();
        }
      }
      catch (InterruptedException e) {
      }
    });
  }

  public void initializeProfiling(final ProfilerDataConsumer sampleProcessor, final Callback ioExceptionProcessor) {
    myAsyncExecutionQueue.offer(() -> {
      myConnection = new ProfilingConnection(myPort, sampleProcessor, ioExceptionProcessor);
      myConnection.connect();
    });
  }

  public interface Callback extends ProfilingConnection.Callback {
  }

  public void startCollectingLiveObjects(final Callback finished) {
    myAsyncExecutionQueue.offer(() -> {
      try {
        myConnection.startCollectingLiveObjects(finished);
      }
      catch (IOException e) {
        finished.finished(null, e);
      }
    });
  }

  public void stopCollectingLiveObjects(final Callback finished) {
      myAsyncExecutionQueue.offer(() -> {
        try {
          myConnection.stopCollectingLiveObjects(finished);
        }
        catch (IOException e) {
          finished.finished(null, e);
        }
      });
    }

  public void stopCpuProfiling(final Callback finished) {
    myAsyncExecutionQueue.offer(() -> {
      try {
        myConnection.stopCpuProfiling(finished);
      } catch (IOException ex) {
        finished.finished(null, ex);
      }
    });
  }

  public void startCpuProfiling(final Callback finished) {
    myAsyncExecutionQueue.offer(() -> {
      try {
        myConnection.startCpuProfiling(finished);
      } catch (IOException ex) {
        finished.finished(null, ex);
      }
    });
  }

  public void doGc(final Callback finished) {
    myAsyncExecutionQueue.offer(() -> {
      try {
        myConnection.doGc(finished);
      } catch (IOException e) {
        finished.finished(null, e);
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
