/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.UsefulTestCase
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hcl.psi.common.BaseExpression
import org.intellij.terraform.config.inspection.TypeSpecificationValidator
import org.intellij.terraform.config.model.*
import org.junit.Assert

class TerraformTypesPresentationTest : LightPlatformTestCase() {
  fun testPrimitiveTypes() {
    doTest(Types.Number, "number")
    doTest(Types.Boolean, "bool")
  }

  fun testComplex() {
    doTest(ListType(null), "list")
    doTest(ListType(Types.Any), "list(any)")
    doTest(ListType(Types.Number), "list(number)")
    doTest(ListType(ListType(Types.Number)), "list(list(number))")

    doTest(SetType(null), "set")
    doTest(SetType(Types.Any), "set(any)")
    doTest(SetType(Types.Number), "set(number)")
    doTest(SetType(SetType(Types.Number)), "set(set(number))")

    doTest(MapType(null), "map")
    doTest(MapType(Types.Any), "map(any)")
    doTest(MapType(Types.Number), "map(number)")
    doTest(MapType(MapType(Types.Number)), "map(map(number))")

    doTest(TupleType(listOf()), "tuple([])")
    doTest(TupleType(listOf(Types.Any)), "tuple([any])")
    doTest(TupleType(listOf(Types.Number)), "tuple([number])")
    doTest(TupleType(listOf(Types.Number, Types.Number)), "tuple([number, number])")

    doTest(ObjectType(emptyMap()), "object")
    doTest(ObjectType(mapOf("a" to Types.Any)), "object({a=any})")
  }

  private fun doTest(type: Type, expected: String) {
    Assert.assertEquals(expected, type.presentableText)
    val validator: TypeSpecificationValidator = object : TypeSpecificationValidator(null, true, true) {
      override fun error(element: PsiElement, description: String, range: TextRange?): Type? {
        Assert.fail("Error at element '" + element.text + "': " + description)
        return null
      }
    }
    val text = "x=$expected"
    val psiFile = PsiFileFactory.getInstance(getProject()).createFileFromText("a.tf", TerraformFileType, text)
    Assert.assertEquals(TerraformLanguage, psiFile.language)
    val root = psiFile.firstChild
    Assert.assertNotNull(root)
    UsefulTestCase.assertInstanceOf(root, HCLProperty::class.java)
    val value = (root as HCLProperty).value
    Assert.assertNotNull(value)
    UsefulTestCase.assertInstanceOf(value, BaseExpression::class.java)
    validator.getType(value!!)
  }
}