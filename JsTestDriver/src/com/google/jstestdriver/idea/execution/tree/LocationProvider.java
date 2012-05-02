package com.google.jstestdriver.idea.execution.tree;

import com.intellij.execution.Location;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated
abstract class LocationProvider {

  @Nullable
  abstract Location provideLocation(@NotNull Project project);

  public static LocationProvider EMPTY = new LocationProvider() {
    @Override
    public Location provideLocation(@NotNull Project project) {
      return null;
    }
  };

  public static LocationProvider createConstantProvider(final Location location) {
    if (location == null) {
      return EMPTY;
    }
    return new LocationProvider() {
      @Override
      @Nullable
      Location provideLocation(@NotNull Project project) {
        return location;
      }
    };
  }
}
