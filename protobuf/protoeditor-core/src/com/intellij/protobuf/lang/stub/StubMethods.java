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

import com.intellij.psi.stubs.Stub;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public final class StubMethods {

  @Nullable
  static QualifiedName getQualifiedName(PbNamedElementStub<?> stub) {
    String name = stub.getName();
    if (name == null) {
      return null;
    }
    PbStatementOwnerStub<?> parent = stub.getOwner();
    if (parent == null) {
      return null;
    }
    QualifiedName scope = parent.getChildScope();
    if (scope == null) {
      return null;
    }
    return scope.append(name);
  }

  @Nullable
  static PbStatementOwnerStub<?> getOwner(PbStatementStub<?> stub) {
    Stub parent = stub.getParentStub();
    if (parent instanceof PbStatementOwnerStub) {
      return (PbStatementOwnerStub<?>) parent;
    }
    return null;
  }

  @NotNull
  static List<PbStatementStub<?>> getStatements(PbStatementOwnerStub<?> stub) {
    @SuppressWarnings("rawtypes")
    List<StubElement> children = stub.getChildrenStubs();

    return children
        .stream()
        .filter(child -> child instanceof PbStatementStub)
        .map(child -> (PbStatementStub<?>) child)
        .collect(Collectors.toList());
  }

  private StubMethods() {}
}
