// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi;

import com.intellij.psi.XmlElementVisitor;

public class Angular2HtmlElementVisitor extends XmlElementVisitor {

  public void visitBananaBoxBinding(Angular2HtmlBananaBoxBinding bananaBoxBinding) {
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

  public void visitExpansionFormCase(Angular2HtmlExpansionFormCase expansionCase) {
    visitElement(expansionCase);
  }

  public void visitPropertyBinding(Angular2HtmlPropertyBinding propertyBinding) {
    visitBoundAttribute(propertyBinding);
  }

  public void visitReference(Angular2HtmlReference reference) {
    visitXmlAttribute(reference);
  }

  public void visitLet(Angular2HtmlLet variable) {
    visitXmlAttribute(variable);
  }

  public void visitTemplateBindings(Angular2HtmlTemplateBindings bindings) {
    visitBoundAttribute(bindings);
  }

  public void visitNgContentSelector(Angular2HtmlNgContentSelector ngContentSelect) {
    visitElement(ngContentSelect);
  }
}
