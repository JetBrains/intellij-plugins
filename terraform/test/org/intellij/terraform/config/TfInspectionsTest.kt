// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config

import com.intellij.grazie.spellcheck.GrazieSpellCheckingInspection
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

internal class TfInspectionsTest : TfInspectionFixtureTestCase() {

  override fun setUp() {
    super.setUp()
    myFixture.setTestDataPath(basePath)
  }

  override fun getBasePath(): String {
    return TfTestUtils.getTestDataPath() + "/terraform/inspections/"
  }

  fun testResourcePropertyReferences() {
    doTest("resource_property_reference", HILUnresolvedReferenceInspection())
  }

  fun testMappingVariableReference() {
    doTest("mapping_variable_reference", HILUnresolvedReferenceInspection())
  }

  fun testWeirdBlockComputedPropertyReference() {
    doTest("weird_block_computed_property_reference", HILUnresolvedReferenceInspection())
  }

  fun testUnresolvedVariable() {
    doTest("unresolved_variable", HILUnresolvedReferenceInspection())
  }

  fun testResolveForVariables() {
    doTest("resolve_for_variables", HILUnresolvedReferenceInspection())
  }

  fun testResolveDynamicVariables() {
    doTest("resolve_dynamic_variables", HILUnresolvedReferenceInspection())
  }

  fun testResolveVariables() {
    doTest("resolve_variables", HILUnresolvedReferenceInspection())
  }

  fun testResolveProvider() {
    doTest("resolve_provider", HILUnresolvedReferenceInspection())
  }

  fun testModuleComplexOutputReferences() {
    doTest("module_complex_output_references", HILUnresolvedReferenceInspection())
  }

  fun testSelfReferences() {
    doTest("self_references", HILUnresolvedReferenceInspection())
  }

  fun testSelfReferenceInPostCondition() {
    doTest("self_reference_in_post_condition", HILUnresolvedReferenceInspection())
  }

  fun testComplexPropertyKeys() {
    doTest("complex_property_keys", HILUnresolvedReferenceInspection())
  }

  fun testKnownBlockNameFromModel() {
    doTest("unknown_block_name", TfUnknownBlockTypeInspection())
  }

  fun testUnknownPropertyInResource() {
    doTest("unknown_property_in_resource", TfUnknownPropertyInspection())
  }

  // Test for issue #198
  fun testNoUnknownBlocksForNomad() {
    doTest("no_unknown_blocks_for_nomad", TfUnknownBlockTypeInspection())
  }

  fun testIncorrectTFVARS() {
    doTest("incorrect_tfvars", TfVARSIncorrectElementInspection())
  }

  fun testIncorrectVariableType() {
    doTest("incorrect_variable_type", TfIncorrectVariableTypeInspection())
  }

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

  fun testDuplicatedProvider() {
    doTest("duplicated_provider", TfDuplicatedProviderInspection())
  }

  fun testDuplicatedOutput() {
    UiInterceptors.register(RenameDialogInterceptor("newOutput", listOf("a", "a1")))
    UiInterceptors.register(RenameDialogInterceptor("newOutput", listOf("a", "a1")))
    doTest("duplicated_output", TfDuplicatedOutputInspection())
  }

  fun testDuplicatedVariable() {
    UiInterceptors.register(RenameDialogInterceptor("newVar", listOf("x", "x1")))
    UiInterceptors.register(RenameDialogInterceptor("newVar", listOf("x", "x1")))
    doTest("duplicated_variable", TfDuplicatedVariableInspection())
  }

  fun testDuplicatedBlockProperty() {
    doTest("duplicated_block_property", HclDuplicatedPropertyInspection())
  }

  fun testInterpolationsInWrongPlaces() {
    doTest("interpolations_in_wrong_places", TfNoInterpolationsAllowedInspection())
  }

  fun testMissingBlockProperty() {
    doTest("missing_properties", HclBlockMissingPropertyInspection())
  }

  fun testMissingPropertyConflictsWith() {
    doTest("missing_property_conflicts_with", HclBlockMissingPropertyInspection())
  }

  // ignored, because no "Conflicts" medatada is provided in recent updates
  fun _testConflictingBlockProperty() {
    doTest("conflicting_properties", HCLBlockConflictingPropertiesInspection())
  }

  fun testMissingSelfInContext() {
    doTest("reference_to_self", HILMissingSelfInContextInspection())
  }

  fun testInterpolationBinaryExpressionsTypesCheck() {
    doTest("interpolation_operations_types", HILOperationTypesMismatchInspection())
  }

  fun testConvertHILToHCL() {
    doTest("convert_hil_to_hcl", HILConvertToHCLInspection())
  }

  fun testNoConvertHILToHCL() {
    doTest("no_convert_hil_to_hcl", HILConvertToHCLInspection())
  }

  fun testSpellchecking() {
    doTest("spellchecking", GrazieSpellCheckingInspection())
  }

  fun testSpellcheckingInDependsOn() {
    doTest("spellchecking_depends_on", GrazieSpellCheckingInspection())
  }

  fun testSpellcheckingInHashesProperty() {
    doTest("spellchecking_hashes_property", GrazieSpellCheckingInspection())
  }

  fun testSpellcheckingForBlockIdentifiers() {
    doTest("spellchecking_block_identifiers", GrazieSpellCheckingInspection())
  }

  fun testUnknownResourceType() {
    doTest("unknown_resource_type", HILUnknownResourceTypeInspection())
  }

  fun testDoubleQuotedStringLiteral() {
    doTest("double_quoted_string_literal", HCLLiteralValidnessInspection())
  }

  fun testStringLiteral() {
    doTest("check_string_literal", HCLLiteralValidnessInspection())
  }

  fun testUnknownResource() {
    doTest("unknown_resource", TfUnknownResourceInspection())
  }

  fun testGoogleAndGoogleBetaResources() {
    doTest("google_beta_resource", TfUnknownResourceInspection())
  }

  fun testUnusedVariableAndLocals() {
    doTest("unused_elements", TfUnusedElementsInspection())
  }

  fun testHclBlockWithEmptyName() {
    UiInterceptors.register(RenameDialogInterceptor("new_name"))
    doTest("hcl_block_with_empty_name", HclBlockNameValidnessInspection())
  }
}