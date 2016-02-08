package com.intellij.aws.cloudformation.references

import com.intellij.json.psi.JsonElementGenerator
import com.intellij.json.psi.JsonLiteral
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase

abstract class CloudFormationReferenceBase(element: JsonLiteral) : PsiReferenceBase<JsonLiteral>(element) {
  override fun getVariants(): Array<Any> {
    return getCompletionVariants().toTypedArray()
  }

  override fun handleElementRename(newElementName: String): PsiElement {
    val newElement = JsonElementGenerator(myElement.project).createStringLiteral(newElementName)
    return myElement.replace(newElement)
  }

  abstract fun getCompletionVariants(): List<String>
}
