package com.jetbrains.lang.dart.ide.marker;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonBundle;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartInterfaceDefinition;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartMethodOverrideMarkerProvider implements LineMarkerProvider {

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    if (!(element instanceof DartComponentName)) {
      return null;
    }
    final PsiElement parent = element.getParent();
    if (DartComponentType.typeOf(parent) == DartComponentType.METHOD) {
      final DartClass dartClass = PsiTreeUtil.getParentOfType(element, DartClass.class);
      return dartClass == null ? null : createOverrideMarker(dartClass, (DartComponent)parent);
    }
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
  }

  @Nullable
  private static LineMarkerInfo createOverrideMarker(DartClass dartClass, DartComponent dartComponent) {
    final List<DartClass> supers = new ArrayList<DartClass>();
    final DartClassResolveResult dartClassResolveResult = DartResolveUtil.resolveClassByType(dartClass.getSuperClass());
    if (dartClassResolveResult.getDartClass() != null) {
      supers.add(dartClassResolveResult.getDartClass());
    }
    List<DartClassResolveResult> implementsAndMixinsList = DartResolveUtil.resolveClassesByTypes(
      DartResolveUtil.getImplementsAndMixinsList(dartClass)
    );
    for (DartClassResolveResult resolveResult : implementsAndMixinsList) {
      final DartClass resolveResultDartClass = resolveResult.getDartClass();
      if (resolveResultDartClass != null) {
        supers.add(resolveResultDartClass);
      }
    }
    final List<DartComponent> superItems = DartResolveUtil.findNamedSubComponents(supers.toArray(new DartClass[supers.size()]));
    return tryCreateOverrideMarker(dartComponent, superItems);
  }

  @Nullable
  private static LineMarkerInfo tryCreateOverrideMarker(final DartComponent methodDeclaration,
                                                        List<DartComponent> superItems) {
    final String methodName = methodDeclaration.getName();
    if (methodName == null || !methodDeclaration.isPublic()) {
      return null;
    }
    final List<DartComponent> filteredSuperItems = ContainerUtil.filter(superItems, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return methodName.equals(component.getName());
      }
    });
    if (filteredSuperItems.isEmpty()) {
      return null;
    }
    final PsiElement element = methodDeclaration.getComponentName();
    final DartComponent dartComponent = filteredSuperItems.iterator().next();
    final boolean overrides = !dartComponent.isAbstract() &&
                              !(PsiTreeUtil.getParentOfType(dartComponent, DartClass.class) instanceof DartInterfaceDefinition);
    final Icon icon = overrides ? AllIcons.Gutter.OverridingMethod : AllIcons.Gutter.ImplementingMethod;
    assert element != null;
    return new LineMarkerInfo<PsiElement>(
      element,
      element.getTextRange(),
      icon,
      Pass.UPDATE_ALL,
      new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement element) {
          final DartClass superDartClass = PsiTreeUtil.getParentOfType(methodDeclaration, DartClass.class);
          if (superDartClass == null) return "null";
          if (overrides) {
            return DartBundle.message("overrides.method.in", methodDeclaration.getName(), superDartClass.getName());
          }
          return DartBundle.message("implements.method.in", methodDeclaration.getName(), superDartClass.getName());
        }
      },
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          PsiElementListNavigator.openTargets(
            e,
            DartResolveUtil.getComponentNames(filteredSuperItems).toArray(new NavigatablePsiElement[filteredSuperItems.size()]),
            DaemonBundle.message("navigation.title.super.method", methodDeclaration.getName()),
            DaemonBundle.message("navigation.findUsages.title.super.method", methodDeclaration.getName()),
            new DefaultPsiElementCellRenderer());
        }
      },
      GutterIconRenderer.Alignment.LEFT
    );
  }
}
