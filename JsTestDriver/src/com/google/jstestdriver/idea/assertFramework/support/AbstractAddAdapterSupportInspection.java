package com.google.jstestdriver.idea.assertFramework.support;

import com.google.inject.Provider;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractAddAdapterSupportInspection extends AbstractMethodBasedInspection {

  private final AddAdapterSupportQuickFix myAddAdapterQuickSupportQuickFix;
  private final String myAssertionFrameworkName;

  protected AbstractAddAdapterSupportInspection(@NotNull String assertionFrameworkName,
                                                @NotNull Provider<List<VirtualFile>> adapterSourceFilesProvider,
                                                @Nullable String adapterHomePageUrl) {
    myAssertionFrameworkName = assertionFrameworkName;
    myAddAdapterQuickSupportQuickFix = new AddAdapterSupportQuickFix(
      assertionFrameworkName,
      adapterSourceFilesProvider,
      adapterHomePageUrl
    );
  }

  @Override
  protected LocalQuickFix getQuickFix() {
    return myAddAdapterQuickSupportQuickFix;
  }

  @Override
  protected String getProblemDescription() {
    return "No " + myAssertionFrameworkName + " framework configured";
  }
}
