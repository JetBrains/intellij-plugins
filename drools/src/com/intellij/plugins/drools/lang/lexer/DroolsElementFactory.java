// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.lexer;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;

import java.util.HashMap;
import java.util.Map;

public final class DroolsElementFactory {
  private static final Map<String, IElementType> ourCompositeMap = new HashMap<>();

  public static synchronized IElementType getTokenType(String name) {
    IElementType elementType = ourCompositeMap.get(name);
    if (elementType == null) {
      elementType = switch (name) {
        case "JAVA_STATEMENT" -> new DroolsJavaStatementLazyParseableElementType();
        case "BLOCK_EXPRESSION" -> new DroolsBlockExpressionsLazyParseableElementType();
        case "true", "false" -> new DroolsElementType(StringUtil.toUpperCase(name));
        case "==" -> new DroolsElementType("EQ");
        default -> new DroolsElementType(name);
      };

      ourCompositeMap.put(name, elementType);
    }
    return elementType;
  }
}
