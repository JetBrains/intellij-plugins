package com.google.jstestdriver.idea.assertFramework.support;

import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineAdapterSupportProvider;
import com.google.jstestdriver.idea.assertFramework.jstd.JstdDefaultAssertionFrameworkSupportInspection;
import com.google.jstestdriver.idea.assertFramework.qunit.QUnitAdapterSupportProvider;
import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author Sergey Simonchik
 */
public class JstdInspectionToolProvider implements InspectionToolProvider {
  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{
      JstdDefaultAssertionFrameworkSupportInspection.class,
      QUnitAdapterSupportProvider.class,
      JasmineAdapterSupportProvider.class
    };
  }
}
