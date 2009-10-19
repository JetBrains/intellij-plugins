package com.intellij.tapestry.intellij.inspections;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author Alexey Chmutov
 *         Date: 19.10.2009
 *         Time: 13:41:14
 */

/**
 * @author Alexey Chmutov
 */
public class TapestryInspectionToolProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[]{TelReferencesInspection.class,};
  }
}
