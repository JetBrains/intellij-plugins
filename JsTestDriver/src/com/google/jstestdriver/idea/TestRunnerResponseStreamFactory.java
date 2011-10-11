package com.google.jstestdriver.idea;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.jstestdriver.*;
import com.google.jstestdriver.browser.BrowserPanicException;
import com.google.jstestdriver.idea.execution.tree.TestResultProtocolMessage;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.output.TestResultListener;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * Informs IDE about test's status changes.
 */
public class TestRunnerResponseStreamFactory implements ResponseStreamFactory {

  private static final ResponseStream NULL_RESPONSE_STREAM = new ResponseStream() {
    @Override
    public void stream(Response response) {}

    @Override
    public void finish() {}
  };

  private final ObjectOutput testResultProtocolMessageOutput;
  private final TestResultListener testResultListener;
  private final File jstdConfigFile;

  @Inject
  public TestRunnerResponseStreamFactory(
      @Named("testResultProtocolMessageOutput") ObjectOutput testResultProtocolMessageOutput,
      @Named("jstdConfigFile") File jstdConfigFile,
      TestResultListener testResultListener) {
    this.jstdConfigFile = jstdConfigFile;
    this.testResultProtocolMessageOutput = testResultProtocolMessageOutput;
    this.testResultListener = testResultListener;
  }

  @Override
  public ResponseStream getRunTestsActionResponseStream(String browserId) {
    final TestResultGenerator testResultGenerator = new TestResultGenerator(new FailureParser(new NullPathPrefix()));
    final Gson gson = new Gson();
    return new ResponseStream() {
      @Override
      public void stream(Response response) {
        Collection<TestResult> testResults = testResultGenerator.getTestResults(response);
        for (TestResult testResult : testResults) {
          writeTestResultProtocolMessage(TestResultProtocolMessage.fromTestResult(jstdConfigFile, testResult));
          switch(response.getResponseType()) {
            case TEST_RESULT:
                testResultListener.onTestComplete(testResult);
              break;
            case FILE_LOAD_RESULT:
              LoadedFiles files = gson.fromJson(response.getResponse(),
                  response.getGsonType());
              for (FileResult result : files.getLoadedFiles()) {
                testResultListener.onFileLoad(response.getBrowser().toString(), result);
              }
              break;
            case BROWSER_PANIC:
              BrowserPanic panic = gson.fromJson(response.getResponse(), response.getGsonType());
              throw new BrowserPanicException(panic.getBrowserInfo(), panic.getCause());
          }
        }
      }

      @Override
      public void finish() {
        testResultListener.finish();
      }
    };
  }

  @Override
  public ResponseStream getDryRunActionResponseStream() {
    final Gson gson = new Gson();
    return new ResponseStream() {

      @Override
      public void stream(Response response) {
        if (response.getResponseType() == Response.ResponseType.FILE_LOAD_RESULT) {
          // TODO process it?
//              new Gson().fromJson(response.getResponse(), LoadedFiles.class);
          return; // for now, don't send back to IDEA
        }
        BrowserInfo browser = response.getBrowser();
        DryRunInfo dryRunInfo = gson.fromJson(response.getResponse(), DryRunInfo.class);
        for (String testName : dryRunInfo.getTestNames()) {
          writeTestResultProtocolMessage(TestResultProtocolMessage.fromDryRun(jstdConfigFile, testName, browser));
        }
      }

      @Override
      public void finish() {
      }
    };
  }

  @Override
  public ResponseStream getEvalActionResponseStream() {
    return NULL_RESPONSE_STREAM;
  }

  @Override
  public ResponseStream getResetActionResponseStream() {
    return NULL_RESPONSE_STREAM;
  }

  private void writeTestResultProtocolMessage(TestResultProtocolMessage message) {
    try {
      synchronized (testResultProtocolMessageOutput) {
        testResultProtocolMessageOutput.writeObject(message);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
