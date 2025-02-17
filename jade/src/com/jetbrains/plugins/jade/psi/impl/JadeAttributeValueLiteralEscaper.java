// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.injected.XmlAttributeLiteralEscaper;

public class JadeAttributeValueLiteralEscaper extends XmlAttributeLiteralEscaper {
  public JadeAttributeValueLiteralEscaper(final JadeAttributeValueImpl jadeAttribute) {
    super(jadeAttribute);
  }
}
