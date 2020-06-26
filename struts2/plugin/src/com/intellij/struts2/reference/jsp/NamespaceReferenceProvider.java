/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.Function;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import java.util.List;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * Custom tags attribute "namespace".
 *
 * @author Yann C&eacute;bron
 */
public class NamespaceReferenceProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext context) {

    final StrutsManager strutsManager = StrutsManager.getInstance(psiElement.getProject());
    final StrutsModel strutsModel = strutsManager.getCombinedModel(psiElement);
    if (strutsModel == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    return new PsiReference[]{new NamespaceReference((XmlAttributeValue) psiElement, strutsModel)
    };
  }


  private static final class NamespaceReference extends PsiReferenceBase.Poly<XmlAttributeValue>
      implements EmptyResolveMessageProvider {

    private static final Function<StrutsPackage, LookupElement> STRUTS_PACKAGE_LOOKUP_ELEMENT_FUNCTION =
      strutsPackage -> LookupElementBuilder.create(strutsPackage.getXmlTag(),
                                         strutsPackage.searchNamespace())
                                 .withTypeText(strutsPackage.getName().getStringValue());

    private final StrutsModel strutsModel;

    private NamespaceReference(final XmlAttributeValue psiElement, final StrutsModel strutsModel) {
      super(psiElement);
      this.strutsModel = strutsModel;
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(final boolean incompleteCode) {
      final String namespace = myElement.getValue();
      final List<ResolveResult> resolveResults = new SmartList<>();
      for (final StrutsPackage strutsPackage : strutsModel.getStrutsPackages()) {
        if (Objects.equals(namespace, strutsPackage.searchNamespace())) {
          final XmlTag packageTag = strutsPackage.getXmlTag();
          assert packageTag != null;
          resolveResults.add(new PsiElementResolveResult(packageTag));
        }
      }
      return resolveResults.toArray(ResolveResult.EMPTY_ARRAY);
    }

    @Override
    public Object @NotNull [] getVariants() {
      return ContainerUtil.map2Array(strutsModel.getStrutsPackages(), LookupElement.class,
                                     STRUTS_PACKAGE_LOOKUP_ELEMENT_FUNCTION);
    }

    @Override
    @NotNull
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve namespace '" + getCanonicalText() + "'";
    }
  }

}