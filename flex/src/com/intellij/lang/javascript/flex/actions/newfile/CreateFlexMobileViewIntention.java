package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CreateFlexMobileViewIntention extends CreateMxmlFileIntentionBase {
  @NotNull
  public String getText() {
    return FlexBundle.message("create.mobile.view", myClassName);
  }

  public CreateFlexMobileViewIntention(final String classFqn, final PsiElement element) {
    super(classFqn, element);
  }

  protected String getFileText() {
    return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
           "<s:View xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:s=\"library://ns.adobe.com/flex/spark\">\n" +
           "\n" +
           "</s:View>\n";
  }
}
