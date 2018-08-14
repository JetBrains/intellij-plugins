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
package org.angular2.lang.html.psi.impl;

import com.intellij.lang.javascript.psi.JSStatement;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.Angular2HtmlAnimationEvent;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.intellij.openapi.util.Pair.pair;

public class Angular2HtmlAnimationEventImpl extends XmlAttributeImpl implements Angular2HtmlAnimationEvent {

  public Angular2HtmlAnimationEventImpl(@NotNull Angular2ElementType type) {
    super(type);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof Angular2HtmlElementVisitor) {
      ((Angular2HtmlElementVisitor)visitor).visitAnimationEvent(this);
    } else if (visitor instanceof XmlElementVisitor) {
      ((XmlElementVisitor)visitor).visitXmlAttribute(this);
    } else {
      visitor.visitElement(this);
    }
  }

  @NotNull
  @Override
  public String getAnimationEventName() {
    return getNameAndPhase().first;
  }

  @Nullable
  @Override
  public AnimationPhase getPhase() {
    return getNameAndPhase().second;
  }

  private Pair<String, AnimationPhase> getNameAndPhase() {
    String name = getName();
    if (name.startsWith("(") && name.endsWith(")")) {
      name = name.substring(1, name.length() - 1 );
    } else if (name.startsWith("on-")) {
      name = name.substring(3);
    } else {
      throw new IllegalStateException("Bad attribute name: " + name);
    }
    if (name.startsWith("@")) {
      name = name.substring(1 );
    } else {
      throw new IllegalStateException("Bad attribute name: " + name);
    }
    int dot = name.indexOf('.');
    if (dot < 0) {
      return pair(name, null);
    }
    String phase = name.substring(dot +1).toLowerCase(Locale.ENGLISH);
    name = name.substring(0, dot);
    if ("done".equals(phase)) {
      return pair(name, AnimationPhase.DONE);
    } else if ("start".equals(phase)) {
      return pair(name, AnimationPhase.START);
    }
    return pair(name, null);
  }

  @Nullable
  @Override
  public JSStatement getStatement() {
    return null;
  }


}
