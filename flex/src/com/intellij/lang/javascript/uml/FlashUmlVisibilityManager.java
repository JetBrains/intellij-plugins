package com.intellij.lang.javascript.uml;

import com.intellij.diagram.AbstractUmlVisibilityManager;
import com.intellij.diagram.VisibilityLevel;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FlashUmlVisibilityManager extends AbstractUmlVisibilityManager {

  private static final List<VisibilityLevel> predefinedLevels = new ArrayList<>();

  static {
    for (JSAttributeList.AccessType accessType : JSVisibilityUtil.ACCESS_TYPES) {
      predefinedLevels
        .add(new VisibilityLevel(accessType.name(), JSBundle.message("javascript.uml.visibility." + accessType.name().toLowerCase())));
    }
  }

  private static final Comparator<VisibilityLevel> COMPARATOR = (o1, o2) -> {
    final int ind1 = predefinedLevels.indexOf(o1);
    final int ind2 = predefinedLevels.indexOf(o2);
    return ind1 == ind2 ? 0 : ind1 < 0 ? 1 : ind1 - ind2;
  };

  //@Override
  //public boolean isVisible(PsiElement element) {
  //  // TODO: support namespaces
  //  return super.isVisible(element);
  //}

  public VisibilityLevel[] getVisibilityLevels() {
    return predefinedLevels.toArray(new VisibilityLevel[0]);
  }

  public VisibilityLevel getVisibilityLevel(Object element) {
    // TODO: support namespaces

    if (element instanceof JSAttributeListOwner) {
      JSAttributeList attributeList = ((JSAttributeListOwner)element).getAttributeList();
      JSAttributeList.AccessType accessType =
        attributeList != null ? attributeList.getAccessType() : JSUtils.getImplicitAccessType((PsiElement)element);

      return predefinedLevels.get(ArrayUtil.indexOf(JSVisibilityUtil.ACCESS_TYPES, accessType));
    }
    return null;
  }

  public Comparator<VisibilityLevel> getComparator() {
    return COMPARATOR;
  }
}
