// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.psi;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import org.intellij.terraform.hcl.psi.*;
import org.intellij.terraform.config.model.Types;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TerraformElementGeneratorTest extends HCLElementGeneratorTest {
  @NotNull
  @Override
  protected HCLElementGenerator createElementGenerator() {
    return new TerraformElementGenerator(getProject());
  }

  public void testCreateVariable() throws Exception {
    TerraformElementGenerator generator = (TerraformElementGenerator) myElementGenerator;
    HCLBlock element = generator.createVariable("name", Types.INSTANCE.getString(), "\"42\"");
    assertEquals("name", element.getName());
    HCLObject object = element.getObject();
    assertNotNull(object);
    HCLProperty property = object.findProperty("default");
    assertNotNull(property);
    HCLValue value = (HCLValue) property.getValue();
    assertNotNull(value);
    assertTrue(value instanceof HCLStringLiteral);
    assertEquals("42", ((HCLStringLiteral) value).getValue());
  }

  public void testStringTestFragments() {
    doStringFragmentsTest("literal", "literal");
    doStringFragmentsTest("a${\"b\"}c", "a", "${\\\"b\\\"}", "c");
    doStringFragmentsTest("a%{\"b\"}c", "a", "%{\\\"b\\\"}", "c");
  }

  private void doStringFragmentsTest(String input, String... expected) {
    final HCLStringLiteral element = myElementGenerator.createStringLiteral(input, '"');
    final List<Pair<TextRange, String>> fragments = element.getTextFragments();
    assertEquals(Arrays.asList(expected), fragments.stream().map(it -> it.second).collect(Collectors.toList()));
  }
}
