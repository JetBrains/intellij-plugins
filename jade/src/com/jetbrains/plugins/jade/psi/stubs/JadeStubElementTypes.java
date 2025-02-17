// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.stubs;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.jetbrains.plugins.jade.psi.JadeFileElementType;

public interface JadeStubElementTypes {

  IFileElementType JADE_FILE = new JadeFileElementType();

  IElementType MIXIN_DECLARATION = new JadeMixinDeclarationType("MIXIN_DECLARATION");

}
