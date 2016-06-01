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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.index.DartInheritanceIndex;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DartImplementationsMarkerProvider implements LineMarkerProvider {

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull final List<PsiElement> elements, @NotNull final Collection<LineMarkerInfo> result) {
    for (PsiElement element : elements) {
      ProgressManager.checkCanceled();
      if (element instanceof DartClass) {
        collectMarkers(result, (DartClass)element);
      }
    }
  }

  private static void collectMarkers(@NotNull final Collection<LineMarkerInfo> result, @NotNull final DartClass dartClass) {
    final List<DartClass> subClasses = DartInheritanceIndex.getItemsByName(dartClass);
    if (!subClasses.isEmpty() && !DartResolveUtil.OBJECT.equals(dartClass.getName())) {
      result.add(createImplementationMarker(dartClass, subClasses));
    }

    final List<DartComponent> subItems = new ArrayList<DartComponent>();
    for (DartClass subClass : subClasses) {
      subItems.addAll(DartResolveUtil.getNamedSubComponents(subClass));
    }

    for (DartComponent dartComponent : DartResolveUtil.getNamedSubComponents(dartClass)) {
      final LineMarkerInfo markerInfo = tryCreateImplementationMarker(dartComponent, subItems, dartComponent.isAbstract());
      if (markerInfo != null) {
        result.add(markerInfo);
      }
    }
  }

  @NotNull
  private static LineMarkerInfo createImplementationMarker(@NotNull final DartClass dartClass, @NotNull final List<DartClass> subClasses) {
    final DartComponentName componentName = dartClass.getComponentName();
    assert componentName != null : dartClass.getText(); // unnamed class can't have subclasses

    return new LineMarkerInfo<PsiElement>(
      componentName,
      componentName.getTextRange(),
      AllIcons.Gutter.OverridenMethod,
      Pass.UPDATE_OVERRIDDEN_MARKERS,
      element -> DaemonBundle.message("method.is.implemented.too.many"),
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          PsiElementListNavigator.openTargets(
            e, DartResolveUtil.getComponentNameArray(subClasses),
            DaemonBundle.message("navigation.title.subclass", dartClass.getName(), subClasses.size(),""),
            "Superclasses of " + dartClass.getName(),
            new DefaultPsiElementCellRenderer()
          );
        }
      },
      GutterIconRenderer.Alignment.RIGHT
    );
  }

  @Nullable
  private static LineMarkerInfo tryCreateImplementationMarker(@NotNull final DartComponent componentWithDeclarationList,
                                                              @NotNull final List<DartComponent> subItems,
                                                              final boolean isInterface) {
    final PsiElement componentName = componentWithDeclarationList.getComponentName();
    final String methodName = componentWithDeclarationList.getName();
    if (methodName == null || !componentWithDeclarationList.isPublic()) {
      return null;
    }
    final List<DartComponent> filteredSubItems = ContainerUtil.filter(subItems, component -> methodName.equals(component.getName()));
    if (filteredSubItems.isEmpty() || componentName == null) {
      return null;
    }
    return new LineMarkerInfo<PsiElement>(
      componentName,
      componentName.getTextRange(),
      isInterface ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.OverridenMethod,
      Pass.UPDATE_OVERRIDDEN_MARKERS,
      element -> isInterface
             ? DaemonBundle.message("method.is.implemented.too.many")
             : DaemonBundle.message("method.is.overridden.too.many"),
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          PsiElementListNavigator.openTargets(
            e, DartResolveUtil.getComponentNameArray(filteredSubItems),
            isInterface ?
            DaemonBundle.message("navigation.title.implementation.method", componentWithDeclarationList.getName(), filteredSubItems.size())
                        :
            DaemonBundle.message("navigation.title.overrider.method", componentWithDeclarationList.getName(), filteredSubItems.size()),
            "Implementations of " + componentWithDeclarationList.getName(),
            new DefaultPsiElementCellRenderer()
          );
        }
      },
      GutterIconRenderer.Alignment.RIGHT
    );
  }
}
