package org.angularjs.editor;

import com.intellij.openapi.project.Project;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSBracesUtil {
  public static String getInjectionStart(Project project) {
    return "{{";
  }
  public static String getInjectionEnd(Project project) {
    return "}}";
  }
}
