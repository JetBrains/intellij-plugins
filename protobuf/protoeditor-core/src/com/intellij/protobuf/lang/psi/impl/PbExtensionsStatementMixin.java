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
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.descriptor.DescriptorOptionType;
import com.intellij.protobuf.lang.psi.PbExtensionsStatement;
import com.intellij.protobuf.lang.psi.PbMessageType;
import com.intellij.protobuf.lang.psi.PbOptionExpression;
import com.intellij.protobuf.lang.psi.PbOptionList;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

abstract class PbExtensionsStatementMixin extends PbStatementBase implements PbExtensionsStatement {

  PbExtensionsStatementMixin(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull QualifiedName getDescriptorOptionsTypeName(Descriptor descriptor) {
    return DescriptorOptionType.EXTENSION_RANGE_OPTIONS.forDescriptor(descriptor);
  }

  @Override
  public @Nullable QualifiedName getExtensionOptionScope() {
    PbMessageType message = PsiTreeUtil.getParentOfType(this, PbMessageType.class);
    if (message == null) {
      return null;
    }
    QualifiedName name = message.getQualifiedName();
    return name != null ? name.removeLastComponent() : null;
  }

  @Override
  public @NotNull List<PbOptionExpression> getOptions() {
    PbOptionList optionList = getOptionList();
    if (optionList == null) {
      return Collections.emptyList();
    }
    return optionList.getOptions();
  }
}
