package org.jetbrains.idea.perforce;

import com.intellij.idea.IgnoreJUnit3;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.PerformanceUnitTest;
import org.jetbrains.idea.perforce.perforce.OutputMessageParser;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

@PerformanceUnitTest
public class SubmittedChangeListsParserPerformanceTest {
  @Test
  @Ignore("https://youtrack.jetbrains.com/issue/IJPL-237679/Failing-test-org.jetbrains.idea.perforce.SubmittedChangeListsParserPerformanceTest.test")
  public void test() throws IOException {
    File testData = new File(PathManager.getHomePath() + "/plugins/PerforceIntegration/testData/changes.txt");
    final String output = FileUtil.loadFile(testData);
    final long start = System.currentTimeMillis();
    OutputMessageParser.processChangesOutput(output);
    final long executionTime = System.currentTimeMillis() - start;
    assertTrue("Execution time: " + executionTime, executionTime < 30000);
  }
}
