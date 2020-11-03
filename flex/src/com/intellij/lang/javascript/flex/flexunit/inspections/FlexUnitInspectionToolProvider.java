// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.flexunit.inspections;

public final class FlexUnitInspectionToolProvider {
  public static Class[] getInspectionClasses() {
    return new Class[]{
      FlexUnitClassInProductSourceInspection.class,
      FlexUnitClassVisibilityInspection.class,
      FlexUnitMethodVisibilityInspection.class,
      FlexUnitMethodIsStaticInspection.class,
      FlexUnitMethodIsPropertyInspection.class,
      FlexUnitMethodHasParametersInspection.class,
      FlexUnitMethodReturnTypeInspection.class,
      FlexUnitMixedAPIInspection.class,
      FlexUnitClassWithNoTestsInspection.class,
      FlexUnitMethodInSuiteInspection.class,
      FlexUnitEmptySuiteInspection.class,
      FlexUnitSuiteWithNoRunnerInspection.class};
  }
}
