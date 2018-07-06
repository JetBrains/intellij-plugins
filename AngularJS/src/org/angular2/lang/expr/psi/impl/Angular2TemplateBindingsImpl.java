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
package org.angular2.lang.expr.psi.impl;

import com.intellij.lang.javascript.psi.impl.JSStatementImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.angular2.lang.expr.parser.Angular2ElementTypes;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;

import java.util.Arrays;

public class Angular2TemplateBindingsImpl extends JSStatementImpl implements Angular2TemplateBindings {

  public Angular2TemplateBindingsImpl(IElementType elementType) {
    super(elementType);
  }

  @Override
  public Angular2TemplateBinding[] getBindings() {
    return Arrays.stream(getChildren(TokenSet.create(Angular2ElementTypes.TEMPLATE_BINDING_STATEMENT)))
                 .map(n -> n.getPsi(Angular2TemplateBinding.class))
                 .toArray(Angular2TemplateBinding[]::new);
  }

}
