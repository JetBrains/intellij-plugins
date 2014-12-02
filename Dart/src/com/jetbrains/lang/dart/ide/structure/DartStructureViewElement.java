package com.jetbrains.lang.dart.ide.structure;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import com.jetbrains.lang.dart.resolve.ComponentNameScopeProcessor;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DartStructureViewElement extends PsiTreeElementBase<NavigatablePsiElement>
  implements ItemPresentation, StructureViewTreeElement {
  public DartStructureViewElement(@NotNull final NavigatablePsiElement element) {
    super(element);
  }

  @NotNull
  @Override
  public Collection<StructureViewTreeElement> getChildrenBase() {
    final NavigatablePsiElement element = getElement();
    final List<StructureViewTreeElement> result = new ArrayList<StructureViewTreeElement>();

    if (element instanceof DartFile || element instanceof DartEmbeddedContent) {
      THashSet<DartComponentName> componentNames = new THashSet<DartComponentName>();
      DartPsiCompositeElementImpl
        .processDeclarationsImpl(element, new ComponentNameScopeProcessor(componentNames), ResolveState.initial(), null);
      for (DartComponentName componentName : componentNames) {
        PsiElement parent = componentName.getParent();
        if (parent instanceof DartComponent) {
          result.add(new DartStructureViewElement((DartComponent)parent));
        }
      }
    }
    else if (element instanceof DartClass) {
      for (DartComponent subNamedComponent : DartResolveUtil.getNamedSubComponents((DartClass)element)) {
        result.add(new DartStructureViewElement(subNamedComponent));
      }
    }

    Collections.sort(result, new Comparator<StructureViewTreeElement>() {
      @Override
      public int compare(StructureViewTreeElement o1, StructureViewTreeElement o2) {
        PsiElement element1, element2;
        if (o1 instanceof DartStructureViewElement &&
            o2 instanceof DartStructureViewElement &&
            (element1 = ((DartStructureViewElement)o1).getElement()) != null &&
            (element2 = ((DartStructureViewElement)o2).getElement()) != null) {
          return element1.getTextOffset() - element2.getTextOffset();
        }
        return 0;
      }
    });

    return result;
  }

  @Nullable
  @Override
  public String getPresentableText() {
    final NavigatablePsiElement element = getElement();
    final ItemPresentation presentation = element == null ? null : element.getPresentation();
    return presentation == null ? null : presentation.getPresentableText();
  }
}
