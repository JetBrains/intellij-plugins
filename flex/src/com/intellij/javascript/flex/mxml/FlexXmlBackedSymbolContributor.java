// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.navigation.JavaScriptClassContributor;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.structureView.JSStructureItemPresentation;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.navigation.PsiElementNavigationItem;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collections;

public class FlexXmlBackedSymbolContributor implements ChooseByNameContributorEx {
  @Override
  public void processNames(@NotNull Processor<? super String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
    if (!FileBasedIndex.getInstance().processAllKeys(FlexXmlBackedMembersIndex.NAME, processor, scope, filter)) return;
    FileType type = JavaScriptSupportLoader.getMxmlFileType();
    if (type != null) {
      if (!FileTypeIndex.processFiles(type, Processors.map(processor, VirtualFile::getNameWithoutExtension), scope)) return;
    }
  }

  @Override
  public void processElementsWithName(@NotNull String name,
                                      @NotNull Processor<? super NavigationItem> processor,
                                      @NotNull FindSymbolParameters parameters) {
    boolean[] result = {true};
    PsiManager psiManager = PsiManager.getInstance(parameters.getProject());
    FileBasedIndex.getInstance().getFilesWithKey(FlexXmlBackedMembersIndex.NAME, Collections.singleton(name), file -> {
      PsiFile psiFile = psiManager.findFile(file);
      if (!(psiFile instanceof XmlFile)) return true;
      FlexXmlBackedMembersIndex.process((XmlFile)psiFile, element -> {
        if (!result[0]) return;
        if (name.equals(FlexXmlBackedMembersIndex.getName(element))) {
          if (element instanceof JSNamedElement) {
            result[0] = processor.process((JSNamedElement)element);
          }
          else {
            XmlAttribute id = ((XmlTag)element).getAttribute("id");
            if (id != null) {
              XmlAttributeValue valueElement = id.getValueElement();
              PsiElement[] children;
              if (valueElement != null && (children = valueElement.getChildren()).length == 3) {
                result[0] = processor.process(new TagNavigationItem(children[1], name));
              }
            }
          }
        }
      }, true);
      return result[0];
    }, parameters.getSearchScope());
    new JavaScriptClassContributor().processElementsWithName(name, o ->
      !(o instanceof XmlBackedJSClassImpl) || processor.process(o), parameters);
  }

  private static class TagNavigationItem extends FakePsiElement implements PsiElementNavigationItem, ItemPresentation {
    final PsiElement myElement;
    final String myName;

    TagNavigationItem(PsiElement element, String name) {
      myElement = element;
      myName = name;
    }

    @Override
    public String getName() {
      return myName;
    }

    @Override
    public ItemPresentation getPresentation() {
      return super.getPresentation();
    }

    @Override
    public PsiElement getTargetElement() {
      return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
      ((Navigatable)myElement).navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
      return ((Navigatable)myElement).canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
      return ((Navigatable)myElement).canNavigateToSource();
    }

    @Override
    public String getPresentableText() {
      return getName();
    }

    @Override
    public String getLocationString() {
      PsiFile file = myElement.getContainingFile();
      String packageName = JSResolveUtil.getExpectedPackageNameFromFile(file.getVirtualFile(), myElement.getProject());
      return StringUtil.getQualifiedName(packageName, FileUtilRt.getNameWithoutExtension(file.getName())) +
             "(" + file.getName() + ")";
    }

    @Override
    public Icon getIcon(boolean open) {
      return JSStructureItemPresentation.getIcon(PsiTreeUtil.getParentOfType(myElement, XmlTag.class));
    }

    @Override
    public PsiElement getParent() {
      return myElement.getParent();
    }

    @Override
    public PsiFile getContainingFile() {
      return myElement.getContainingFile();
    }
  }
}
