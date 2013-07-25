package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.util.io.StringRef;

/**
 * @author vnikolaenko
 */
public class CfmlFileStubImpl extends PsiFileStubImpl<CfmlFile> implements CfmlFileStub {
  private StringRef myName;

  public CfmlFileStubImpl(CfmlFile file) {
    super(file);
  }

  public CfmlFileStubImpl(StringRef name) {
    super(null);
    myName = name;
  }

  public StringRef getName() {
    return myName;
  }
}

