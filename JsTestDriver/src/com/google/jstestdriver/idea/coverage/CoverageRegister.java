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
  private static final String COVERAGE_ENGINE_EP_NAME = "com.intellij.coverageEngine";
  private static final String COVERAGE_RUNNER_EP_NAME = "com.intellij.coverageRunner";
  private static final String[] EXTENSION_POINT_NAMES = {
    COVERAGE_ENGINE_EP_NAME, COVERAGE_RUNNER_EP_NAME
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
    try {
      for (String extensionPointName : EXTENSION_POINT_NAMES) {
        Extensions.getRootArea().getExtensionPoint(extensionPointName);
      }
    } catch (Exception e) {
      return false;
    }
    return true;
  }

  private static void registerCoverage() {
    RunnerRegistry runnerRegistry = RunnerRegistry.getInstance();
    if (runnerRegistry instanceof RunnerRegistryImpl) {
      ((RunnerRegistryImpl) runnerRegistry).registerRunner(PROGRAM_RUNNER);
    }
    registerExtension(COVERAGE_ENGINE_EP_NAME, new JstdCoverageEngine());
    registerExtension(COVERAGE_RUNNER_EP_NAME, new JstdCoverageRunner());
  }

  @SuppressWarnings("unchecked")
  private static void registerExtension(String extensionPointName, Object extension) {
    ExtensionPoint extensionPoint = Extensions.getRootArea().getExtensionPoint(extensionPointName);
    extensionPoint.registerExtension(extension);
  }

}
