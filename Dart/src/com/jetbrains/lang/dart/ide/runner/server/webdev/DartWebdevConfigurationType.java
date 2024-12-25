// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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

public final class DartWebdevConfigurationType extends ConfigurationTypeBase implements DumbAware {
  public static @NotNull DartWebdevConfigurationType getInstance() {
    return ConfigurationTypeUtil.findConfigurationType(DartWebdevConfigurationType.class);
  }

  public DartWebdevConfigurationType() {
    super("DartWebdevConfigurationType",
          DartBundle.message("webdev.debug.configuration.name"),
          DartBundle.message("webdev.debug.configuration.description"),
          NotNullLazyValue.createValue(() -> DartIcons.DartWeb));
    addFactory(new ConfigurationFactory(this) {
      @Override
      public @NotNull RunConfiguration createTemplateConfiguration(final @NotNull Project project) {
        return new DartWebdevConfiguration(project, this, "Dart Web");
      }

      @Override
      public boolean isApplicable(final @NotNull Project project) {
        return FileTypeIndex.containsFileOfType(DartFileType.INSTANCE, GlobalSearchScope.projectScope(project)) &&
               FileTypeIndex.containsFileOfType(HtmlFileType.INSTANCE, GlobalSearchScope.projectScope(project));
      }

      @Override
      public @NotNull String getId() {
        return "Dart Web";
      }
    });
  }

  @Override
  public String getHelpTopic() {
    return "reference.dialogs.rundebug.DartWebdevConfigurationType";
  }
}
