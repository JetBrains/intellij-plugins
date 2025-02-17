package com.jetbrains.plugins.jade.js;

import com.intellij.lang.javascript.highlighting.IntentionAndInspectionFilter;
import com.intellij.lang.javascript.inspections.JSUnnecessarySemicolonInspection;
import com.intellij.util.containers.ContainerUtil;
import com.sixrr.inspectjs.confusing.EmptyStatementBodyJSInspection;
import com.sixrr.inspectjs.validity.StringLiteralBreaksHTMLJSInspection;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;

public final class JavaScriptInJadeInspectionFilter extends IntentionAndInspectionFilter {
  private static final Set<String> ourRestrictedInspectionNames = ContainerUtil.map2Set(List.of(
    StringLiteralBreaksHTMLJSInspection.class,
    JSUnnecessarySemicolonInspection.class,
    EmptyStatementBodyJSInspection.class
  ), aClass -> {
    try {
      return aClass.getConstructor().newInstance().getID();
    }
    catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
      return aClass.getSimpleName();
    }
  });

  @Override
  public boolean isSupportedInspection(String inspectionToolId) {
    return !ourRestrictedInspectionNames.contains(inspectionToolId);
  }
}
