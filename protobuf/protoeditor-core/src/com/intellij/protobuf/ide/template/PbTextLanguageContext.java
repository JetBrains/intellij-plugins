/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.template;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.protobuf.ide.PbIdeBundle;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

/** Defines a Live Template context for prototext files (or text format options in .proto). */
class PbTextLanguageContext extends TemplateContextType {

  PbTextLanguageContext() {
    super(PbIdeBundle.message("prototext.name.sentence"));
  }

  @Override
  public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
    return PbTextLanguage.INSTANCE.is(
        PsiUtilCore.getLanguageAtOffset(templateActionContext.getFile(), templateActionContext.getStartOffset()));
  }
}
