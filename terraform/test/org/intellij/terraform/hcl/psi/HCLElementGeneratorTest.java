// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl.psi;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.LightPlatformTestCase;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HCLElementGeneratorTest extends LightPlatformTestCase {
  protected HCLElementGenerator myElementGenerator;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    myElementGenerator = createElementGenerator();
  }

  @NotNull
  protected HCLElementGenerator createElementGenerator() {
    return new HCLElementGenerator(getProject());
  }


  public void testCreateIdentifier() throws Exception {
    final HCLIdentifier element = myElementGenerator.createIdentifier("id");
    assertEquals("id", element.getId());
    assertEquals("id", element.getName());
  }

  public void testCreateStringLiteral() throws Exception {
    final HCLStringLiteral element = myElementGenerator.createStringLiteral("literal", '"');
    assertEquals("literal", element.getValue());
    assertEquals("literal", element.getName());
    final List<Pair<TextRange, String>> fragments = element.getTextFragments();
    assertEquals(1, fragments.size());
    assertEquals("literal", fragments.iterator().next().second);
  }

  public void testCreateProperty() throws Exception {
    final HCLProperty element = myElementGenerator.createProperty("n", "'v'");
    assertEquals("n", element.getName());
    final HCLValue value = (HCLValue) element.getValue();
    assertNotNull(value);
    assertTrue(value instanceof HCLStringLiteral);
    HCLStringLiteral literal = (HCLStringLiteral) value;
    assertEquals("v", literal.getValue());
  }

  public void testCreateStringValue() throws Exception {
    HCLValue element = myElementGenerator.createValue("'v'");
//    assertEquals("v", element.getName());
    assertTrue(element instanceof HCLStringLiteral);
    HCLStringLiteral literal = (HCLStringLiteral) element;
    assertEquals("v", literal.getValue());

  }

  public void testCreateIdentifierValue() throws Exception {
    HCLValue element = myElementGenerator.createValue("id");
    assertTrue(element instanceof HCLIdentifier);
    assertEquals("id", ((HCLIdentifier) element).getId());
  }

  public void testCreateBooleanValue() throws Exception {
    HCLValue element = myElementGenerator.createValue("true");
    assertTrue(element instanceof HCLBooleanLiteral);
    assertTrue(((HCLBooleanLiteral) element).getValue());
    element = myElementGenerator.createValue("false");
    assertTrue(element instanceof HCLBooleanLiteral);
    assertFalse(((HCLBooleanLiteral) element).getValue());
  }

  public void testCreateNumericalValue() throws Exception {
    HCLValue element = myElementGenerator.createValue("42");
    assertTrue(element instanceof HCLNumberLiteral);
    Number value = ((HCLNumberLiteral) element).getValue();
    assertInstanceOf(value, Integer.class);
    assertEquals(42, value);
    element = myElementGenerator.createValue("42.0");
    assertTrue(element instanceof HCLNumberLiteral);
    value = ((HCLNumberLiteral) element).getValue();
    assertInstanceOf(value, Double.class);
    assertEquals(42.0, value);
  }

  public void testCreateObject() throws Exception {
    final HCLObject element = myElementGenerator.createObject("a=1");
    final HCLProperty property = element.findProperty("a");
    assertNotNull(property);
    assertEquals("a", property.getName());
  }

  public void testCreateBlock() throws Exception {
    HCLBlock element;
    element = myElementGenerator.createBlock("bbb");
    assertEquals("bbb", element.getName());
    assertEquals(1, element.getNameElements().length);
    assertInstanceOf(element.getNameIdentifier(), HCLIdentifier.class);
    element = myElementGenerator.createBlock("b-b");
    assertEquals("b-b", element.getName());
    assertEquals(1, element.getNameElements().length);
    assertInstanceOf(element.getNameIdentifier(), HCLStringLiteral.class);
    element = myElementGenerator.createBlock("0b");
    assertEquals("0b", element.getName());
    assertEquals(1, element.getNameElements().length);
    assertInstanceOf(element.getNameIdentifier(), HCLStringLiteral.class);
  }

  public void testCreatePropertyKey() throws Exception {
    HCLExpression element;
    element = myElementGenerator.createPropertyKey("id.b");
    assertInstanceOf(element, HCLSelectExpression.class);
    assertEquals("id.b", element.getText());
    element = myElementGenerator.createPropertyKey("id");
    assertInstanceOf(element, HCLIdentifier.class);
    assertEquals("id", element.getText());
    element = myElementGenerator.createPropertyKey("\"id\"");
    assertInstanceOf(element, HCLStringLiteral.class);
    assertEquals("\"id\"", element.getText());
  }

//  public void testCreateHereDocLines() throws Exception {
//    final List<String> strings = Arrays.asList("A", "B", "C");
//    final List<HCLHeredocLine> lines = myElementGenerator.createHeredocLines(strings);
//    assertNotNull(lines);
//    List<String> actual = new ArrayList<String>(lines.size());
//    for (HCLHeredocLine line : lines) {
//      actual.add(StringUtil.trimEnd(line.getValue(), "\n"));
//    }
//    assertOrderedEquals(actual, strings);
//  }

//  public void testCreateHereDocLine() throws Exception {
//    final HCLHeredocLine line = myElementGenerator.createHeredocLine("ABC");
//    assertNotNull(line);
//    assertEquals("ABC" + "\n", line.getValue());
//  }

//  public void testCreate() throws Exception {
//  }
}
