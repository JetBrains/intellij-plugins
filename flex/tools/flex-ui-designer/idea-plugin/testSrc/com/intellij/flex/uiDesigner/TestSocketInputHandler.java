package com.intellij.flex.uiDesigner;

import java.io.IOException;

class TestSocketInputHandler extends SocketInputHandlerImpl {
  private String expectedError;
  private MessageHandler customMessageHandler;

  TestSocketInputHandler() {
    super();
  }

  protected static class TestServerMethod {
    private static final int success = 100;
    private static final int fail = 101;
    private static final int custom = 102;
  }

  public Reader getReader() {
    return reader;
  }

  public void setExpectedErrorMessage(String message) {
    expectedError = message;
  }

  public void process(MessageHandler customMessageHandler) throws IOException {
    this.customMessageHandler = customMessageHandler;
    process();
    if (this.customMessageHandler != null) {
      throw new AssertionError("customMessageHandler must be null");
    }
  }

  @Override
  protected boolean processCommand(int command) throws IOException {
    if (isFileBased(command) || command == ServerMethod.SAVE_PROJECT_WINDOW_BOUNDS || command == ServerMethod.DOCUMENT_OPENED) {
      return super.processCommand(command);
    }

    switch (command) {
      case ServerMethod.SHOW_ERROR:
        final String errorMessage = reader.readUTF();
        if (expectedError == null) {
          throw new IOException(errorMessage);
        }
        else {
          if (!errorMessage.startsWith(expectedError)) {
            throw new IOException("Expected error message " + expectedError + ", but got " + errorMessage);
          }
          expectedError = null;
        }
        break;

      case TestServerMethod.fail:
        throw new IOException(reader.readUTF());

      case TestServerMethod.success:
        String message = reader.readUTF();
        if (message.equals("__passed__")) {
          return false;
        }
        else {
          throw new IOException(message);
        }
        
      case ServerMethod.LOG_WARNING:
        LOG.warn(reader.readUTF());
        break;

      default:
        if (customMessageHandler != null && customMessageHandler.getExpectedCommand() == command) {
          customMessageHandler.process();
          customMessageHandler = null;
          return false;
        }
        else {
          throw new IllegalStateException("Unexpected server command: " + command);
        }
    }

    return true;
  }

  public static interface MessageHandler {
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
