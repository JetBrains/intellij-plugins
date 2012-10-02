package com.intellij.flex.compiler.flex3;

import com.intellij.flex.compiler.SdkSpecificHandler;
import flex2.compiler.API;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.Compc;

public class Flex3Handler extends SdkSpecificHandler {

  public void cleanThreadLocals() {
    API.removePathResolver();
    ThreadLocalToolkit.setLogger(null);
    ThreadLocalToolkit.setLocalizationManager(null);

    super.cleanThreadLocals();
  }

  @Override
  public void compileSwf(String[] args) {
    flex2.tools.Compiler.mxmlc(args);
  }

  @Override
  public void compileSwc(String[] args) {
    Compc.compc(args);
  }
}
