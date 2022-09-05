// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.presentable.JSFormatUtil;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.impl.JSVariableImpl;
import com.intellij.lang.javascript.psi.stubs.JSVariableStub;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.HintedReferenceHost;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceService;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.angular2.lang.html.psi.Angular2HtmlAttrVariable;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.stub.Angular2HtmlStubElementTypes;
import org.angular2.lang.html.stub.Angular2HtmlVariableElementType;
import org.angular2.lang.types.Angular2LetType;
import org.angular2.lang.types.Angular2ReferenceType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2HtmlAttrVariableImpl extends JSVariableImpl<JSVariableStub<JSVariable>, JSVariable>
  implements Angular2HtmlAttrVariable, HintedReferenceHost {

  public Angular2HtmlAttrVariableImpl(ASTNode node) {
    super(node);
  }

  public Angular2HtmlAttrVariableImpl(JSVariableStub<JSVariable> stub) {
    super(stub, Angular2HtmlStubElementTypes.REFERENCE_VARIABLE);
  }

  @Override
  public @NotNull Kind getKind() {
    return ((Angular2HtmlVariableElementType)getElementType()).getKind();
  }

  @Override
  public @Nullable JSType calculateType() {
    return switch (getKind()) {
      case REFERENCE -> new Angular2ReferenceType(this);
      case LET -> new Angular2LetType(this);
    };
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public boolean isExported() {
    return true;
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return switch (getKind()) {
      case REFERENCE -> Angular2ReferenceType.getUseScope(this);
      case LET -> Angular2LetType.getUseScope(this);
    };
  }

  @Override
  public void delete() throws IncorrectOperationException {
    PsiElement ref = PsiTreeUtil.findFirstParent(this, Angular2HtmlReference.class::isInstance);
    if (ref != null) {
      ref.delete();
    }
    else {
      super.delete();
    }
  }

  @Override
  protected @NotNull JSAttributeList.AccessType calcAccessType() {
    return JSAttributeList.AccessType.PUBLIC;
  }

  @Override
  public boolean useTypesFromJSDoc() {
    return false;
  }

  @Override
  public PsiReference @NotNull [] getReferences(@NotNull PsiReferenceService.Hints hints) {
    return super.getReferences();
  }

  @Override
  public boolean shouldAskParentForReferences(@NotNull PsiReferenceService.Hints hints) {
    return false;
  }

  @Override
  public String toString() {
    String classname = "Angular2HtmlAttrVariable[" + getKind() + "]";
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      classname += ":";
      final String name = this.getName();
      classname += name != null ? name : JSFormatUtil.getAnonymousElementPresentation();
    }
    return classname;
  }
}
