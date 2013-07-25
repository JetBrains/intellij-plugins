package com.intellij.coldFusion.model.psi.stubs;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.util.io.StringRef;

/**
 * @author vnikolaenko
 */
public interface CfmlFileStub extends PsiFileStub<CfmlFile> {
  StringRef getName();
}
