package com.google.jstestdriver.idea.coverage;

import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.impl.RunnerRegistryImpl;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.extensions.ExtensionPoint;
import com.intellij.openapi.extensions.Extensions;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class CoverageRegister implements ApplicationComponent {

  private static final JstdCoverageProgramRunner PROGRAM_RUNNER = new JstdCoverageProgramRunner();
  private static final String[] REQUIRED_CLASSES = {
    "com.intellij.coverage.CoverageEngine",
    "com.intellij.coverage.CoverageRunner",
  };

  @Override
  public void initComponent() {
    if (isCoreCoverageAvailable()) {
      registerCoverage();
    }
  }

  @Override
  public void disposeComponent() {
  }

  @NotNull
  @Override
  public String getComponentName() {
    return CoverageRegister.class.getSimpleName();
  }

  private static boolean isCoreCoverageAvailable() {
    for (String aClass : REQUIRED_CLASSES) {
      try {
        Class.forName(aClass);
      }
      catch (ClassNotFoundException e) {
        return false;
      }
    }
    return true;
  }

  private static void registerCoverage() {
    RunnerRegistry runnerRegistry = RunnerRegistry.getInstance();
    if (runnerRegistry instanceof RunnerRegistryImpl) {
      ((RunnerRegistryImpl) runnerRegistry).registerRunner(PROGRAM_RUNNER);
    }
    registerExtension("com.intellij.coverageEngine", new JstdCoverageEngine());
    registerExtension("com.intellij.coverageRunner", new JstdCoverageRunner());
  }

  @SuppressWarnings("unchecked")
  private static void registerExtension(String extensionPointName, Object extension) {
    ExtensionPoint extensionPoint = Extensions.getRootArea().getExtensionPoint(extensionPointName);
    extensionPoint.registerExtension(extension);
  }

}
