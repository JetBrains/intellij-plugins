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
package com.intellij.protobuf.lang.stub.index;

import com.intellij.protobuf.lang.psi.PbNamedElement;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class QualifiedNameIndex extends StringStubIndexExtension<PbNamedElement> {
  public static final StubIndexKey<String, PbNamedElement> KEY =
      StubIndexKey.createIndexKey("protobuf.byQualifiedName");

  private static final QualifiedNameIndex INSTANCE = new QualifiedNameIndex();

  public static QualifiedNameIndex getInstance() {
    return INSTANCE;
  }

  @Override
  public @NotNull StubIndexKey<String, PbNamedElement> getKey() {
    return KEY;
  }

  @Override
  public int getVersion() {
    return super.getVersion() + 1;
  }
}
