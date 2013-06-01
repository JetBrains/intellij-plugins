package com.jetbrains.lang.dart.analyzer;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * @author: Fedor.Korotkov
 */
abstract public class AnalyzerMessageParsingTest extends CodeInsightFixtureTestCase {
  @Override
  protected String getBasePath() {
    return FileUtil.toSystemDependentName("/plugins/Dart/testData/analyzer/parsing");
  }

  public void doTest() throws Throwable {
    final String name = getTestName(true);
    final String[] lines = StringUtil.splitByLines(FileUtil.loadFile(new File(getTestDataPath(), name + ".stdout.txt")));
    final List<AnalyzerMessage> messageList = AnalyzerMessage.parseMessages(Arrays.asList(lines), "unused");

    final File expected = new File(getTestDataPath(), name + ".txt");
    if (!expected.exists()) {
      final PrintWriter out = new PrintWriter(new FileOutputStream(expected));
      try {
        for (AnalyzerMessage message : messageList) {
          out.println(message.toString());
        }
      }
      finally {
        out.close();
      }
      assertNotNull("Expected file not found. Create: " + expected.getAbsolutePath(), null);
    }
    final Scanner in = new Scanner(expected);
    int i = 0;
    for (int size = messageList.size(); i < size; i++) {
      AnalyzerMessage message = messageList.get(i);
      assertEquals(in.nextLine(), message.toString());
    }
    assertEquals(messageList.size(), i);
  }

  public void testAnalyzer1() throws Throwable {
    doTest();
  }
}
