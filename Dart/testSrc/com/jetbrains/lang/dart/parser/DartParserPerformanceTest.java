package com.jetbrains.lang.dart.parser;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.ParsingTestCase;
import com.intellij.testFramework.SkipSlowTestLocally;
import com.intellij.tools.ide.metrics.benchmark.Benchmark;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartParserDefinition;
import com.jetbrains.lang.dart.util.DartTestUtils;

@SkipSlowTestLocally
public class DartParserPerformanceTest extends ParsingTestCase {

  public DartParserPerformanceTest() {
    super("parsing", DartFileType.DEFAULT_EXTENSION, new DartParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH;
  }

  public void testMissingOpenQuoteInMapPerformance() {
    String dartCode = """
      void main() {
        list.add({"id": 1, "param0": 1, "param1": "001", "param2": 50, "param3": 5, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 2, "param0": 1, "param1": "001", "param2": 60, "param3": 4, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 3, "param0": 1, "param1": "001", "param2": 70, "param3": 3, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 4, "param0": 1, "param1": "001", "param2": 75, "param3": 3, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 4, "param0": 1, "param1": "001", "param2": 75, "param3": 3, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 4, "param0": 1, "param1": "001", "param2": 75, "param3": 3, "param4": 1, param5": 0, "param6": 1});
        list.add({"id": 4, "param0": 1, "param1": "001", "param2": 75, "param3": 3, "param4": 1, param5": 0, "param6": 1});
      }
      """;

    Benchmark.newBenchmark(getTestName(true), () -> {
        PsiFile file = createPsiFile("test.dart", dartCode);
        ensureParsed(file);
      })
      .attempts(20)
      .start();
  }
}
