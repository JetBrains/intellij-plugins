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
package com.intellij.coldFusion;

import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.coldFusion.model.files.CfmlFileViewProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class CfmlTemplateContextType extends TemplateContextType {
  protected CfmlTemplateContextType() {
    super("ColdFusion", CfmlBundle.message("template.context.presentable.name.coldfusion"));
  }

  @Override
  public boolean isInContext(@NotNull PsiFile file, int offset) {
    return file.getViewProvider() instanceof CfmlFileViewProvider;
  }
}
