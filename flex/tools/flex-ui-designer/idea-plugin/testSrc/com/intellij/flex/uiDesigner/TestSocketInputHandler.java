package com.intellij.flex.uiDesigner;

import com.intellij.openapi.util.ActionCallback;

import java.io.IOException;
import java.io.PrintStream;

class TestSocketInputHandler extends SocketInputHandlerImpl {
  private String expectedError;
  private MessageHandler customMessageHandler;

  TestSocketInputHandler() {
    super();
  }

  private static class TestServerMethod {
    private static final int custom = 102;
  }

  public Reader getReader() {
    return reader;
  }

  public void setExpectedErrorMessage(String message) {
    expectedError = message;
  }

  @Override
  protected boolean processOnRead() {
    return false;
  }

  public void process(MessageHandler customMessageHandler) throws IOException {
    assert !processOnRead();

    this.customMessageHandler = customMessageHandler;
    process();
    if (this.customMessageHandler != null) {
      throw new AssertionError("customMessageHandler must be null");
    }
  }

  public void processUntil(ActionCallback callback) throws IOException {
    while (true) {
      final int command = reader.read();
      if (command == -1) {
        break;
      }

      processCommandAndNotifyFileBased(command);
      if (callback.isProcessed()) {
        break;
      }
    }
  }

  @Override
  protected void processCommand(int command) throws IOException {
    if (isFileBased(command) ||
      command == ServerMethod.SAVE_PROJECT_WINDOW_BOUNDS ||
      command == ServerMethod.CALLBACK ||
      command == ServerMethod.UNREGISTER_LIBRARY_SETS ||
      command == ServerMethod.LOG_WARNING) {
      super.processCommand(command);
      return;
    }

    switch (command) {
      case ServerMethod.SHOW_ERROR:
        final String errorMessage = reader.readUTF();
        if (expectedError == null) {
          throw new AssertionError(errorMessage) {
            @Override
            public void printStackTrace(PrintStream s) {
            }
          };
        }
        else {
          if (!errorMessage.startsWith(expectedError)) {
            throw new AssertionError("Expected error message " + expectedError + ", but got " + errorMessage) {
              @Override
              public void printStackTrace(PrintStream s) {
              }
            };
          }
          expectedError = null;
        }
        break;

      default:
        if (customMessageHandler != null && customMessageHandler.getExpectedCommand() == command) {
          customMessageHandler.process();
          customMessageHandler = null;
        }
        else {
          throw new IllegalStateException("Unexpected server command: " + command);
        }
    }
  }

  public interface MessageHandler {
    void process() throws IOException;
    int getExpectedCommand();
  }

  public abstract static class CustomMessageHandler implements MessageHandler {
    @Override
    public int getExpectedCommand() {
      return TestServerMethod.custom;
    }
  }
}
