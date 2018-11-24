package com.jetbrains.lang.dart.ide;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartNamedConstructorDeclaration;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class DartRenamePsiElementProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return !DartResolver.isServerDrivenResolution() && element instanceof DartComponentName;
  }

  @Override
  public void prepareRenaming(PsiElement element, final String newName, final Map<PsiElement, String> allRenames, SearchScope scope) {
    super.prepareRenaming(element, newName, allRenames, scope);
    for (PsiElement elementToRename : allRenames.keySet()) {
      final PsiElement parent = elementToRename.getParent();
      if (parent instanceof DartClass) {
        for (DartComponent constructor : ((DartClass)parent).getConstructors()) {
          if (!(constructor instanceof DartNamedConstructorDeclaration)) {
            allRenames.put(constructor.getComponentName(), newName);
          }
        }
      }
      else if (parent instanceof DartComponent) {
        final DartClass dartClass = PsiTreeUtil.getParentOfType(parent, DartClass.class, true);
        final String componentName = ((DartComponent)parent).getName();
        if (componentName != null && dartClass != null && componentName.equals(dartClass.getName())) {
          allRenames.put(dartClass.getComponentName(), newName);
        }
        DefinitionsSearch.search(((DartComponent)parent).getComponentName()).forEach(element1 -> {
          if (element1 instanceof DartComponent) {
            allRenames.put(((DartComponent)element1).getComponentName(), newName);
          }
          return true;
        });
      }
    }
  }

  @Override
  public PsiElement substituteElementToRename(@NotNull final PsiElement element,
                                              @NotNull Editor editor) {
    final PsiElement parent = element.getParent();
    if (parent instanceof DartComponent && DartComponentType.typeOf(parent) == DartComponentType.METHOD) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(parent, DartClass.class, true);
      final DartComponent superMethod = ContainerUtil.find(
        DartResolveUtil.findNamedSuperComponents(dartClass),
        component -> StringUtil.equals(component.getName(), ((DartComponent)parent).getName())
      );
      if (superMethod != null) {
        return superMethod.getComponentName();
      }
    }
    return element;
  }
}
