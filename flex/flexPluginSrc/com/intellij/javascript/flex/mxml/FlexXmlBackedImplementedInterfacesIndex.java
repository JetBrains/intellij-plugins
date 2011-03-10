package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.Nullable;

public class FlexXmlBackedImplementedInterfacesIndex extends FlexXmlBackedClassesIndex {

  public static final ID<String, Void> NAME = ID.create("FlexXmlBackedImplementedInterfacesIndex");

  public ID<String, Void> getName() {
    return NAME;
  }

  @Nullable
  protected JSReferenceList getSupers(JSClass clazz) {
    return clazz.getImplementsList();
  }


}
