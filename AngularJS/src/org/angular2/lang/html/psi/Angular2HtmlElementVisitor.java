// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.XmlElementVisitor;
import org.angular2.lang.html.psi.impl.Angular2HtmlBananaBoxBindingImpl;
import org.angular2.lang.html.psi.impl.Angular2HtmlExpansionFormCaseImpl;
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl;

public class Angular2HtmlElementVisitor extends XmlElementVisitor {

  public void visitBananaBoxBinding(Angular2HtmlBananaBoxBindingImpl bananaBoxBinding) {
    visitBoundAttribute(bananaBoxBinding);
  }

  public void visitBoundAttribute(Angular2HtmlBoundAttribute boundAttribute) {
    visitXmlAttribute(boundAttribute);
  }

  public void visitEvent(Angular2HtmlEvent event) {
    visitBoundAttribute(event);
  }

  public void visitExpansionForm(Angular2HtmlExpansionForm expansion) {
    visitElement(expansion);
  }

  public void visitExpansionFormCase(Angular2HtmlExpansionFormCaseImpl expansionCase) {
    visitElement(expansionCase);
  }

  public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
    visitBoundAttribute(propertyBinding);
  }

  public void visitReference(Angular2HtmlReference reference) {
    visitXmlAttribute(reference);
  }

  public void visitVariable(Angular2HtmlVariable variable) {
    visitXmlAttribute(variable);
  }

  public void visitTemplateBindings(Angular2HtmlTemplateBindingsImpl bindings) {
    visitBoundAttribute(bindings);
  }
}
