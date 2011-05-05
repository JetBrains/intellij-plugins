package com.intellij.flex.uiDesigner;

import com.intellij.util.concurrency.Semaphore;

import java.io.IOException;

class TestSocketInputHandler extends SocketInputHandlerImpl {
  public final Semaphore semaphore = new Semaphore();
  private String expectedError;

  private boolean waitingResult;
  private String failedMessage;

  private static class TestServerMethod {
    private static final int success = 100;
    private static final int fail = 101;
  }

  public void setExpectedErrorMessage(String message) {
    expectedError = message;
  }

  public void waitResult() {
    semaphore.down();
    waitingResult = true;
    semaphore.waitFor();
  }

  public boolean isPassed() {
    return failedMessage == null;
  }

  public String getAndClearFailedMessage() {
    String s = failedMessage;
    failedMessage = null;
    return s;
  }

  @Override
  protected void processCommand(int command) throws IOException {
    if (isFileBased(command)) {
      super.processCommand(command);
    }
    else {
      if (waitingResult) {
        if (expectedError == null) {
          waitingResult = false;
        }
      }
      else if (command != ServerMethod.showError) {
        throw new IllegalStateException("Unexpected server command " + command + ", result is not waiting");
      }

      switch (command) {
        case ServerMethod.showError:
          final String errorMessage = reader.readUTF();
          if (expectedError == null) {
            failedMessage = errorMessage;
          }
          else {
            if (!errorMessage.startsWith(expectedError)) {
              failedMessage = "Expected error message " + expectedError + ", but got " + errorMessage;
            }
            expectedError = null;
          }
          break;

        case TestServerMethod.fail:
          failedMessage = reader.readUTF();
          break;

        case TestServerMethod.success:
          String message = reader.readUTF();
          if (!message.equals("__passed__")) {
            failedMessage = message;
          }
          break;

        default:
          throw new IllegalStateException("Unexpected server command: " + command);
      }

      if (!waitingResult) {
        semaphore.up();
      }
    }
  }
}
