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
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.ide.index.DartInheritanceIndex;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartInterfaceDefinition;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartImplementationsMarkerProvider implements LineMarkerProvider {

  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    elements = ContainerUtil.filter(elements, new Condition<PsiElement>() {
      @Override
      public boolean value(PsiElement element) {
        return element instanceof DartClass;
      }
    });
    if (elements.size() > 20) {
      return;
    }
    for (PsiElement dartClass : elements) {
      collectMarkers(result, (DartClass)dartClass);
    }
  }

  private void collectMarkers(Collection<LineMarkerInfo> result, DartClass dartClass) {
    final List<DartClass> subClasses = DartInheritanceIndex.getItemsByName(dartClass);
    if (!subClasses.isEmpty()) {
      result.add(createImplementationMarker(dartClass, subClasses));
    }
    final List<DartComponent> subItems = new ArrayList<DartComponent>();
    for (DartClass subClass : subClasses) {
      subItems.addAll(DartResolveUtil.getNamedSubComponents(subClass));
    }
    final boolean isInterface = DartComponentType.typeOf(dartClass) == DartComponentType.INTERFACE;
    for (DartComponent dartComponent : DartResolveUtil.getNamedSubComponents(dartClass)) {
      final LineMarkerInfo markerInfo = tryCreateImplementationMarker(dartComponent, subItems, isInterface || dartComponent.isAbstract());
      if (markerInfo != null) {
        result.add(markerInfo);
      }
    }
  }

  private static LineMarkerInfo createImplementationMarker(final DartClass dartClass,
                                                           final List<DartClass> items) {
    final DartComponentName componentName = dartClass.getComponentName();
    return new LineMarkerInfo<PsiElement>(
      componentName,
      componentName.getTextRange(),
      dartClass instanceof DartInterfaceDefinition
      ? AllIcons.Gutter.ImplementedMethod
      : AllIcons.Gutter.OverridenMethod,
      Pass.UPDATE_ALL,
      new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement element) {
          return DaemonBundle.message("method.is.implemented.too.many");
        }
      },
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          PsiElementListNavigator.openTargets(
            e, DartResolveUtil.getComponentNames(items).toArray(new NavigatablePsiElement[items.size()]),
            DaemonBundle.message("navigation.title.subclass", dartClass.getName(), items.size()),
            "Superclasses of " + dartClass.getName(),
            new DefaultPsiElementCellRenderer()
          );
        }
      },
      GutterIconRenderer.Alignment.RIGHT
    );
  }

  @Nullable
  private static LineMarkerInfo tryCreateImplementationMarker(final DartComponent componentWithDeclarationList,
                                                              List<DartComponent> subItems,
                                                              final boolean isInterface) {
    final PsiElement componentName = componentWithDeclarationList.getComponentName();
    final String methodName = componentWithDeclarationList.getName();
    if (methodName == null || !componentWithDeclarationList.isPublic()) {
      return null;
    }
    final List<DartComponent> filteredSubItems = ContainerUtil.filter(subItems, new Condition<DartComponent>() {
      @Override
      public boolean value(DartComponent component) {
        return methodName.equals(component.getName());
      }
    });
    if (filteredSubItems.isEmpty() || componentName == null) {
      return null;
    }
    return new LineMarkerInfo<PsiElement>(
      componentName,
      componentName.getTextRange(),
      isInterface ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.OverridenMethod,
      Pass.UPDATE_ALL,
      new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement element) {
          return isInterface
                 ? DaemonBundle.message("method.is.implemented.too.many")
                 : DaemonBundle.message("method.is.overridden.too.many");
        }
      },
      new GutterIconNavigationHandler<PsiElement>() {
        @Override
        public void navigate(MouseEvent e, PsiElement elt) {
          PsiElementListNavigator.openTargets(
            e, DartResolveUtil.getComponentNames(filteredSubItems).toArray(new NavigatablePsiElement[filteredSubItems.size()]),
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
