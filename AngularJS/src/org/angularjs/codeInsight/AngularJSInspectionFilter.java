package org.angularjs.codeInsight;

import com.intellij.codeInspection.InspectionProfileEntry;
import com.intellij.lang.javascript.highlighting.IntentionAndInspectionFilter;
import com.intellij.lang.javascript.inspections.JSBitwiseOperatorUsageInspection;
import com.sixrr.inspectjs.confusing.CommaExpressionJSInspection;
import com.sixrr.inspectjs.validity.BadExpressionStatementJSInspection;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSInspectionFilter extends IntentionAndInspectionFilter {
  @Override
  public boolean isSupportedInspection(String inspectionToolId) {
    return !inspectionToolId.equals(InspectionProfileEntry.getShortName(BadExpressionStatementJSInspection.class.getSimpleName())) &&
           !inspectionToolId.equals(InspectionProfileEntry.getShortName(CommaExpressionJSInspection.class.getSimpleName())) &&
           !inspectionToolId.equals(InspectionProfileEntry.getShortName(JSBitwiseOperatorUsageInspection.class.getSimpleName()));
  }
}
