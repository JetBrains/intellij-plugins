package com.jetbrains.maya;

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * Process handler associated with socket.
 * Reads socket output until ascii 26(EOF) is not reached.
 * Then processes is treated as terminated.
 *
 * @author traff
 */
class SocketProcessHandler extends ProcessHandler {
  @NotNull private final String myCommandLine;
  private final Socket mySocket;
  private SocketOutputReader myOutputReader;

  SocketProcessHandler(Socket socket, @NotNull String commandLine) {
    mySocket = socket;
    myCommandLine = commandLine;
  }

  @Override
  public void startNotify() {
    notifyTextAvailable(myCommandLine + '\n', ProcessOutputTypes.SYSTEM);

    addProcessListener(new ProcessAdapter() {
      @Override
      public void startNotified(@NotNull final ProcessEvent event) {
        try {
          myOutputReader = new SocketOutputReader(mySocket.getInputStream());
        }
        catch (Exception e) {
          //pass
        }
        finally {
          removeProcessListener(this);
        }
      }
    });

    super.startNotify();
  }

  @Override
  public boolean isProcessTerminated() {
    return mySocket.isClosed();
  }

  @Override
  protected void destroyProcessImpl() {
    try {
      mySocket.close();
      myOutputReader.stop();
    }
    catch (Exception e) {
      //pass
    }
  }

  @Override
  protected void detachProcessImpl() {
    destroyProcess();
  }

  @Override
  public boolean detachIsDefault() {
    return false;
  }

  @Nullable
  @Override
  public OutputStream getProcessInput() {
    return null;
  }

  private class SocketOutputReader extends BaseOutputReader {
    public SocketOutputReader(InputStream inputStream) {
      super(inputStream, null);
      start(myCommandLine);
    }

    @NotNull
    @Override
    protected Future<?> executeOnPooledThread(@NotNull Runnable runnable) {
      return ApplicationManager.getApplication().executeOnPooledThread(runnable);
    }

    @Override
    protected void onTextAvailable(@NotNull String text) {
      if (text.equals("\u0000\u001A\n")) {
        destroyProcess();
      }
      else {
        notifyTextAvailable(text, ProcessOutputTypes.STDOUT);
      }
    }
  }
}
