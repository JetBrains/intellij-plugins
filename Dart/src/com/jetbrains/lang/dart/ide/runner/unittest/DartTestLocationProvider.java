package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.Location;
import com.intellij.openapi.project.Project;
import com.intellij.testIntegration.TestLocationProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartTestLocationProvider implements TestLocationProvider {
  @NotNull
  @Override
  public List<Location> getLocation(@NotNull String protocolId, @NotNull String locationData, Project project) {
    return null;
  }
}
