package com.google.jstestdriver.idea.assertFramework.support;

import com.google.inject.Provider;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.List;

public abstract class AbstractAddAdapterSupportInspection extends AbstractMethodBasedInspection {

  private final AddAdapterSupportQuickFix myAddAdapterQuickSupportQuickFix;

  protected AbstractAddAdapterSupportInspection(String assertionFrameworkName, Provider<List<VirtualFile>> adapterSourceFilesProvider) {
    myAddAdapterQuickSupportQuickFix = new AddAdapterSupportQuickFix(assertionFrameworkName, adapterSourceFilesProvider);
  }

  @Override
  protected LocalQuickFix getQuickFix() {
    return myAddAdapterQuickSupportQuickFix;
  }

}
