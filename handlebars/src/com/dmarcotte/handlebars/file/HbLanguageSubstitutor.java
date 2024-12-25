// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.dmarcotte.handlebars.file;

import com.dmarcotte.handlebars.HbLanguage;
import com.dmarcotte.handlebars.config.HbConfig;
import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HbLanguageSubstitutor extends LanguageSubstitutor {
  @Override
  public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    if (file instanceof LightVirtualFile) {
      return null;
    }
    return HbConfig.shouldOpenHtmlAsHandlebars(project) &&
           FileTypeRegistry.getInstance().isFileOfType(file, HtmlFileType.INSTANCE) ? HbLanguage.INSTANCE : null;
  }
}
