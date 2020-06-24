package com.intellij.lang.javascript.uml;

import com.intellij.diagram.AbstractDiagramNodeContentManager;
import com.intellij.diagram.DiagramCategory;
import com.intellij.diagram.presentation.DiagramState;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.psi.PsiElement;
import com.intellij.uml.utils.DiagramBundle;
import com.intellij.util.PlatformIcons;

public class FlashUmlNodeContentManager extends AbstractDiagramNodeContentManager {
  private final DiagramCategory myFields =
    new DiagramCategory(DiagramBundle.message("category.name.fields"), PlatformIcons.FIELD_ICON);
  private final DiagramCategory myConstructors =
    new DiagramCategory(DiagramBundle.message("category.name.constructors"), JSFunctionImpl.CONSTRUCTOR_ICON);
  private final DiagramCategory myMethods =
    new DiagramCategory(DiagramBundle.message("category.name.methods"), PlatformIcons.METHOD_ICON);
  private final DiagramCategory myProperties =
    new DiagramCategory(DiagramBundle.message("category.name.properties"), PlatformIcons.PROPERTY_ICON);

  private final DiagramCategory[] myCategories = {myFields, myConstructors, myMethods, myProperties};

  @Override
  public DiagramCategory[] getContentCategories() {
    return myCategories;
  }

  @Override
  public boolean isInCategory(Object obj, DiagramCategory category, DiagramState presentation) {
    if (!(obj instanceof PsiElement)) return false;
    PsiElement element = (PsiElement)obj;

    if (JSUtils.getMemberContainingClass(element) == null) return false;

    if (myFields.equals(category)) {
      return element instanceof JSVariable;
    }
    if (myConstructors.equals(category)) {
      return element instanceof JSFunction && ((JSFunction)element).getKind() == JSFunction.FunctionKind.CONSTRUCTOR;
    }
    if (myMethods.equals(category)) {
      return element instanceof JSFunction && ((JSFunction)element).getKind() == JSFunction.FunctionKind.SIMPLE;
    }

    if (myProperties.equals(category)) {
      return element instanceof JSFunction &&
             (((JSFunction)element).getKind() == JSFunction.FunctionKind.GETTER ||
              ((JSFunction)element).getKind() == JSFunction.FunctionKind.SETTER);
    }
    return false;
  }
}
