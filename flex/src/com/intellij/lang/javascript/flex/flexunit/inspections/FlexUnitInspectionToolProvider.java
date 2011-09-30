package com.intellij.lang.javascript.flex.flexunit.inspections;

public class FlexUnitInspectionToolProvider {
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
