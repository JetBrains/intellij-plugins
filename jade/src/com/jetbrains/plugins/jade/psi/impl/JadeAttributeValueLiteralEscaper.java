package com.jetbrains.plugins.jade.psi.impl;

import com.intellij.psi.impl.source.tree.injected.XmlAttributeLiteralEscaper;

public class JadeAttributeValueLiteralEscaper extends XmlAttributeLiteralEscaper {
  public JadeAttributeValueLiteralEscaper(final JadeAttributeValueImpl jadeAttribute) {
    super(jadeAttribute);
  }
}
