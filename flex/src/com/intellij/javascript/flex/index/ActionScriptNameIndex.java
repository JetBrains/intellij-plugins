// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

/*
 * @author max
 */
package com.intellij.javascript.flex.index;

import com.intellij.lang.javascript.index.JSIndexKeys;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

/** AS only. Keeps classes and vars, functions and namespaces under package or file  */
public final class ActionScriptNameIndex extends StringStubIndexExtension<JSQualifiedNamedElement> {
  private static final int VERSION = 2;

  @Override
  public @NotNull StubIndexKey<String, JSQualifiedNamedElement> getKey() {
    return JSIndexKeys.JS_NAME_INDEX_KEY;
  }

  @Override
  public int getVersion() {
    return super.getVersion() + VERSION;
  }
}