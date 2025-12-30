package org.angular2.entities

import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.psi.PsiElement
import org.angular2.lang.Angular2Bundle

interface Angular2ClassBasedEntity : Angular2Entity {

  val decorator: ES6Decorator?

  val typeScriptClass: TypeScriptClass?

  override val entityJsType: JSType?
    get() = typeScriptClass?.jsType

  override val entitySource: PsiElement?
    get() = typeScriptClass

  override val entitySourceName: String
    get() = className

  val className: String
    get() = typeScriptClass?.name
            ?: Angular2Bundle.message("angular.description.unknown-class")
}