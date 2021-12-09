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
package com.intellij.protobuf.lang.stub;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.protobuf.lang.psi.PbExtendDefinition;
import com.intellij.protobuf.lang.stub.type.PbExtendDefinitionType;
import org.jetbrains.annotations.Nullable;

public class PbExtendDefinitionStub extends StubBase<PbExtendDefinition>
    implements PbStatementStub<PbExtendDefinition>, PbStatementOwnerStub<PbExtendDefinition> {

  private final String extendedType;

  // TODO(volkman): extendedType might not be a string.
  public PbExtendDefinitionStub(
      StubElement parent,
      PbExtendDefinitionType elementType,
      String extendedType) {
    super(parent, elementType);
    this.extendedType = extendedType;
  }

  public String getExtendedType() {
    return this.extendedType;
  }

  @Nullable
  @Override
  public QualifiedName getChildScope() {
    PbStatementOwnerStub<?> owner = getOwner();
    if (owner != null) {
      return owner.getChildScope();
    }
    return null;
  }
}
