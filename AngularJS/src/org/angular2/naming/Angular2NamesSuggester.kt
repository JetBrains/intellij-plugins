// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.naming;

import com.intellij.lang.javascript.names.JSNameSuggestionsUtil;
import com.intellij.lang.javascript.names.JSNamesSuggester;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Angular2NamesSuggester implements JSNamesSuggester {
  private static final HashMap<String, String> AngularDecoratorEntityMap = new HashMap<>();

  static {
    AngularDecoratorEntityMap.put("Component", "Component");
    AngularDecoratorEntityMap.put("Directive", "Directive");
    AngularDecoratorEntityMap.put("NgModule", "Module");
    AngularDecoratorEntityMap.put("Injectable", "Service");
    AngularDecoratorEntityMap.put("Pipe", "Pipe");
  }

  @Override
  public @Nullable String suggestFileName(@NotNull JSNamedElement namedElement, @NotNull String newElementName) {
    if (!(namedElement instanceof JSClass)) return null;
    return getAngularSpecificFileName((JSClass)namedElement, newElementName);
  }

  private static @Nullable String getAngularSpecificFileName(@NotNull JSClass jsClass, @NotNull String newElementName) {
    ES6Decorator[] decorators = PsiTreeUtil.getChildrenOfType(jsClass.getAttributeList(), ES6Decorator.class);
    if (decorators == null) {
      return null;
    }

    for (ES6Decorator decorator : decorators) {
      String referenceName = decorator.getDecoratorName();
      if (referenceName == null) return null;

      String entityName = AngularDecoratorEntityMap.get(referenceName);
      if (entityName != null) {
        String name;
        if (StringUtil.endsWith(newElementName, entityName)) {
          name = newElementName.substring(0, newElementName.length() - entityName.length());
        }
        else {
          name = newElementName;
        }

        String[] parts = name.split(JSNameSuggestionsUtil.SPLIT_BY_CAMEL_CASE_REGEX);
        String finalName = StringUtil.join(parts, StringUtil::toLowerCase, "-");
        return (StringUtil.isEmpty(finalName) ? "" : finalName + ".")
               + StringUtil.toLowerCase(entityName);
      }
    }

    return null;
  }
}
