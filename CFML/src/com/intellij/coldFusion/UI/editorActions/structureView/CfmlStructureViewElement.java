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
package com.intellij.coldFusion.UI.editorActions.structureView;

import com.intellij.coldFusion.model.files.CfmlFile;
import com.intellij.coldFusion.model.info.CfmlFunctionDescription;
import com.intellij.coldFusion.model.psi.CfmlComponent;
import com.intellij.coldFusion.model.psi.CfmlFunction;
import com.intellij.coldFusion.model.psi.CfmlTag;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Created by Lera Nikolaenko
 * Date: 19.02.2009
 */
public class CfmlStructureViewElement extends PsiTreeElementBase<PsiElement> {
  protected CfmlStructureViewElement(final PsiElement psiElement) {
    super(psiElement);
  }

  private void collectResults(Collection<StructureViewTreeElement> result, PsiElement element) {
    if (element instanceof CfmlComponent) {
      result.addAll(makeCollection(((CfmlComponent)element).getFunctions()));
    }
    else if (element instanceof CfmlFunction) {
      result.add(new CfmlStructureViewElement(element));
    }
    else if (element instanceof CfmlTag) {
      final PsiElement[] children = element.getChildren();
      for (PsiElement child : children) {
        collectResults(result, child);
      }
    }
  }

  @NotNull
  public Collection<StructureViewTreeElement> getChildrenBase() {
    PsiElement element = getElement();
    Collection<StructureViewTreeElement> result = new LinkedList<StructureViewTreeElement>();

    if (element != null && (element instanceof CfmlFile || !(element instanceof CfmlFunction))) {
      final PsiElement[] children = element.getChildren();
      for (PsiElement child : children) {
        collectResults(result, child);
      }
    }

    return result;
  }

  @Override
  public ItemPresentation getPresentation() {
    return new PresentationData(getPresentableText(), null, getIcon(false), null);
  }

  public String getPresentableText() {
    PsiElement element = getElement();
    if (element instanceof CfmlFunction) {
      return ((CfmlFunction)element).getFunctionInfo().getPresentableText();
    }
    else if (element instanceof CfmlFile) {
      return ((CfmlFile)element).getName();
    }
    return "";
  }

  public static String getParameterPresentation(CfmlFunctionDescription.CfmlParameterDescription param) {
    return param.getPresetableText();
  }

  private Collection<StructureViewTreeElement> makeCollection(@Nullable PsiElement[] tags) {
    if (tags == null) {
      return Collections.emptyList();
    }
    return ContainerUtil.map2List(tags,
                                  new Function<PsiElement, StructureViewTreeElement>() {
                                    public StructureViewTreeElement fun(PsiElement cfmlTag) {
                                      return new CfmlStructureViewElement(cfmlTag);
                                    }
                                  });
  }
}
