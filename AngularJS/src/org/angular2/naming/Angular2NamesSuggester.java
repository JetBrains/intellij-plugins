// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angular2.naming;

import com.intellij.lang.javascript.names.JSNameSuggestionsUtil;
import com.intellij.lang.javascript.names.JSNamesSuggester;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Angular2NamesSuggester implements JSNamesSuggester {
  private static final HashMap<String, String> AngularDecoratorEntityMap = ContainerUtil.newHashMap();

  static {
    AngularDecoratorEntityMap.put("Component", "Component");
    AngularDecoratorEntityMap.put("Directive", "Directive");
    AngularDecoratorEntityMap.put("NgModule", "Module");
    AngularDecoratorEntityMap.put("Injectable", "Service");
    AngularDecoratorEntityMap.put("Pipe", "Pipe");
  }

  @Nullable
  @Override
  public String suggestFileName(@NotNull JSNamedElement namedElement, @NotNull String newElementName) {
    if (!(namedElement instanceof JSClass)) return null;
    return getAngularSpecificFileName((JSClass)namedElement, newElementName);
  }

  @Nullable
  private static String getAngularSpecificFileName(@NotNull JSClass jsClass, @NotNull String newElementName) {
    ES6Decorator[] decorators = PsiTreeUtil.getChildrenOfType(jsClass.getAttributeList(), ES6Decorator.class);
    if (decorators == null) {
      return null;
    }

    for (ES6Decorator decorator : decorators) {
      String referenceName = getDecoratorName(decorator);
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

  @Nullable
  private static String getDecoratorName(@NotNull ES6Decorator decorator) {
    JSExpression expression = decorator.getExpression();
    if (!(expression instanceof JSCallExpression)) {
      return null;
    }

    JSExpression methodExpression = ((JSCallExpression)expression).getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression)) {
      return null;
    }

    String referenceName = ((JSReferenceExpression)methodExpression).getReferenceName();
    if (referenceName == null) {
      return null;
    }
    return referenceName;
  }
}
