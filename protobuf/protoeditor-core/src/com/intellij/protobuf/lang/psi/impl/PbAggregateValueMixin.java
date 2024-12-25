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
package com.intellij.protobuf.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.resolve.PbSymbolResolver;
import com.intellij.protobuf.lang.resolve.SchemaInfo;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nullable;

abstract class PbAggregateValueMixin extends PbElementBase implements PbAggregateValue {

  PbAggregateValueMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @Nullable SchemaInfo getSchemaInfo() {
    // Find the containing OptionExpression.
    PbFile pbFile = getPbFile();
    PbOptionExpression optionExpression =
        PsiTreeUtil.getParentOfType(this, PbOptionExpression.class);
    if (optionExpression == null) {
      return null;
    }

    PbNamedTypeElement namedType = optionExpression.getOptionName().getNamedType();
    if (!(namedType instanceof PbMessageType)) {
      return null;
    }

    PbSymbolResolver resolver = PbSymbolResolver.forFile(pbFile);
    return SchemaInfo.create((PbMessageType) namedType, resolver);
  }
}
