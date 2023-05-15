// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi

import com.intellij.psi.XmlElementVisitor

open class Angular2HtmlElementVisitor : XmlElementVisitor() {
  open fun visitBananaBoxBinding(bananaBoxBinding: Angular2HtmlBananaBoxBinding) {
    visitBoundAttribute(bananaBoxBinding)
  }

  open fun visitBoundAttribute(boundAttribute: Angular2HtmlBoundAttribute) {
    visitXmlAttribute(boundAttribute)
  }

  open fun visitEvent(event: Angular2HtmlEvent) {
    visitBoundAttribute(event)
  }

  open fun visitExpansionForm(expansion: Angular2HtmlExpansionForm) {
    visitElement(expansion)
  }

  fun visitExpansionFormCase(expansionCase: Angular2HtmlExpansionFormCase) {
    visitElement(expansionCase)
  }

  open fun visitPropertyBinding(propertyBinding: Angular2HtmlPropertyBinding) {
    visitBoundAttribute(propertyBinding)
  }

  open fun visitReference(reference: Angular2HtmlReference) {
    visitXmlAttribute(reference)
  }

  open fun visitLet(variable: Angular2HtmlLet) {
    visitXmlAttribute(variable)
  }

  open fun visitTemplateBindings(bindings: Angular2HtmlTemplateBindings) {
    visitBoundAttribute(bindings)
  }

  open fun visitNgContentSelector(ngContentSelector: Angular2HtmlNgContentSelector) {
    visitElement(ngContentSelector)
  }
}