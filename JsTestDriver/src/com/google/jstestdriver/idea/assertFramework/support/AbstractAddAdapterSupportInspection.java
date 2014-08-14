package com.google.jstestdriver.idea.assertFramework.support;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.NotNullProducer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractAddAdapterSupportInspection extends AbstractMethodBasedInspection {

  private final AddAdapterSupportIntentionAction myAddAdapterQuickSupportIntentionAction;
  private final String myAssertionFrameworkName;

  protected AbstractAddAdapterSupportInspection(@NotNull String assertionFrameworkName,
                                                @NotNull NotNullProducer<List<VirtualFile>> adapterSourceFilesProvider,
                                                @Nullable String adapterHomePageUrl) {
    myAssertionFrameworkName = assertionFrameworkName;
    myAddAdapterQuickSupportIntentionAction = new AddAdapterSupportIntentionAction(
      assertionFrameworkName,
      adapterSourceFilesProvider,
      adapterHomePageUrl
    );
  }

  @Override
  protected IntentionAction getFix() {
    return myAddAdapterQuickSupportIntentionAction;
  }

  @Override
  protected String getProblemDescription() {
    return "No " + myAssertionFrameworkName + " framework configured";
  }
}
