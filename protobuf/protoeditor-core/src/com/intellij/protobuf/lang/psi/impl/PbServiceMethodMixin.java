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
import com.intellij.psi.PsiComment;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.descriptor.DescriptorOptionType;
import com.intellij.protobuf.lang.psi.PbBlockBody;
import com.intellij.protobuf.lang.psi.PbMethodOptions;
import com.intellij.protobuf.lang.psi.PbOptionStatement;
import com.intellij.protobuf.lang.psi.PbServiceMethod;
import com.intellij.protobuf.lang.psi.util.PbCommentUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

abstract class PbServiceMethodMixin extends PbNamedElementBase implements PbServiceMethod {

  PbServiceMethodMixin(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public QualifiedName getDescriptorOptionsTypeName(Descriptor descriptor) {
    return DescriptorOptionType.METHOD_OPTIONS.forDescriptor(descriptor);
  }

  @Override
  @Nullable
  public QualifiedName getExtensionOptionScope() {
    QualifiedName name = getQualifiedName();
    return name != null ? name.removeLastComponent() : null;
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return PbIcons.SERVICE_METHOD;
  }

  @NotNull
  @Override
  public List<PsiComment> getTrailingComments() {
    PbBlockBody options = getMethodOptions();
    if (options == null) {
      // No options defined; collect comments after the method statement.
      return PbCommentUtil.collectTrailingComments(this);
    }
    return PbCommentUtil.collectTrailingComments(options.getStart());
  }

  @Override
  @NotNull
  public List<PbOptionStatement> getOptionStatements() {
    PbMethodOptions methodOptions = getMethodOptions();
    if (methodOptions == null) {
      return Collections.emptyList();
    }
    return methodOptions.getOptionStatements();
  }
}
