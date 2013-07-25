/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  @NonNls private static final String TEMPLATE_ATTR_NAME = "template";
  private static final FileType[] CFML_FILE_TYPES = new FileType[]{CfmlFileType.INSTANCE};

  public CfmlCompositeElement(@NotNull ASTNode node) {
    super(node);
  }

  public CfmlCompositeElement(@NotNull NamedStub stub, @NotNull IStubElementType nodeType) {
    super(stub, nodeType);
  }

  @NotNull
  @Override
  public CfmlFile getContainingFile() {
    return (CfmlFile)super.getContainingFile();    //To change body of overridden methods use File | Settings | File Templates.
  }

  @NotNull
  @Override
  public Language getLanguage() {
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

  public int getProvidedScope() {
    return CfmlScopesInfo.DEFAULT_SCOPE;
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
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
