// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.util.xmlb.annotations.Attribute;

public class DartProblemsViewSettings {

  public enum FileFilterMode {All, ContentRoot, DartPackage, Directory, File}

  public enum ScopedAnalysisMode {All, DartPackage}

  static final boolean AUTO_SCROLL_TO_SOURCE_DEFAULT = false;
  static final boolean GROUP_BY_SEVERITY_DEFAULT = true;
  static final boolean SHOW_ERRORS_DEFAULT = true;
  static final boolean SHOW_WARNINGS_DEFAULT = true;
  static final boolean SHOW_HINTS_DEFAULT = true;
  static final FileFilterMode FILE_FILTER_MODE_DEFAULT = FileFilterMode.All;
  static final ScopedAnalysisMode SCOPED_ANALYSIS_MODE_DEFAULT = ScopedAnalysisMode.All;

  @Attribute(value = "auto-scroll-to-source")
  public boolean autoScrollToSource = AUTO_SCROLL_TO_SOURCE_DEFAULT;

  @Attribute(value = "group-by-severity")
  public boolean groupBySeverity = GROUP_BY_SEVERITY_DEFAULT;

  @Attribute(value = "file-filter-mode")
  public FileFilterMode fileFilterMode = FILE_FILTER_MODE_DEFAULT;

  @Attribute(value = "show-errors")
  public boolean showErrors = SHOW_ERRORS_DEFAULT;
  @Attribute(value = "show-warnings")
  public boolean showWarnings = SHOW_WARNINGS_DEFAULT;
  @Attribute(value = "show-hints")
  public boolean showHints = SHOW_HINTS_DEFAULT;

  @Attribute(value = "scoped-analysis-mode")
  public ScopedAnalysisMode scopedAnalysisMode = SCOPED_ANALYSIS_MODE_DEFAULT;
}
