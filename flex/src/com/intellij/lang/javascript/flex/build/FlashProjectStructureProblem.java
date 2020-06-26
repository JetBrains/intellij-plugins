// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.AirDesktopPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.AndroidPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.ui.*;
import com.intellij.openapi.roots.ui.configuration.projectRoot.daemon.ProjectStructureProblemType;
import org.jetbrains.annotations.Nullable;

public class FlashProjectStructureProblem {

  public final ProjectStructureProblemType.Severity severity;
  public final String errorMessage;
  public final String errorId;
  public final String tabName;
  public final @Nullable Object locationOnTab;

  private FlashProjectStructureProblem(final ProjectStructureProblemType.Severity severity,
                                       final String errorMessage,
                                       final String errorId,
                                       final String tabName,
                                       final @Nullable Object locationOnTab) {
    this.severity = severity;
    this.errorMessage = errorMessage;
    this.errorId = errorId;
    this.tabName = tabName;
    this.locationOnTab = locationOnTab;
  }

  public static FlashProjectStructureProblem createGeneralOptionProblem(final ProjectStructureProblemType.Severity severity,
                                                                        final String bcName,
                                                                        final String errorMessage,
                                                                        final FlexBCConfigurable.Location location) {
    return new FlashProjectStructureProblem(severity, errorMessage, location.errorId, bcName, location);
  }

  public static FlashProjectStructureProblem createDependenciesProblem(final ProjectStructureProblemType.Severity severity,
                                                                       final String errorMessage,
                                                                       final DependenciesConfigurable.Location location) {
    return new FlashProjectStructureProblem(severity, errorMessage, location.errorId, DependenciesConfigurable.getTabName(), location);
  }

  public static FlashProjectStructureProblem createCompilerOptionsProblem(final ProjectStructureProblemType.Severity severity,
                                                                          final String errorMessage,
                                                                          final CompilerOptionsConfigurable.Location location) {
    return new FlashProjectStructureProblem(severity, errorMessage, location.errorId, CompilerOptionsConfigurable.getTabName(), location);
  }

  public static FlashProjectStructureProblem createPackagingOptionsProblem(final ProjectStructureProblemType.Severity severity,
                                                                           final AirPackagingOptions packagingOptions,
                                                                           final String errorMessage,
                                                                           final AirPackagingConfigurableBase.Location location) {
    final String tabName = packagingOptions instanceof AirDesktopPackagingOptions
                           ? AirDesktopPackagingConfigurable.getTabName()
                           : packagingOptions instanceof AndroidPackagingOptions
                             ? AndroidPackagingConfigurable.getTabName()
                             : packagingOptions instanceof IosPackagingOptions
                               ? IOSPackagingConfigurable.getTabName() :
                               null;
    assert tabName != null : packagingOptions;
    return new FlashProjectStructureProblem(severity, errorMessage, location.errorId, tabName, location);
  }

  public static final class FlexUnitOutputFolderProblem extends FlashProjectStructureProblem {

    public static final FlexUnitOutputFolderProblem INSTANCE = new FlexUnitOutputFolderProblem();

    private FlexUnitOutputFolderProblem() {
      super(ProjectStructureProblemType.Severity.ERROR, FlexBundle.message("flexunit.output.folder.not.set"), "project-output", "", null);
    }
  }
}
