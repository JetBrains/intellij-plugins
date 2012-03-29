package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.ui.*;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.Nullable;

public class FlashProjectStructureProblem {

  public final String errorMessage;
  public final String errorId;
  public final String tabName;
  public final @Nullable Pair<String, ?> location;

  private FlashProjectStructureProblem(String errorMessage, String errorId, String tabName, @Nullable Pair<String, ?> location) {
    this.errorMessage = errorMessage;
    this.errorId = errorId;
    this.tabName = tabName;
    this.location = location;
  }

  public static FlashProjectStructureProblem createGeneralOptionProblem(final String bcName,
                                                                        final String errorMessage,
                                                                        final FlexIdeBCConfigurable.Location location) {
    final Pair<String, Object> locationPair = Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, location);
    return new FlashProjectStructureProblem(errorMessage, location.errorId, bcName, locationPair);
  }

  public static FlashProjectStructureProblem createDependenciesProblem(final String errorMessage,
                                                                       final DependenciesConfigurable.Location location) {
    final Pair<String, Object> locationPair = Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, location);
    return new FlashProjectStructureProblem(errorMessage, location.errorId, DependenciesConfigurable.TAB_NAME, locationPair);
  }

  public static FlashProjectStructureProblem createCompilerOptionsProblem(final String errorMessage,
                                                                          final CompilerOptionsConfigurable.Location location) {
    final Pair<String, Object> locationPair = Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, location);
    return new FlashProjectStructureProblem(errorMessage, location.errorId, CompilerOptionsConfigurable.TAB_NAME, locationPair);
  }

  public static FlashProjectStructureProblem createPackagingOptionsProblem(final AirPackagingOptions packagingOptions,
                                                                           final String errorMessage,
                                                                           final AirPackagingConfigurableBase.Location location) {
    final String tabName = packagingOptions instanceof AirDesktopPackagingOptions
                           ? AirDesktopPackagingConfigurable.TAB_NAME
                           : packagingOptions instanceof AndroidPackagingOptions
                             ? AndroidPackagingConfigurable.TAB_NAME
                             : packagingOptions instanceof IosPackagingOptions
                               ? IOSPackagingConfigurable.TAB_NAME :
                               null;
    assert tabName != null : packagingOptions;
    final Pair<String, Object> locationPair = Pair.<String, Object>create(FlexIdeBCConfigurable.LOCATION_ON_TAB, location);
    return new FlashProjectStructureProblem(errorMessage, location.errorId, tabName, locationPair);
  }
}
