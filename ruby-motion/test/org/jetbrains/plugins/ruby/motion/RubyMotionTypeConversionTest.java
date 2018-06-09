/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.motion;

import org.jetbrains.plugins.ruby.motion.symbols.MotionSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolFilterFactory;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.*;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.collections.RCollectionType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.REmptyType;

/**
 * @author Dennis.Ushakov
 */
public class RubyMotionTypeConversionTest extends RubyMotionLightFixtureTestCase {
  @Override
  protected String getTestDataRelativePath() {
    return "testApp";
  }

  public void testBoolean() {
    defaultConfigure();
    RType type = MotionSymbolUtil.getTypeByName(getModule(), "bool");
    assertEquals("TrueClass or FalseClass", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "BOOL");
    assertEquals("TrueClass or FalseClass", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "bool*");
    assertInstanceOf(type, RCollectionType.class);
    assertEquals("TrueClass or FalseClass", ((RCollectionType)type).getIndexType().getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "BOOL*");
    assertInstanceOf(type, RCollectionType.class);
    assertEquals("TrueClass or FalseClass", ((RCollectionType)type).getIndexType().getPresentableName());
  }

  public void testVoid() {
    defaultConfigure();
    RType type = MotionSymbolUtil.getTypeByName(getModule(), "void");
    assertEquals(REmptyType.INSTANCE, type);
    type = MotionSymbolUtil.getTypeByName(getModule(), "void*");
    assertInstanceOf(type, RCollectionType.class);
    assertEquals(REmptyType.INSTANCE, ((RCollectionType)type).getIndexType());
  }

  public void testFloat() {
    defaultConfigure();
    RType type = MotionSymbolUtil.getTypeByName(getModule(), "float");
    assertEquals("Float", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "double");
    assertEquals("Float", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "CGFloat");
    assertEquals("Float", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "Float32");
    assertEquals("Float", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "Float64");
    assertEquals("Float", type.getPresentableName());
  }

  public void testInt() {
    defaultConfigure();
    RType type = MotionSymbolUtil.getTypeByName(getModule(), "int");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "char");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "short");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "long");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "long long");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "unsigned int");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "unsigned char");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "unsigned short");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "unsigned long");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "unsigned long long");
    assertEquals("Integer", type.getPresentableName());

    type = MotionSymbolUtil.getTypeByName(getModule(), "Byte");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "SignedByte");
    assertEquals("Integer", type.getPresentableName());

    type = MotionSymbolUtil.getTypeByName(getModule(), "Int16");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "SInt16");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "UInt16");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "Int64");
    assertEquals("Integer", type.getPresentableName());

    type = MotionSymbolUtil.getTypeByName(getModule(), "NSInteger");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "NSUInteger");
    assertEquals("Integer", type.getPresentableName());

    type = MotionSymbolUtil.getTypeByName(getModule(), "int32_t");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "uint64_t");
    assertEquals("Integer", type.getPresentableName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "size_t");
    assertEquals("Integer", type.getPresentableName());
  }

  public void testNSObject() {
    defaultConfigure();
    RType type = MotionSymbolUtil.getTypeByName(getModule(), "NSObject*");
    assertInstanceOf(type, RSymbolType.class);
    assertEquals("NSObject", ((RSymbolType)type).getSymbol().getName());
    type = MotionSymbolUtil.getTypeByName(getModule(), "NSObject**");
    assertInstanceOf(type, RCollectionType.class);
    final RType itemType = ((RCollectionType)type).getIndexType();
    assertInstanceOf(itemType, RSymbolType.class);
    assertEquals("NSObject", ((RSymbolType)itemType).getSymbol().getName());
  }


  public void testSuperTypes() {
    defaultConfigure();
    assertHasMembers(CoreTypes.String, "lowercaseString", "appendFormat");
    assertHasMembers(CoreTypes.Array, "sortedArrayHint", "removeObjectIdenticalTo");
    assertHasMembers(CoreTypes.Numeric, "initWithLong");
    assertHasMembers(CoreTypes.Hash, "descriptionInStringsFileFormat", "addEntriesFromDictionary");
    assertHasMembers(CoreTypes.Time, "dateByAddingTimeInterval");
  }

  private void assertHasMembers(final String className, final String... members) {
    final RType type = RTypeFactory.createTypeByFQN(getProject(), className, Context.INSTANCE_PRIVATE);
    for (String member : members) {
      assertNotNull("Does not have member " + member, type.getMemberForName(member, SymbolFilterFactory.EMPTY_FILTER, myFixture.getFile()));
    }
  }
}
