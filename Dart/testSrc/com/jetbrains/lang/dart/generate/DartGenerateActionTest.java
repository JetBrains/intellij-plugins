package com.jetbrains.lang.dart.generate;

import com.intellij.openapi.util.io.FileUtil;
import com.jetbrains.lang.dart.ide.generation.CreateGetterSetterFix;
import com.jetbrains.lang.dart.util.DartTestUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartGenerateActionTest extends DartGenerateActionTestBase {
  @NotNull
  @Override
  protected String getTestDataPath() {
    return DartTestUtils.BASE_TEST_DATA_PATH +  FileUtil.toSystemDependentName("/generate/");
  }

  public void testConstructor1() throws Throwable {
    doConstructor();
  }

  public void testConstructor2() throws Throwable {
    doConstructor();
  }

  public void testConstructor3() throws Throwable {
    doConstructor();
  }

  public void testImplement1() throws Throwable {
    doImplementTest();
  }

  public void testImplement2() throws Throwable {
    doImplementTest();
  }

  public void testImplement3() throws Throwable {
    doImplementTest();
  }

  public void testImplement4() throws Throwable {
    doImplementTest();
  }

  public void testImplement5() throws Throwable {
    doImplementTest();
  }

  public void testImplement_WEB_2479() throws Throwable {
    doImplementTest();
  }

  public void testImplement_WEB_2479_2() throws Throwable {
    doImplementTest();
  }

  public void testImplementMixin1() throws Throwable {
    doImplementTest();
  }

  public void testOverride1() throws Throwable {
    doOverrideTest();
  }

  public void testOverride2() throws Throwable {
    doOverrideTest();
  }

  public void testOverride3() throws Throwable {
    doOverrideTest();
  }

  public void testOverride4() throws Throwable {
    doOverrideTest();
  }

  public void testOverrideMixin1() throws Throwable {
    doOverrideTest();
  }

  public void testGetter1() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER);
  }

  public void testGetter2() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTER);
  }

  public void testSetter1() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.SETTER);
  }

  public void testGetterSetter1() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTERSETTER);
  }

  public void testGetterSetter2() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTERSETTER);
  }

  public void testGetterSetter3() throws Throwable {
    doGetterSetterTest(CreateGetterSetterFix.Strategy.GETTERSETTER);
  }
}
