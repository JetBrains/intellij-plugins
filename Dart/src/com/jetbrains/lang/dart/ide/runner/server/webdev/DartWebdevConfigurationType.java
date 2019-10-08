// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner.server.webdev;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartFileType;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;

public class DartWebdevConfigurationType extends ConfigurationTypeBase implements DumbAware {
  @NotNull
  public static DartWebdevConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartWebdevConfigurationType.class);
  }

  public DartWebdevConfigurationType() {
    super("DartRemoteDebugConfigurationType",
          DartBundle.message("webdev.debug.configuration.name"),
          DartBundle.message("webdev.debug.configuration.description"),
          NotNullLazyValue.createValue(() -> DartIcons.Dart_16));
    addFactory(new ConfigurationFactory(this) {
      @NotNull
      @Override
      public RunConfiguration createTemplateConfiguration(@NotNull final Project project) {
        return new DartWebdevConfiguration(project, DartWebdevConfigurationType.this, "Dart Web");
      }

      @Override
      public boolean isApplicable(@NotNull final Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project)) &&
               FileTypeIndex.containsFileOfType(HtmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.DartWebdevConfigurationType";
  }
}
