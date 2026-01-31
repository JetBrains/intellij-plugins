// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.jhipster.psi.JdlApplication;
import com.intellij.jhipster.psi.JdlConfigBlock;
import com.intellij.jhipster.psi.JdlConfigurationOption;
import com.intellij.jhipster.psi.JdlConstant;
import com.intellij.jhipster.psi.JdlDeployment;
import com.intellij.jhipster.psi.JdlEntity;
import com.intellij.jhipster.psi.JdlEntityFieldMapping;
import com.intellij.jhipster.psi.JdlEnum;
import com.intellij.jhipster.psi.JdlEnumValue;
import com.intellij.jhipster.psi.JdlFile;
import com.intellij.jhipster.psi.JdlOptionNameValue;
import com.intellij.jhipster.psi.JdlRelationshipGroup;
import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.editor.Editor;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class JdlPsiStructureViewFactory implements PsiStructureViewFactory {
  @Override
  public StructureViewBuilder getStructureViewBuilder(@NotNull PsiFile psiFile) {
    return new TreeBasedStructureViewBuilder() {
      @Override
      public @NotNull StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new JdlStructureViewModel(psiFile, editor);
      }
    };
  }
}

class JdlStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider {
  public JdlStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
    super(psiFile, editor, new JdlStructureViewElement(psiFile));
    withSuitableClasses(
      JdlFile.class,
      JdlApplication.class, JdlConfigBlock.class, JdlOptionNameValue.class,
      JdlConfigurationOption.class,
      JdlEntity.class, JdlEntityFieldMapping.class,
      JdlEnum.class, JdlEnumValue.class,
      JdlConstant.class,
      JdlRelationshipGroup.class, JdlDeployment.class
    );
    withSorters(Sorter.ALPHA_SORTER);
  }

  @Override
  public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
    return false;
  }

  @Override
  public boolean isAlwaysLeaf(StructureViewTreeElement element) {
    return false;
  }
}

class JdlStructureViewElement implements StructureViewTreeElement {
  private final PsiElement element;

  public JdlStructureViewElement(@NotNull PsiElement element) {
    this.element = element;
  }

  @Override
  public PsiElement getValue() {
    return element;
  }

  @Override
  public void navigate(boolean requestFocus) {
    ((Navigatable)element).navigate(requestFocus);
  }

  @Override
  public boolean canNavigate() {
    return ((Navigatable)element).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return ((Navigatable)element).canNavigateToSource();
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    ItemPresentation presentation = ((NavigationItem)element).getPresentation();
    assert presentation != null;
    return presentation;
  }

  @Override
  public TreeElement @NotNull [] getChildren() {
    if (element instanceof JdlFile) {
      Class<?>[] topLevelElements = {
        JdlApplication.class,
        JdlConfigurationOption.class,
        JdlEntity.class,
        JdlEnum.class,
        JdlConstant.class,
        JdlRelationshipGroup.class, JdlDeployment.class
      };
      @SuppressWarnings("unchecked")
      var elements = PsiTreeUtil.getChildrenOfAnyType(element, (Class<PsiElement>[])topLevelElements);
      return ContainerUtil.map2Array(elements, TreeElement.class, JdlStructureViewElement::new);
    }

    if (element instanceof JdlApplication) {
      Class<?>[] appElements = {
        JdlConfigBlock.class,
        JdlConfigurationOption.class
      };
      @SuppressWarnings("unchecked")
      var elements = PsiTreeUtil.getChildrenOfAnyType(element, (Class<PsiElement>[])appElements);
      return ContainerUtil.map2Array(elements, TreeElement.class, JdlStructureViewElement::new);
    }

    if (element instanceof JdlConfigBlock
        || element instanceof JdlDeployment) {
      var elements = PsiTreeUtil.getChildrenOfAnyType(element, JdlOptionNameValue.class);
      return ContainerUtil.map2Array(elements, TreeElement.class, JdlStructureViewElement::new);
    }

    if (element instanceof JdlEntity) {
      var elements = PsiTreeUtil.getChildrenOfAnyType(element, JdlEntityFieldMapping.class);
      return ContainerUtil.map2Array(elements, TreeElement.class, JdlStructureViewElement::new);
    }

    if (element instanceof JdlEnum) {
      var elements = PsiTreeUtil.getChildrenOfAnyType(element, JdlEnumValue.class);
      return ContainerUtil.map2Array(elements, TreeElement.class, JdlStructureViewElement::new);
    }

    return EMPTY_ARRAY;
  }
}

