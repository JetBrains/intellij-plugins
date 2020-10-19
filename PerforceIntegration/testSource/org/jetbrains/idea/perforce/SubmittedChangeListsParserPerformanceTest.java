package org.jetbrains.idea.perforce;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.idea.perforce.perforce.OutputMessageParser;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class SubmittedChangeListsParserPerformanceTest {
  @Test
  public void test() throws IOException {
    File testData = new File(PathManager.getHomePath() + "/plugins/PerforceIntegration/testData/changes.txt");
    final String output = FileUtil.loadFile(testData);
    final long start = System.currentTimeMillis();
    OutputMessageParser.processChangesOutput(output);
    final long executionTime = System.currentTimeMillis() - start;
    assertTrue("Execution time: " + executionTime, executionTime < 30000);
  }
}
