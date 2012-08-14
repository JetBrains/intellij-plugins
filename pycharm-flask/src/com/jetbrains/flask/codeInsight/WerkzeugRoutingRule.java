/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.flask.codeInsight;

import com.intellij.openapi.util.TextRange;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yole
 */
public class WerkzeugRoutingRule {
  public static class Parameter {
    public final TextRange psiTextRange;
    public final TextRange valueTextRange;
    @Nullable public final String converterName;
    public final String parameterName;

    public Parameter(TextRange psiTextRange, TextRange valueTextRange, @Nullable String converterName, String parameterName) {
      this.parameterName = parameterName;
      this.converterName = converterName;
      this.valueTextRange = valueTextRange;
      this.psiTextRange = psiTextRange;
    }
  }

  public final List<Parameter> parameters = new ArrayList<Parameter>();

  public static WerkzeugRoutingRule parse(PyStringLiteralExpression literal) {
    WerkzeugRoutingRule rule = new WerkzeugRoutingRule();
    String value = literal.getStringValue();
    int index = 0;
    while (true) {
      int gt = value.indexOf('<', index);
      if (gt < 0) break;
      int lt = value.indexOf('>', gt);
      if (lt < 0) lt = value.length();
      String parameterText = value.substring(gt+1, lt);
      int colon = parameterText.indexOf(':');
      String parameterName, converterName;
      if (colon >= 0) {
        converterName = parameterText.substring(0, colon);
        parameterName = parameterText.substring(colon+1);
      }
      else {
        parameterName = parameterText;
        converterName = null;
      }
      TextRange valueTextRange = new TextRange(gt+1, lt);
      TextRange psiTextRange = new TextRange(literal.valueOffsetToTextOffset(gt+1), literal.valueOffsetToTextOffset(lt));
      rule.parameters.add(new Parameter(psiTextRange, valueTextRange, converterName, parameterName));
      index = lt;
    }
    return rule;
  }
}
