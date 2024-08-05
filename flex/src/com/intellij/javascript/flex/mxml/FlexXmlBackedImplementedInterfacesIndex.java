// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FlexXmlBackedImplementedInterfacesIndex extends FlexXmlBackedClassesIndex {

  public static final ID<String, Void> NAME = ID.create("FlexXmlBackedImplementedInterfacesIndex");

  @Override
  public @NotNull ID<String, Void> getName() {
    return NAME;
  }

  @Override
  protected @Nullable JSReferenceList getSupers(JSClass clazz) {
    return clazz.getImplementsList();
  }


}
