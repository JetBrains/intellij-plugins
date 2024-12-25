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

import com.intellij.protobuf.lang.psi.PbMessageDefinition;
import com.intellij.protobuf.lang.stub.type.PbMessageDefinitionType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.Nullable;

public class PbMessageDefinitionStub extends StubBase<PbMessageDefinition>
    implements PbNamedElementStub<PbMessageDefinition>, PbStatementOwnerStub<PbMessageDefinition> {

  private final String name;

  public PbMessageDefinitionStub(
      StubElement parent,
      PbMessageDefinitionType elementType,
      String name) {
    super(parent, elementType);
    this.name = name;
  }

  @Override
  public @Nullable String getName() {
    return this.name;
  }

  @Override
  public @Nullable QualifiedName getQualifiedName() {
    return StubMethods.getQualifiedName(this);
  }

  @Override
  public @Nullable QualifiedName getChildScope() {
    return StubMethods.getQualifiedName(this);
  }
}
