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
package org.angular2.lang.html.psi;

import com.intellij.psi.XmlElementVisitor;
import org.angular2.lang.html.psi.impl.Angular2HtmlExpansionFormCaseImpl;
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl;

public class Angular2HtmlElementVisitor extends XmlElementVisitor {

  public void visitEvent(Angular2HtmlEvent event) {
    visitXmlAttribute(event);
  }

  public void visitExpansionForm(Angular2HtmlExpansionForm expansion) {
    visitElement(expansion);
  }

  public void visitExpansionFormCase(Angular2HtmlExpansionFormCaseImpl expansionCase) {
    visitElement(expansionCase);
  }

  public void visitAnimation(Angular2HtmlAnimation animation) {
    visitXmlAttribute(animation);
  }

  public void visitAnimationEvent(Angular2HtmlAnimationEvent animationEvent) {
    visitXmlAttribute(animationEvent);
  }

  public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
    visitXmlAttribute(propertyBinding);
  }

  public void visitReference(Angular2HtmlReference reference) {
    visitXmlAttribute(reference);
  }

  public void visitVariable(Angular2HtmlVariable variable) {
    visitXmlAttribute(variable);
  }

  public void visitTemplateBindings(Angular2HtmlTemplateBindingsImpl bindings) {
    visitXmlAttribute(bindings);
  }
}
