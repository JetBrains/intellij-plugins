// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.generate;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.generation.CreateGetterSetterFix;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

public class DartGenerateActionTest extends DartGenerateActionTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH + FileUtil.toSystemDependentName("/generate/");
  }

  public void testConstructor1() {
    doConstructorTest();
  }

  public void testConstructor2() {
    doConstructorTest();
  }

  public void testConstructor3() {
    doConstructorTest();
  }

  public void testNamedConstructor1() {
    doNamedConstructorTest();
  }

  public void testNamedConstructor2() {
    doNamedConstructorTest();
  }

  public void testNamedConstructor3() {
    doNamedConstructorTest();
  }

  public void testEqualsAndHashCode1() {
    doEqualsAndHashcodeTest();
  }

  public void testEqualsAndHashCode2() {
    doEqualsAndHashcodeTest();
  }

  public void testEqualsAndHashCode20() {
    doEqualsAndHashcodeTest();
  }

  public void testEqualsAndHashCode21() {
    doEqualsAndHashcodeTest();
  }

  public void testImplement1() {
    doImplementTest();
  }

  public void testImplement2() {
    doImplementTest();
  }

  public void testImplement3() {
    doImplementTest();
  }

  public void testImplement4() {
    doImplementTest();
  }

  public void testImplement5() {
    doImplementTest();
  }

  public void testImplement6() {
    doImplementTest();
  }

  public void testImplement7() {
    doImplementTest();
  }

  public void testImplement_WEB_2479() {
    doImplementTest();
  }

  public void testImplement_WEB_2479_2() {
    doImplementTest();
  }

  public void testImplement_WEB_16793() {
    doImplementTest();
  }

  public void testImplement_WEB_16793_2() {
    doImplementTest();
  }

  public void testImplement_WEB_14400() {
    doImplementTest();
  }

  public void testImplementMixin1() {
    doImplementTest();
  }

  public void testOverride1() {
    doOverrideTest();
  }

  public void testOverride2() {
    doOverrideTest();
  }

  public void testOverride3() {
    doOverrideTest();
  }

  public void testOverride4() {
    doOverrideTest();
  }

  public void testOverride5() {
    doOverrideTest();
  }

  public void testOverrideMixin1() {
    doOverrideTest();
  }

  public void testGetter1() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER);
  }

  public void testGetter2() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER);
  }

  public void testSetter1() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.SETTER);
  }

  public void testGetterSetter1() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER_SETTER);
  }

  public void testGetterSetter2() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER_SETTER);
  }

  public void testGetterSetter3() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER_SETTER);
  }

  public void testGetterSetter4() {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER_SETTER);
  }

  public void testOverrideOperator() {
    doOverrideTest();
  }

  public void testToString1() {
    doToStringTest();
  }

  public void testToString2() {
    doToStringTest();
  }

  public void testToString_WEB_16813() {
    doToStringTest();
  }
}
