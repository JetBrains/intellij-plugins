// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.util.io.StringRef;

public class CfmlFileStubImpl extends PsiFileStubImpl<CfmlFile> implements CfmlFileStub {
  private StringRef myName;

  public CfmlFileStubImpl(CfmlFile file) {
    super(file);
  }

  public CfmlFileStubImpl(StringRef name) {
    super(null);
    myName = name;
  }

  @Override
  public StringRef getName() {
    return myName;
  }
}

