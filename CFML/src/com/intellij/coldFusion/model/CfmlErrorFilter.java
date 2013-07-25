/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package com.intellij.coldFusion.model;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 * Date: 4/23/12
 */

import com.intellij.codeInsight.highlighting.HighlightErrorFilter;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiErrorElement;
import com.intellij.sql.psi.SqlLanguage;
import org.jetbrains.annotations.NotNull;


public class CfmlErrorFilter extends HighlightErrorFilter {

  public boolean shouldHighlightErrorElement(@NotNull final PsiErrorElement element) {
    VirtualFile vFile = element.getContainingFile() != null ? element.getContainingFile().getVirtualFile() : null;
    if (!(vFile != null && vFile.getFileType() instanceof CfmlFileType)) {
      return true;
    }
    final Language language = element.getParent().getLanguage();
    if (language == CfmlLanguage.INSTANCE || language == HTMLLanguage.INSTANCE || language == SqlLanguage.INSTANCE) return true;
    return false;
  }
}
