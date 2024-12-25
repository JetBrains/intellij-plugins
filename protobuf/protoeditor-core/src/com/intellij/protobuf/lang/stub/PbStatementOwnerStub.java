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

import com.intellij.protobuf.lang.psi.PbElement;
import com.intellij.protobuf.lang.psi.PbStatementOwner;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.util.QualifiedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A base {@link StubElement} interface for elements that implement {@link
 * PbStatementOwner}.
 */
public interface PbStatementOwnerStub<T extends PbElement> extends PbElementStub<T> {
  /** Returns the list of child statement stubs. */
  default @NotNull List<PbStatementStub<?>> getStatements() {
    return StubMethods.getStatements(this);
  }

  /**
   * Returns the scope that child statements reside in. Some statement owners, such as messages,
   * create new scopes. Others, such as oneof definitions, do not. In the latter case, this method
   * returns the parent scope. The scope of a PbFile is the defined package statement.
   */
  @Nullable
  QualifiedName getChildScope();
}
