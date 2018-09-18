// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.lexer;

import com.intellij.psi.xml.XmlTokenType;

public interface Angular2HtmlTokenTypes extends XmlTokenType {

  Angular2HtmlTokenType LBRACE = new Angular2HtmlTokenType("NG:LBRACE");
  Angular2HtmlTokenType RBRACE = new Angular2HtmlTokenType("NG:RBRACE");

  Angular2HtmlTokenType INTERPOLATION_START = new Angular2HtmlTokenType("NG:INTERPOLATION_START");
  Angular2HtmlTokenType INTERPOLATION_END = new Angular2HtmlTokenType("NG:INTERPOLATION_END");
}
