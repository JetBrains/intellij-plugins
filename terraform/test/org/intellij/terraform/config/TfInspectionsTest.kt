// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.ui.RenameDialogInterceptor
import com.intellij.ui.UiInterceptors
import org.intellij.terraform.TfTestUtils
import org.intellij.terraform.config.inspection.HCLBlockConflictingPropertiesInspection
import org.intellij.terraform.config.inspection.HCLLiteralValidnessInspection
import org.intellij.terraform.config.inspection.HclBlockMissingPropertyInspection
import org.intellij.terraform.config.inspection.HclBlockNameValidnessInspection
import org.intellij.terraform.config.inspection.HclDuplicatedPropertyInspection
import org.intellij.terraform.config.inspection.TfDuplicatedOutputInspection
import org.intellij.terraform.config.inspection.TfDuplicatedProviderInspection
import org.intellij.terraform.config.inspection.TfDuplicatedVariableInspection
import org.intellij.terraform.config.inspection.TfIncorrectVariableTypeInspection
import org.intellij.terraform.config.inspection.TfNoInterpolationsAllowedInspection
import org.intellij.terraform.config.inspection.TfUnknownBlockTypeInspection
import org.intellij.terraform.config.inspection.TfUnknownPropertyInspection
import org.intellij.terraform.config.inspection.TfUnknownResourceInspection
import org.intellij.terraform.config.inspection.TfUnusedElementsInspection
import org.intellij.terraform.config.inspection.TfVARSIncorrectElementInspection
import org.intellij.terraform.hil.inspection.HILConvertToHCLInspection
import org.intellij.terraform.hil.inspection.HILMissingSelfInContextInspection
import org.intellij.terraform.hil.inspection.HILOperationTypesMismatchInspection
import org.intellij.terraform.hil.inspection.HILUnknownResourceTypeInspection
import org.intellij.terraform.hil.inspection.HILUnresolvedReferenceInspection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
internal class TfInspectionsTest : TfInspectionFixtureTestCase() {

  @BeforeEach
  fun setUpTestDataPath() {
    myFixture.setTestDataPath(basePath)
  }

  override val basePath: String
    get() = TfTestUtils.getTestDataPath() + "/terraform/inspections/"

  @Test
  fun testResourcePropertyReferences() {
    doTest("resource_property_reference", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testMappingVariableReference() {
    doTest("mapping_variable_reference", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testWeirdBlockComputedPropertyReference() {
    doTest("weird_block_computed_property_reference", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testUnresolvedVariable() {
    doTest("unresolved_variable", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testResolveForVariables() {
    doTest("resolve_for_variables", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testResolveDynamicVariables() {
    doTest("resolve_dynamic_variables", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testResolveVariables() {
    doTest("resolve_variables", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testResolveProvider() {
    doTest("resolve_provider", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testModuleComplexOutputReferences() {
    doTest("module_complex_output_references", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testSelfReferences() {
    doTest("self_references", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testSelfReferenceInPostCondition() {
    doTest("self_reference_in_post_condition", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testComplexPropertyKeys() {
    doTest("complex_property_keys", HILUnresolvedReferenceInspection())
  }

  @Test
  fun testKnownBlockNameFromModel() {
    doTest("unknown_block_name", TfUnknownBlockTypeInspection())
  }

  @Test
  fun testUnknownPropertyInResource() {
    doTest("unknown_property_in_resource", TfUnknownPropertyInspection())
  }

  // Test for issue #198
  @Test
  fun testNoUnknownBlocksForNomad() {
    doTest("no_unknown_blocks_for_nomad", TfUnknownBlockTypeInspection())
  }

  @Test
  fun testIncorrectTFVARS() {
    doTest("incorrect_tfvars", TfVARSIncorrectElementInspection())
  }

  @Test
  fun testIncorrectVariableType() {
    doTest("incorrect_variable_type", TfIncorrectVariableTypeInspection())
  }

  @Test
  fun testMapOfAnyVariableType() {
    myFixture.configureByText(TerraformFileType, """
      variable "foo" {
        type = map(any)
        default = {
          "bar" = "a-string"
          "baz" = 10
          "qux" = true
        }
      }
    """.trimIndent())
    myFixture.enableInspections(TfIncorrectVariableTypeInspection())
    myFixture.checkHighlighting()
  }

  @Test
  fun testDuplicatedProvider() {
    doTest("duplicated_provider", TfDuplicatedProviderInspection())
  }

  @Test
  fun testDuplicatedOutput() {
    UiInterceptors.register(RenameDialogInterceptor("newOutput", listOf("a", "a1")))
    UiInterceptors.register(RenameDialogInterceptor("newOutput", listOf("a", "a1")))
    doTest("duplicated_output", TfDuplicatedOutputInspection())
  }

  @Test
  fun testDuplicatedVariable() {
    UiInterceptors.register(RenameDialogInterceptor("newVar", listOf("x", "x1")))
    UiInterceptors.register(RenameDialogInterceptor("newVar", listOf("x", "x1")))
    doTest("duplicated_variable", TfDuplicatedVariableInspection())
  }

  @Test
  fun testDuplicatedBlockProperty() {
    doTest("duplicated_block_property", HclDuplicatedPropertyInspection())
  }

  @Test
  fun testInterpolationsInWrongPlaces() {
    doTest("interpolations_in_wrong_places", TfNoInterpolationsAllowedInspection())
  }

  @Test
  fun testMissingBlockProperty() {
    doTest("missing_properties", HclBlockMissingPropertyInspection())
  }

  @Test
  fun testMissingPropertyConflictsWith() {
    doTest("missing_property_conflicts_with", HclBlockMissingPropertyInspection())
  }

  // ignored, because no "Conflicts" medatada is provided in recent updates
  fun _testConflictingBlockProperty() {
    doTest("conflicting_properties", HCLBlockConflictingPropertiesInspection())
  }

  @Test
  fun testMissingSelfInContext() {
    doTest("reference_to_self", HILMissingSelfInContextInspection())
  }

  @Test
  fun testInterpolationBinaryExpressionsTypesCheck() {
    myFixture.enableInspections(HILOperationTypesMismatchInspection())
    myFixture.configureByFile("interpolation_operations_types/src/test.tf")
    myFixture.checkHighlighting()
    myFixture.configureByFile("interpolation_operations_types/src/ternary.tf")
    myFixture.checkHighlighting()
  }

  @Test
  fun testConvertHILToHCL() {
    doTest("convert_hil_to_hcl", HILConvertToHCLInspection())
  }

  @Test
  fun testNoConvertHILToHCL() {
    doTest("no_convert_hil_to_hcl", HILConvertToHCLInspection())
  }

  @Test
  fun testSpellchecking() {
    doTest("spellchecking", GrazieSpellCheckingInspection())
  }

  @Test
  fun testSpellcheckingInDependsOn() {
    doTest("spellchecking_depends_on", GrazieSpellCheckingInspection())
  }

  @Test
  fun testSpellcheckingInHashesProperty() {
    doTest("spellchecking_hashes_property", GrazieSpellCheckingInspection())
  }

  @Test
  fun testSpellcheckingForBlockIdentifiers() {
    doTest("spellchecking_block_identifiers", GrazieSpellCheckingInspection())
  }

  @Test
  fun testUnknownResourceType() {
    doTest("unknown_resource_type", HILUnknownResourceTypeInspection())
  }

  @Test
  fun testDoubleQuotedStringLiteral() {
    doTest("double_quoted_string_literal", HCLLiteralValidnessInspection())
  }

  @Test
  fun testStringLiteral() {
    doTest("check_string_literal", HCLLiteralValidnessInspection())
  }

  @Test
  fun testUnknownResource() {
    doTest("unknown_resource", TfUnknownResourceInspection())
  }

  @Test
  fun testGoogleAndGoogleBetaResources() {
    doTest("google_beta_resource", TfUnknownResourceInspection())
  }

  @Test
  fun testUnusedVariableAndLocals() {
    doTest("unused_elements", TfUnusedElementsInspection())
  }

  @Test
  fun testHclBlockWithEmptyName() {
    UiInterceptors.register(RenameDialogInterceptor("new_name"))
    doTest("hcl_block_with_empty_name", HclBlockNameValidnessInspection())
  }
}