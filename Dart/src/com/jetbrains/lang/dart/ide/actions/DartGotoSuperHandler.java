package com.jetbrains.lang.dart.ide.actions;

import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartType;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DartGotoSuperHandler implements LanguageCodeInsightActionHandler {
  @Override
  public boolean isValidFor(Editor editor, PsiFile file) {
    return file.getLanguage() == DartLanguage.INSTANCE;
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    final PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());

    final DartComponent component = PsiTreeUtil.getParentOfType(at, DartComponent.class);
    final DartClass dartClass = PsiTreeUtil.getParentOfType(at, DartClass.class);
    if (at == null || dartClass == null || component == null) return;

    final List<DartClass> supers = new ArrayList<DartClass>();
    final DartType superClass = dartClass.getSuperClass();
    // looks like there's no sense in jumping to Object class
    if (superClass != null && !DartResolveUtil.OBJECT.equals(superClass.getReferenceExpression().getText())) {
      final DartClassResolveResult dartClassResolveResult = DartResolveUtil.resolveClassByType(superClass);
      if (dartClassResolveResult.getDartClass() != null) {
        supers.add(dartClassResolveResult.getDartClass());
      }
    }

    List<DartClassResolveResult> implementsAndMixinsList =
      DartResolveUtil.resolveClassesByTypes(DartResolveUtil.getImplementsAndMixinsList(dartClass));
    for (DartClassResolveResult resolveResult : implementsAndMixinsList) {
      final DartClass resolveResultDartClass = resolveResult.getDartClass();
      if (resolveResultDartClass != null) {
        supers.add(resolveResultDartClass);
      }
    }
    final List<DartComponent> superItems = DartResolveUtil.findNamedSubComponents(false, supers.toArray(new DartClass[supers.size()]));

    final DartComponentType type = DartComponentType.typeOf(component);
    if (type == DartComponentType.METHOD) {
      tryNavigateToSuperMethod(editor, component, superItems);
    }
    else if (!supers.isEmpty() && component instanceof DartClass) {
      PsiElementListNavigator.openTargets(
        editor,
        DartResolveUtil.getComponentNameArray(supers),
        DaemonBundle.message("navigation.title.subclass", component.getName(), supers.size()),
        "Subclasses of " + component.getName(),
        new DefaultPsiElementCellRenderer()
      );
    }
  }

  private static void tryNavigateToSuperMethod(Editor editor,
                                               DartComponent methodDeclaration,
                                               List<DartComponent> superItems) {
    final String methodName = methodDeclaration.getName();
    if (methodName == null || !methodDeclaration.isPublic()) {
      return;
    }
    final List<DartComponent> filteredSuperItems = ContainerUtil.filter(superItems, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return methodName.equals(component.getName());
      }
    });
    if (!filteredSuperItems.isEmpty()) {
      final NavigatablePsiElement[] targets = DartResolveUtil.getComponentNameArray(filteredSuperItems);
      PsiElementListNavigator.openTargets(editor,
                                          targets,
                                          DaemonBundle.message("navigation.title.super.method", methodName),
                                          null,
                                          new DefaultPsiElementCellRenderer());
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
