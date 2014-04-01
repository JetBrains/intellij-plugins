package org.angularjs.editor;

import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularInjectionDelimiterIndex;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSBracesUtil {
  public static final String DEFAULT_START = "{{";
  public static final String DEFAULT_END = "}}";

  public static String getInjectionStart(Project project) {
    return getInjectionDelimiter(project, "startSymbol", DEFAULT_START);
  }

  public static String getInjectionEnd(Project project) {
    return getInjectionDelimiter(project, "endSymbol", DEFAULT_END);
  }

  private static String getInjectionDelimiter(Project project, final String id, final String defaultDelimiter) {
    final JSNamedElementProxy start = AngularIndexUtil.resolve(project, AngularInjectionDelimiterIndex.INDEX_ID, id);
    if (start != null) {
      return start.getIndexItem().getTypeString();
    }
    return defaultDelimiter;
  }
}
