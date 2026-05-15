package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.version.JSHintVersionUtil;
import com.intellij.lang.javascript.linter.rhino.FunctionWithScope;
import com.intellij.lang.javascript.linter.rhino.RhinoFunctionManager;
import org.junit.Test;

import java.io.IOException;

public class RhinoCompilationTest {


  private static FunctionWithScope check(String version) throws IOException {
    final String content = JSHintVersionUtil.loadSourceContentFromLocalDrive(version);
    RhinoFunctionManager manager = new RhinoFunctionManager(
      () -> content,
      "JSHINT",
      version
    );
    return manager.getFunctionWithScope();
  }


  @Test
  public void testCurrentVersion() throws Exception {
    check(JSHintVersionUtil.BUNDLED_VERSION);
  }

}
