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
package com.intellij.protobuf.lang.stub.type;

import com.intellij.lang.Language;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.stub.PbFileStub;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class PbFileElementType extends IStubFileElementType<PbFileStub> {
  public PbFileElementType(final Language language) {
    super(language);
  }

  public PbFileElementType(final @NonNls String debugName, final Language language) {
    super(debugName, language);
  }

  @Override
  public int getStubVersion() {
    return 0;
  }

  @Override
  public @NotNull StubBuilder getBuilder() {
    return new PbStubBuilder();
  }

  @Override
  public @NotNull String getExternalId() {
    return "protobuf.file";
  }

  @Override
  public void serialize(final @NotNull PbFileStub stub, final @NotNull StubOutputStream dataStream) {}

  @Override
  public @NotNull PbFileStub deserialize(
    final @NotNull StubInputStream dataStream,
    final StubElement parentStub) {
    return new PbFileStub(null);
  }

  private static class PbStubBuilder extends DefaultStubBuilder {
    @Override
    @SuppressWarnings("rawtypes")
    protected @NotNull StubElement createStubForFile(@NotNull PsiFile file) {
      return new PbFileStub((PbFile) file);
    }
  }
}
