// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi.template;

import com.intellij.psi.tree.IElementType;

public interface TftplTokenTypes {
  IElementType DATA_LANGUAGE_TOKEN_UNPARSED = new TftplTokenType("DATA_LANGUAGE_TOKEN_UNPARSED");
}