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
package com.intellij.protobuf.lang.findusages;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.intellij.protobuf.lang.PbLangBundle;
import com.intellij.protobuf.lang.psi.*;
import org.jetbrains.annotations.Nullable;

/**
 * Classify some usages within .proto files. Otherwise, they are treated as "unclassified" and could
 * get lost in a sea of unclassified generated code usages.
 */
public class PbUsageTypeProvider implements UsageTypeProvider {

  public static UsageType fieldDeclaration() {
    return new UsageType(() -> PbLangBundle.message("usage.field.type.reference"));
  }

  public static UsageType extendDefinition() {
    return new UsageType(() -> PbLangBundle.message("usage.extend.type.reference"));
  }

  public static UsageType serviceType() {
    return new UsageType(() -> PbLangBundle.message("usage.service.type.reference"));
  }

  public static UsageType optionExpression() {
    return new UsageType(() -> PbLangBundle.message("usage.option.expr.reference"));
  }

  @Nullable
  @Override
  public UsageType getUsageType(PsiElement element) {
    PbTypeName typeParent = PsiTreeUtil.getParentOfType(element, PbTypeName.class);
    if (typeParent != null) {
      if (PsiTreeUtil.getParentOfType(typeParent, PbField.class) != null) {
        return fieldDeclaration();
      }
      PbDefinition owner = PsiTreeUtil.getParentOfType(typeParent, PbDefinition.class);
      if (owner instanceof PbExtendDefinition) {
        return extendDefinition();
      }
      if (owner instanceof PbServiceDefinition) {
        return serviceType();
      }
    }
    if (PsiTreeUtil.getParentOfType(element, PbOptionExpression.class) != null) {
      return optionExpression();
    }
    return null;
  }
}
