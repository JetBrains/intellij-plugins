// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.psi;

import com.intellij.psi.tree.IElementType;

public interface TerraformTemplateTokenTypes {
  IElementType DATA_LANGUAGE_TOKEN_UNPARSED = new TerraformTemplateTokenType("DATA_LANGUAGE_TOKEN_UNPARSED");
}