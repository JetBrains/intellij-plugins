// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.findAllReferencesByText
import com.intellij.testFramework.findReferenceByText
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.asSafely
import junit.framework.TestCase
import org.intellij.terraform.config.inspection.TFIncorrectVariableTypeInspection
import org.intellij.terraform.hcl.psi.HCLProperty
import org.intellij.terraform.hil.inspection.HILUnknownResourceTypeInspection
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TerraformReferencesTest : BasePlatformTestCase() {

  @Test
  fun forEachOnObject() {
    myFixture.configureByText("main.tf", """
      locals {
        virtual_machines = [
          {
            ip_address = "10.0.0.1"
            name       = "vm-1"
          },
          {
            ip_address = "10.0.0.1"
            name       = "vm-2"
          }
        ]
      }

      resource "example" "example" {
        for_each = {
        for index, vm in local.virtual_machines :
        vm.name => vm
        }
        name       = each.value.name
        ip_address = each.value.ip_<caret>address
      }
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPosition()!!
    assertResolvedNames(ref, "ip_address", "ip_address")
  }

  private fun assertResolvedNames(ref: PsiReference, vararg names: String?) {

    fun asAssertString(elt: PsiElement?): String? = elt.asSafely<HCLProperty>()?.name ?: elt?.toString()

    when (ref) {
      is PsiMultiReference ->
        ref.references.map { ref ->
          ref.resolve().also { if(ref.isSoft.not())TestCase.assertNotNull("reference $ref should be resolved", it) }
        }.map(::asAssertString).toList()
      is PsiPolyVariantReference ->
        ref.multiResolve(false).map { (it.element as HCLProperty).name }.toList()
      else ->
        listOf(asAssertString(ref.resolve()))
    }.let { strings ->
      UsefulTestCase.assertOrderedEquals(strings, *names)
    }
  }

  @Test
  fun forEachOnInst() {
    myFixture.configureByText("main.tf", """
      locals {
        insts = {
          aa = 1
          bb = 2
          cc = 3
        }
      }

      resource "aws_instance" "example" {
        for_each = local.insts
        ami = each.value.a<caret>a
      }
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPosition()!!
    assertResolvedNames(ref, "aa")
  }

  @Test
  fun forGenerator() {
    myFixture.configureByText("main.tf", """
      locals {      
          bar = [
              for i in range(0,1):
              {
                  name: "value",
              }
          ]
      }

      output "server" {
          value = local.bar[*].na<caret>me 
      }
    """.trimIndent())
    val ref = myFixture.getReferenceAtCaretPosition()!!
    assertResolvedNames(ref, "name")
  }

  @Test
  fun forGeneratorWithArrow() {
    val file = myFixture.configureByText("main.tf", """
      resource "aws_route53_record" "example" {
        for_each = {
        for dvo in aws_acm_certificate.example.domain_validation_options : dvo.domain_name => {
          name   = dvo.resource_record_name
          record = dvo.resource_record_value
          type   = dvo.resource_record_type
        }
        }

        allow_overwrite = true
        name            = each.value.name
        records         = [each.value.record]
        ttl             = 60
        type            = each.value.type
        zone_id         = data.aws_route53_zone.example.zone_id
      }
    """.trimIndent())

    assertResolvedNames(file.findReferenceByText("each.value.name", -1), "name")
    assertResolvedNames(file.findReferenceByText("each.value.record", -1), "record")
    assertResolvedNames(file.findReferenceByText("each.value.type", -1), "type")
  }

  @Test
  fun nestedLoopsResolve() {
    val file = terraformFIle("""for user in local.users : [
            {
              name : user.username
              els : [for p in user.innerarr : p.name1]
            }
          ]""")

    assertResolvedNames(file.findReferenceByText("user.username", -1), "username")
    assertResolvedNames(file.findReferenceByText("user.innerarr", -1), "innerarr")
    assertResolvedNames(file.findReferenceByText("p.name1", -1), "name1")
  }

  @Test
  fun loopVariableCompletion() {
    terraformFIle("""for user in local.users : [
            {
              name : <caret>
            }
          ]""")

    UsefulTestCase.assertContainsElements(myFixture.getCompletionVariants("main.tf")!!, "user")
  }

  @Test
  fun loopVariableSelectCompletion() {
    terraformFIle("""for user in local.users : [
            {
              name : user.<caret>
            }
          ]""")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "username")
    UsefulTestCase.assertDoesntContain(completionVariants, "user")
    UsefulTestCase.assertDoesntContain(completionVariants, "name1", "name2", "aa", "key1")
  }

  @Test
  fun nonLoopVariableSelectCompletion() {
    terraformFIle("""local.users.<caret>""")
    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "aa")
    UsefulTestCase.assertDoesntContain(completionVariants, "name1", "name2", "key1", "username", "innerarr")
  }

  @Test
  fun loopVariableNestedCompletion() {
    terraformFIle("""for user in local.users : [
            {
              name : user.username
              els : [for p in user.<caret>]
            }
          ]""")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "innerarr")
  }

  @Test
  fun loopVariableNestedSelectCompletion() {
    terraformFIle("""for user in local.users : [
            {
              name : user.username
              els : [for p in user.innerarr : p.<caret>]
            }
          ]""")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "name1")
    UsefulTestCase.assertDoesntContain(completionVariants, "username")
  }

  private fun terraformFIle(loop: String): PsiFile = myFixture.configureByText("main.tf", """
        locals {
          users = {
            aa : {
              username = "aaa"
              innerarr = {
                key1 : {
                  name1 = 1
                  name2 = 2
                }
              }
            }
          }
        }
        
        locals {
          policies = [
            $loop
          ]
        }
      """.trimIndent())

  @Test
  fun forItemString() {
    myFixture.enableInspections(HILUnknownResourceTypeInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
        locals {
          object_items = [
            {
              aa = "1"
              b = "2"
            },
            {
              aa = "3"
              b = "4"
            }
          ]
          as_list_non_string = [
          for item in local.object_items : item.aa
          ]
          as_map = {
          for item in local.object_items : "key-$DLR{item.aa}" => "value-$DLR{item.b}" 
          }
          as_list = [
          for item in local.object_items : "just-$DLR{item.aa}-and-unresolved$DLR{<warning descr="Unknown resource type">unresolved</warning>.aa}"
          ]

          list_items = [["a"], ["b"], ["c"]]
          as_list2 = [
          for item in local.list_items : "text-$DLR{item[0]}"
          ]
        }
    """.trimIndent())
    myFixture.checkHighlighting()
    for (psiReference in file.findAllReferencesByText("item.aa", -1).toList().also { UsefulTestCase.assertSize(3, it) }) {
      assertResolvedNames(psiReference, "aa", "aa")
    }
  }

  @Test
  fun forItemStringCompletion() {
    myFixture.enableInspections(HILUnknownResourceTypeInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
        locals {
          object_items = [
            {
              aa = "1"
              b = "2"
            },
            {
              aa = "3"
              b = "4"
            }
          ]
          as_list_non_string = [
          for item in local.object_items : item.aa
          ]
          as_map = {
          for item in local.object_items : "key-$DLR{item.<caret>}" => "value-$DLR{item.b}" 
          }
        }
    """.trimIndent())

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "aa")
  }

  @Test
  fun listOfObjectTypeVariableForEach() {
    myFixture.enableInspections(HILUnknownResourceTypeInspection::class.java, HILUnresolvedReferenceInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
      variable "my_var" {
        type = list(
          object({
            f1 = string,
            f2 = string,
            f3 = string,
            f4 = string,
          })
        )
        default = []
      }

      resource "usage" "example" {
        for_each = {for each in var.my_var : each.<caret>f1 => each}
        u1 = each.value.f2
      }
    """.trimIndent())

    myFixture.checkHighlighting()
    assertResolvedNames(file.findReferenceByText("each.f1", -1), "f1", null)
    val psiReference = file.findReferenceByText("each.value.f2", -1)
    assertResolvedNames(psiReference, "f2")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "f1", "f2", "f3", "f4")
  }
  
  @Test
  fun reassignedInputVariable() {
    myFixture.enableInspections(HILUnknownResourceTypeInspection::class.java, HILUnresolvedReferenceInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
      variable "input_variable" {
        type = object({
          attribute_one = string
          attribute_two = string
          attribute_three = string
        })
      }
      
      locals {
         local_variable = var.input_variable
      }
      
      resource "my_resource" "example" {
         argument_key = local.local_variable.<caret>attribute_one
      }
    """.trimIndent())

    myFixture.checkHighlighting()
    assertResolvedNames(file.findReferenceByText("local_variable.attribute_one", -1), "attribute_one")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "attribute_one", "attribute_three", "attribute_two")
  }
  
  @Test
  fun nestedTypesCompletion() {
    myFixture.enableInspections(HILUnknownResourceTypeInspection::class.java, HILUnresolvedReferenceInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
        variable "context" {
          type = object({
            env     = string
            service = object({
              name      = string
              role_name = string
            })
          })
        }
        
        output "service_name" {
          value = var.context.service.<caret>name
        }
    """.trimIndent())

    myFixture.checkHighlighting()
    assertResolvedNames(file.findReferenceByText("service.name", -1), "name")

    val completionVariants = myFixture.getCompletionVariants("main.tf")!!
    UsefulTestCase.assertContainsElements(completionVariants, "name", "role_name")
  }
  
  @Test
  fun variableTypesSOE() {
    myFixture.enableInspections(TFIncorrectVariableTypeInspection::class.java)
    val file = myFixture.configureByText("main.tf", """
      variable "input_variable" {
        type = list(object({
          attribute_one   = string
          attribute_two   = string
          attribute_three = string
        }))
      
        default = <warning>{
          attribute_one   = "test"
          attribute_two   = var.input_variable
          attribute_three = "test3"
        }</warning>
      
      }
      
      locals {
        local_variable = var.input_variable.attribute_two
      }
    """.trimIndent())
    myFixture.checkHighlighting()
    
  }

  private val DLR = "$"

}