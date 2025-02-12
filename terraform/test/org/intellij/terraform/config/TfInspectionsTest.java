// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.config.inspection.*;
import org.intellij.terraform.hil.inspection.*;

public class TfInspectionsTest extends TfInspectionFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(getBasePath());
  }

  @Override
  protected String getBasePath() {
    return TfTestUtils.getTestDataPath() + "/terraform/inspections/";
  }

  public void testResourcePropertyReferences() {
    doTest("resource_property_reference", new HILUnresolvedReferenceInspection());
  }

  public void testMappingVariableReference() {
    doTest("mapping_variable_reference", new HILUnresolvedReferenceInspection());
  }

  public void testWeirdBlockComputedPropertyReference() {
    doTest("weird_block_computed_property_reference", new HILUnresolvedReferenceInspection());
  }

  public void testUnresolvedVariable() {
    doTest("unresolved_variable", new HILUnresolvedReferenceInspection());
  }

  public void testResolveForVariables() {
    doTest("resolve_for_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveDynamicVariables() {
    doTest("resolve_dynamic_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveVariables() {
    doTest("resolve_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveProvider() {
    doTest("resolve_provider", new HILUnresolvedReferenceInspection());
  }

  public void testModuleComplexOutputReferences() {
    doTest("module_complex_output_references", new HILUnresolvedReferenceInspection());
  }

  public void testSelfReferences() {
    doTest("self_references", new HILUnresolvedReferenceInspection());
  }

  public void testComplexPropertyKeys() {
    doTest("complex_property_keys", new HILUnresolvedReferenceInspection());
  }

  public void testKnownBlockNameFromModel() {
    doTest("unknown_block_name", new TfUnknownBlockTypeInspection());
  }

  public void testUnknownPropertyInResource() {
    doTest("unknown_property_in_resource", new TfUnknownPropertyInspection());
  }

  // Test for issue #198
  public void testNoUnknownBlocksForNomad() {
    doTest("no_unknown_blocks_for_nomad", new TfUnknownBlockTypeInspection());
  }

  public void testIncorrectTFVARS() {
    doTest("incorrect_tfvars", new TfVARSIncorrectElementInspection());
  }

  public void testIncorrectVariableType() {
    doTest("incorrect_variable_type", new TfIncorrectVariableTypeInspection());
  }

  public void testDuplicatedProvider() {
    doTest("duplicated_provider", new TfDuplicatedProviderInspection());
  }

  public void testDuplicatedOutput() {
    doTest("duplicated_output", new TfDuplicatedOutputInspection());
  }

  public void testDuplicatedVariable() {
    doTest("duplicated_variable", new TfDuplicatedVariableInspection());
  }

  public void testDuplicatedBlockProperty() {
    doTest("duplicated_block_property", new TfDuplicatedBlockPropertyInspection());
  }

  public void testInterpolationsInWrongPlaces() {
    doTest("interpolations_in_wrong_places", new TfNoInterpolationsAllowedInspection());
  }

  public void testMissingBlockProperty() {
    doTest("missing_properties", new HCLBlockMissingPropertyInspection());
  }

  // ignored, because no "Conflicts" medatada is provided in recent updates
  public void _testConflictingBlockProperty() {
    doTest("conflicting_properties", new HCLBlockConflictingPropertiesInspection());
  }

  public void testMissingSelfInContext() {
    doTest("reference_to_self", new HILMissingSelfInContextInspection());
  }

  public void testInterpolationBinaryExpressionsTypesCheck() {
    doTest("interpolation_operations_types", new HILOperationTypesMismatchInspection());
  }

  public void testConvertHILToHCL() {
    doTest("convert_hil_to_hcl", new HILConvertToHCLInspection());
  }

  public void testNoConvertHILToHCL() {
    doTest("no_convert_hil_to_hcl", new HILConvertToHCLInspection());
  }

  public void testSpellchecking() {
    doTest("spellchecking", new SpellCheckingInspection());
  }

  public void testSpellcheckingInDependsOn() {
    doTest("spellchecking_depends_on", new SpellCheckingInspection());
  }

  public void testSpellcheckingInHashesProperty() {
    doTest("spellchecking_hashes_property", new SpellCheckingInspection());
  }

  public void testSpellcheckingForBlockIdentifiers() {
    doTest("spellchecking_block_identifiers", new SpellCheckingInspection());
  }

  public void testUnknownResourceType() {
    doTest("unknown_resource_type", new HILUnknownResourceTypeInspection());
  }

  public void testDoubleQuotedStringLiteral() {
    doTest("double_quoted_string_literal", new HCLLiteralValidnessInspection());
  }

  public void testStringLiteral() {
    doTest("check_string_literal", new HCLLiteralValidnessInspection());
  }

  public void testUnknownResource() {
    doTest("unknown_resource", new TfUnknownResourceInspection());
  }

  public void testUnusedVariableAndLocals() {
    doTest("unused_elements", new TfUnusedElementsInspection());
  }
}
