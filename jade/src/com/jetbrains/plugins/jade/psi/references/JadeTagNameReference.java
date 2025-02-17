// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.references;

import com.intellij.lang.ASTNode;
import com.intellij.psi.impl.source.xml.TagNameReference;

public class JadeTagNameReference extends TagNameReference {
  public JadeTagNameReference(final ASTNode nameElement) {
    super(nameElement, true);
  }
}