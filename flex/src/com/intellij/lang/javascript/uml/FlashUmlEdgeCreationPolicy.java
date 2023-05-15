package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramEdgeCreationPolicy;
import com.intellij.diagram.DiagramNode;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.util.JSProjectUtil;
import org.jetbrains.annotations.NotNull;

public class FlashUmlEdgeCreationPolicy extends DiagramEdgeCreationPolicy<Object> {

  @Override
  public boolean acceptSource(@NotNull final DiagramNode<Object> source) {
    if (!(source.getIdentifyingElement() instanceof JSClass clazz)) return false;
    JSAttributeList attributeList = clazz.getAttributeList();
    if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) return false;
    if (JSProjectUtil.isInLibrary(clazz)) return false;
    return true;
  }

  @Override
  public boolean acceptTarget(@NotNull final DiagramNode<Object> target) {
    if (!(target.getIdentifyingElement() instanceof JSClass clazz)) return false;
    JSAttributeList attributeList = clazz.getAttributeList();
    if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.FINAL)) return false;
    return true;
  }
}
