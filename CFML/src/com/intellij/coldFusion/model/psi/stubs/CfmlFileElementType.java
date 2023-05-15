// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.lang.Language;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.io.StringRef;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class CfmlFileElementType extends IStubFileElementType<CfmlFileStub> {
  public CfmlFileElementType(@NonNls final String debugName, final Language language) {
    super(debugName, language);
  }

  @Override
  public StubBuilder getBuilder() {
    return new DefaultStubBuilder() {
      @Override
      @NotNull
      protected StubElement createStubForFile(@NotNull final PsiFile file) {
        if (file instanceof CfmlFile) {
          return new CfmlFileStubImpl((CfmlFile)file);
        }

        return super.createStubForFile(file);
      }
    };
  }

  @Override
  public int getStubVersion() {
    return super.getStubVersion() + 34;
  }

  @NotNull
  @Override
  public String getExternalId() {
    return "cfml.FILE";
  }

  @Override
  public void serialize(@NotNull CfmlFileStub stub, @NotNull StubOutputStream dataStream) throws IOException {
    dataStream.writeName(stub.getName().toString());
  }

  @NotNull
  @Override
  public CfmlFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException {
    StringRef name = dataStream.readName();
    return new CfmlFileStubImpl(name);
  }
}
