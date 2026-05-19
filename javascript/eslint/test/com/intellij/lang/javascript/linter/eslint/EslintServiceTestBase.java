package com.intellij.lang.javascript.linter.eslint;

import com.intellij.lang.javascript.linter.LinterHighlightingTest;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.registry.RegistryValue;

import java.io.File;

public abstract class EslintServiceTestBase extends LinterHighlightingTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    RegistryValue registryValue = Registry.get("eslint.service.node.path");
    registryValue.setValue(new File(getNodePackage().getSystemDependentPath()).getParent(), getTestRootDisposable());
  }
}
