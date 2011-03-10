package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;

public abstract class FlexUnitInspectionVisitor extends JSElementVisitor {

  private FlexUnitSupport myFlexUnitSupport;

  private boolean myIsInitialized;

  public FlexUnitSupport getFlexUnitSupport(PsiElement context) {
    if (!myIsInitialized) {
      final Pair<Module, FlexUnitSupport> supportForModule = FlexUnitSupport.getModuleAndSupport(context);
      myFlexUnitSupport = supportForModule != null ? supportForModule.second : null;
      myIsInitialized = true;
    }
    return myFlexUnitSupport;
  }

}
