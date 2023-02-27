// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.UsefulTestCase
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.config.inspection.TypeSpecificationValidator
import org.intellij.terraform.config.model.Type
import org.intellij.terraform.config.model.getCommonSupertype

class TerraformTypesGetCommonSupertypeTest : LightPlatformTestCase() {

  fun testObjectTypes() {
    doTest("object({x=number})", "object({x=string})", "object({x=number})")
    doTest("object({x=string})", "object({x=string})", "object({x=number})", "object({x=bool})")

    doTest("object({x=number, y=string})",
        "object({x=number, y=string})", "object({x=number, y=string})")
    doTest("object({x=number, y=string})",
        "object({x=number, y=string})", "object({x=number})")
    doTest("object({x=number, y=string})",
        "object({x=number})", "object({x=number, y=string})")
    doTest("object({x=number, y=string})",
        "object({x=number, y=string})", "object({y=string})")
    doTest("object({x=number, y=string})",
        "object({y=string})", "object({x=number, y=string})")
  }

  fun testMapAndObjectTypes() {
    doTest("map(number)",
        "object({x=number})", "map(number)")

    doTest("map(object({a=number, b=string}))",
        "map(object({a=number, b=string}))",
        "object({x=object({a=number}), y=object({a=number})})")
  }

  fun testOptionalTypes() {
    doTest("number",
        "optional(number)", "number")
    doTest("object({x=number, y=optional(number)})",
        "object({x=number, y=optional(number)})", "object({x=number})")
    doTest("object({x=number, y=number})",
        "object({x=number, y=optional(number)})", "object({x=number, y=number})")
  }

  private fun doTest(expected: String, vararg from: String) {
    val actual = getCommonSupertype(from.toList().map { getTypeFromString(it) })
    assertEquals(getTypeFromString(expected), actual)
  }

  private fun getTypeFromString(input: String): Type {
    val validator: TypeSpecificationValidator = object : TypeSpecificationValidator(null, true, true) {
      override fun error(element: PsiElement, description: String, range: TextRange?): Type? {
        fail("Error at element '" + element.text + "': " + description)
        return null
      }
    }
    val text = "x=$input"
    val psiFile = PsiFileFactory.getInstance(project).createFileFromText("a.tf", TerraformFileType, text)
    assertEquals(TerraformLanguage, psiFile.language)
    val root = psiFile.firstChild
    assertNotNull(root)
    UsefulTestCase.assertInstanceOf(root, HCLProperty::class.java)
    val value = (root as HCLProperty).value
    assertNotNull(value)
    UsefulTestCase.assertInstanceOf(value, BaseExpression::class.java)
    val type = validator.getType(value!!)
    assertNotNull(type)
    return type!!
  }
}