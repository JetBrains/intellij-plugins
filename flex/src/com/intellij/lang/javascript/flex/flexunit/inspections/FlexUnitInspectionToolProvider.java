package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.InspectionToolProvider;

public class FlexUnitInspectionToolProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
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
