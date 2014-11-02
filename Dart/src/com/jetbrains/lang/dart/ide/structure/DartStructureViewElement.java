package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import com.jetbrains.lang.dart.resolve.ComponentNameScopeProcessor;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DartStructureViewElement implements StructureViewTreeElement, SortableTreeElement {
  private final PsiElement myElement;

  public DartStructureViewElement(final PsiElement element) {
    myElement = element;
  }

  @Override
  public Object getValue() {
    return myElement;
  }

  @Override
  public void navigate(boolean requestFocus) {
    if (myElement instanceof NavigationItem) {
      ((NavigationItem)myElement).navigate(requestFocus);
    }
  }

  @Override
  public boolean canNavigate() {
    return myElement instanceof NavigationItem && ((NavigationItem)myElement).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return myElement instanceof NavigationItem && ((NavigationItem)myElement).canNavigateToSource();
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    return myElement instanceof NavigationItem ? ((NavigationItem)myElement).getPresentation() : null;
  }

  @NotNull
  @Override
  public TreeElement[] getChildren() {
    final List<DartComponent> dartComponents = new ArrayList<DartComponent>();
    if (myElement instanceof DartFile || myElement instanceof DartEmbeddedContent) {
      THashSet<DartComponentName> componentNames = new THashSet<DartComponentName>();
      DartPsiCompositeElementImpl
        .processDeclarationsImpl(myElement, new ComponentNameScopeProcessor(componentNames), ResolveState.initial(), null);
      for (DartComponentName componentName : componentNames) {
        PsiElement parent = componentName.getParent();
        if (parent instanceof DartComponent) {
          dartComponents.add((DartComponent)parent);
        }
      }
    }
    else if (myElement instanceof DartClass) {
      for (DartComponent subNamedComponent : DartResolveUtil.getNamedSubComponents((DartClass)myElement)) {
        dartComponents.add(subNamedComponent);
      }
    }

    Collections.sort(dartComponents, new Comparator<DartComponent>() {
      @Override
      public int compare(DartComponent o1, DartComponent o2) {
        return o1.getTextOffset() - o2.getTextOffset();
      }
    });

    final TreeElement[] result = new TreeElement[dartComponents.size()];
    for (int i = 0; i < result.length; i++) {
      result[i] = new DartStructureViewElement(dartComponents.get(i));
    }
    return result;
  }

  @NotNull
  @Override
  public String getAlphaSortKey() {
    final String result = myElement instanceof NavigationItem ? ((NavigationItem)myElement).getName() : null;
    return result == null ? "" : result;
  }

  public PsiElement getRealElement() {
    return myElement;
  }
}
