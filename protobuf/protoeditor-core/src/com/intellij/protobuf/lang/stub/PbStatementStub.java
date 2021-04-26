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

import com.intellij.psi.stubs.StubElement;
import com.intellij.protobuf.lang.psi.PbElement;
import com.intellij.protobuf.lang.psi.PbStatement;
import org.jetbrains.annotations.Nullable;

/**
 * A base {@link StubElement} interface for elements that implement {@link
 * PbStatement}.
 */
public interface PbStatementStub<T extends PbElement> extends PbElementStub<T> {

  @Nullable
  default PbStatementOwnerStub<?> getOwner() {
    return StubMethods.getOwner(this);
  }
}
