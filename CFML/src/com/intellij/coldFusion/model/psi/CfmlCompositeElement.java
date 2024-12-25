// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.coldFusion.model.CfmlLanguage;
import com.intellij.coldFusion.model.CfmlScopesInfo;
import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.files.CfmlFileType;
import com.intellij.coldFusion.model.parsers.CfmlElementTypes;
import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.NamedStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class CfmlCompositeElement extends StubBasedPsiElementBase<NamedStub> implements CfmlPsiElement, CfmlScopeProvider {

  private static final @NonNls String TEMPLATE_ATTR_NAME = "template";
  private static final FileType[] CFML_FILE_TYPES = new FileType[]{CfmlFileType.INSTANCE};

  public CfmlCompositeElement(@NotNull ASTNode node) {
    super(node);
  }

  public CfmlCompositeElement(@NotNull NamedStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @Override
  public @NotNull CfmlFile getContainingFile() {
    return (CfmlFile)super.getContainingFile();
  }

  @Override
  public @NotNull Language getLanguage() {
    return CfmlLanguage.INSTANCE;
  }

  @Override
  public String toString() {
    return getNode().getElementType().toString();
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return CfmlPsiUtil.processDeclarations(processor, state, lastParent, place);
  }

  @Override
  public int getProvidedScope() {
    return CfmlScopesInfo.DEFAULT_SCOPE;
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    final PsiElement parent = getParent();
    if ((parent.getNode().getElementType() == CfmlElementTypes.ATTRIBUTE)) {
      final String localName = ((CfmlAttribute)parent).getAttributeName();
      final PsiElement parentTag = parent.getParent();
      PsiReference[] refs;
      if (TEMPLATE_ATTR_NAME.equalsIgnoreCase(localName) &&
          parentTag.getNode().getElementType() == CfmlElementTypes.TAG &&
          (((CfmlTag)parentTag).getTagName().equals("cfinclude") || ((CfmlTag)parentTag).getTagName().equalsIgnoreCase("cfmodule"))) {
        refs = (new CfmlFileReferenceSet(this, 0)).getAllReferences();
        return refs;
      }
    }
    if (parent.getNode().getElementType() == CfmlElementTypes.INCLUDEEXPRESSION) {
      return (new CfmlFileReferenceSet(this, 1)).getAllReferences();
    }
    return super.getReferences();
  }
}
