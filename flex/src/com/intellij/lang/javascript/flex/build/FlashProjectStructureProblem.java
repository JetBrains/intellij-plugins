package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.ui.*;
import org.jetbrains.annotations.Nullable;

public class FlashProjectStructureProblem {

  public final String errorMessage;
  public final String errorId;
  public final String tabName;
  public final @Nullable Object locationOnTab;

  private FlashProjectStructureProblem(String errorMessage, String errorId, String tabName, @Nullable Object locationOnTab) {
    this.errorMessage = errorMessage;
    this.errorId = errorId;
    this.tabName = tabName;
    this.locationOnTab = locationOnTab;
  }

  public static FlashProjectStructureProblem createGeneralOptionProblem(final String bcName,
                                                                        final String errorMessage,
                                                                        final FlexBCConfigurable.Location location) {
    return new FlashProjectStructureProblem(errorMessage, location.errorId, bcName, location);
  }

  public static FlashProjectStructureProblem createDependenciesProblem(final String errorMessage,
                                                                       final DependenciesConfigurable.Location location) {
    return new FlashProjectStructureProblem(errorMessage, location.errorId, DependenciesConfigurable.TAB_NAME, location);
  }

  public static FlashProjectStructureProblem createCompilerOptionsProblem(final String errorMessage,
                                                                          final CompilerOptionsConfigurable.Location location) {
    return new FlashProjectStructureProblem(errorMessage, location.errorId, CompilerOptionsConfigurable.TAB_NAME, location);
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
    return new FlashProjectStructureProblem(errorMessage, location.errorId, tabName, location);
  }

  public static class FlexUnitOutputFolderProblem extends FlashProjectStructureProblem {

    public static final FlexUnitOutputFolderProblem INSTANCE = new FlexUnitOutputFolderProblem();

    private FlexUnitOutputFolderProblem() {
      super(FlexBundle.message("flexunit.output.folder.not.set"), "project-output", "", null);
    }
  }
}
