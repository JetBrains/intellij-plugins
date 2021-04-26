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
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.descriptor.Descriptor;
import com.intellij.protobuf.lang.descriptor.DescriptorOptionType;
import com.intellij.protobuf.lang.psi.PbEnumValue;
import com.intellij.protobuf.lang.psi.PbOptionExpression;
import com.intellij.protobuf.lang.psi.PbOptionList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

abstract class PbEnumValueMixin extends PbNamedElementBase implements PbEnumValue {

  PbEnumValueMixin(ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public QualifiedName getDescriptorOptionsTypeName(Descriptor descriptor) {
    return DescriptorOptionType.ENUM_VALUE_OPTIONS.forDescriptor(descriptor);
  }

  @Nullable
  @Override
  public Icon getIcon(int flags) {
    return PbIcons.ENUM_VALUE;
  }

  @NotNull
  @Override
  public List<PbOptionExpression> getOptions() {
    PbOptionList optionList = getOptionList();
    if (optionList == null) {
      return Collections.emptyList();
    }
    return optionList.getOptions();
  }

  @Override
  @Nullable
  public QualifiedName getExtensionOptionScope() {
    QualifiedName name = getQualifiedName();
    return name != null ? name.removeLastComponent() : null;
  }
}
