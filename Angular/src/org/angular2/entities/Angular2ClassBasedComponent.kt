package org.angular2.entities

import com.intellij.psi.PsiElement

interface Angular2ClassBasedComponent : Angular2ClassBasedDirective, Angular2Component {

  override val jsResolveScope: PsiElement?
    get() = typeScriptClass

  override val jsExportScope: PsiElement?
    get() = typeScriptClass?.containingFile

}