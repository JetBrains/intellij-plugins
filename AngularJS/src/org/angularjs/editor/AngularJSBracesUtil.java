package org.angularjs.editor;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularInjectionDelimiterIndex;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSBracesUtil {
  public static final String DEFAULT_START = "{{";
  public static final String DEFAULT_END = "}}";

  private static final Set<String> DEFAULT_CONFLICTS = new HashSet<String>(Arrays.asList("DjangoTemplate", "Jinja2", "Web2Py",
                                                                                         "Handlebars", "Twig"));

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

  public static boolean hasConflicts(String start, String end, PsiElement element) {
    // JSP contains two roots that contain XmlText, don't inject anything in JSP root to prevent double injections
    if ("JSP".equals(element.getLanguage().getDisplayName())) {
      return true;
    }
    PsiFile file = element.getContainingFile();
    if (DEFAULT_START.equals(start) || DEFAULT_END.equals(end)) {
      for (Language language : file.getViewProvider().getLanguages()) {
        if (DEFAULT_CONFLICTS.contains(language.getDisplayName())) {
          return true;
        }
      }
    }
    return false;
  }
}
