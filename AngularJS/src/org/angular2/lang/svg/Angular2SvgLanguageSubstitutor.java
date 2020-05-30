// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.svg;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2SvgLanguageSubstitutor extends LanguageSubstitutor {

  @Override
  public @Nullable Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    if (Angular2LangUtil.isAngular2Context(project, file)) {
      return Angular2SvgLanguage.INSTANCE;
    }
    return null;
  }
}
