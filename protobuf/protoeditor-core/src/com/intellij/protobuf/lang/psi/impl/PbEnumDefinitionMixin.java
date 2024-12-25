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

import com.google.common.collect.Multimap;
import com.intellij.lang.ASTNode;
import com.intellij.protobuf.ide.util.PbIcons;
import com.intellij.protobuf.lang.psi.PbEnumDefinition;
import com.intellij.protobuf.lang.psi.PbEnumValue;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import com.intellij.protobuf.lang.stub.PbEnumDefinitionStub;
import com.intellij.psi.stubs.IStubElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract class PbEnumDefinitionMixin extends PbStubbedNamedDefinitionBase<PbEnumDefinitionStub>
    implements PbEnumDefinition {

  PbEnumDefinitionMixin(ASTNode node) {
    super(node);
  }

  PbEnumDefinitionMixin(PbEnumDefinitionStub stub, IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Override
  public @NotNull Multimap<String, PbEnumValue> getEnumValueMap() {
    return PbPsiImplUtil.getCachedEnumValueMap(this);
  }

  @Override
  public @Nullable Icon getIcon(int flags) {
    return PbIcons.ENUM;
  }
}
