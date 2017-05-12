/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.util.xmlb.annotations.Attribute;

public class DartProblemsViewSettings {

  public enum FileFilterMode {All, ContentRoot, DartPackage, Directory, File}

  static final boolean AUTO_SCROLL_TO_SOURCE_DEFAULT = false;
  static final boolean GROUP_BY_SEVERITY_DEFAULT = true;
  static final boolean SHOW_ERRORS_DEFAULT = true;
  static final boolean SHOW_WARNINGS_DEFAULT = true;
  static final boolean SHOW_HINTS_DEFAULT = true;
  static final FileFilterMode FILE_FILTER_MODE_DEFAULT = FileFilterMode.All;

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
}
