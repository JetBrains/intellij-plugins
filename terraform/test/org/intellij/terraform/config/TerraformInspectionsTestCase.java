// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config;

import com.intellij.spellchecker.inspections.SpellCheckingInspection;
import org.intellij.terraform.TerraformTestUtils;
import org.intellij.terraform.config.inspection.*;
import org.intellij.terraform.hil.inspection.*;

public class TerraformInspectionsTestCase extends TerraformInspectionFixtureTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myFixture.setTestDataPath(getBasePath());
  }

  @Override
  protected String getBasePath() {
    return TerraformTestUtils.getTestDataPath() + "/terraform/inspections/";
  }

  public void testResourcePropertyReferences() throws Exception {
    doTest("resource_property_reference", new HILUnresolvedReferenceInspection());
  }

  public void testMappingVariableReference() throws Exception {
    doTest("mapping_variable_reference", new HILUnresolvedReferenceInspection());
  }

  public void testWeirdBlockComputedPropertyReference() throws Exception {
    doTest("weird_block_computed_property_reference", new HILUnresolvedReferenceInspection());
  }

  public void testUnresolvedVariable() throws Exception {
    doTest("unresolved_variable", new HILUnresolvedReferenceInspection());
  }

  public void testResolveForVariables() throws Exception {
    doTest("resolve_for_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveDynamicVariables() throws Exception {
    doTest("resolve_dynamic_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveVariables() throws Exception {
    doTest("resolve_variables", new HILUnresolvedReferenceInspection());
  }

  public void testResolveProvider() throws Exception {
    doTest("resolve_provider", new HILUnresolvedReferenceInspection());
  }

  public void testModuleComplexOutputReferences() throws Exception {
    doTest("module_complex_output_references", new HILUnresolvedReferenceInspection());
  }

  public void testSelfReferences() throws Exception {
    doTest("self_references", new HILUnresolvedReferenceInspection());
  }

  public void testComplexPropertyKeys() throws Exception {
    doTest("complex_property_keys", new HILUnresolvedReferenceInspection());
  }

  public void testKnownBlockNameFromModel() throws Exception {
    doTest("unknown_block_name", new HCLUnknownBlockTypeInspection());
  }

  // Test for issue #198
  public void testNoUnknownBlocksForNomad() throws Exception {
    doTest("no_unknown_blocks_for_nomad", new HCLUnknownBlockTypeInspection());
  }

  public void testIncorrectTFVARS() throws Exception {
    doTest("incorrect_tfvars", new TFVARSIncorrectElementInspection());
  }

  public void testIncorrectVariableType() throws Exception {
    doTest("incorrect_variable_type", new TFIncorrectVariableTypeInspection());
  }

  public void testDuplicatedProvider() throws Exception {
    doTest("duplicated_provider", new TFDuplicatedProviderInspection());
  }

  public void testDuplicatedOutput() throws Exception {
    doTest("duplicated_output", new TFDuplicatedOutputInspection());
  }

  public void testDuplicatedVariable() throws Exception {
    doTest("duplicated_variable", new TFDuplicatedVariableInspection());
  }

  public void testDuplicatedBlockProperty() throws Exception {
    doTest("duplicated_block_property", new TFDuplicatedBlockPropertyInspection());
  }

  public void testInterpolationsInWrongPlaces() throws Exception {
    doTest("interpolations_in_wrong_places", new TFNoInterpolationsAllowedInspection());
  }

  public void testMissingBlockProperty() throws Exception {
    doTest("missing_properties", new HCLBlockMissingPropertyInspection());
  }

  // ignored, because no "Conflicts" medatada is provided in recent updates
  public void _testConflictingBlockProperty() throws Exception {
    doTest("conflicting_properties", new HCLBlockConflictingPropertiesInspection());
  }

  public void testMissingSelfInContext() throws Exception {
    doTest("reference_to_self", new HILMissingSelfInContextInspection());
  }

  public void testInterpolationBinaryExpressionsTypesCheck() throws Exception {
    doTest("interpolation_operations_types", new HILOperationTypesMismatchInspection());
  }

  public void testConvertHILToHCL() throws Exception {
    doTest("convert_hil_to_hcl", new HILConvertToHCLInspection());
  }

  public void testNoConvertHILToHCL() throws Exception {
    doTest("no_convert_hil_to_hcl", new HILConvertToHCLInspection());
  }

  public void testSpellchecking() throws Exception {
    doTest("spellchecking", new SpellCheckingInspection());
  }

  public void testUnknownResourceType() throws Exception {
    doTest("unknown_resource_type", new HILUnknownResourceTypeInspection());
  }

}
