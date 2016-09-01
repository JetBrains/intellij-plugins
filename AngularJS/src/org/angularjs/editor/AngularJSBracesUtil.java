package org.angularjs.editor;

import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
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
  public static final Key<CachedValue<String>> START_KEY = Key.create("angularjs.startSymbol");
  public static final Key<CachedValue<String>> END_KEY = Key.create("angularjs.endSymbol");

  private static final Set<String> DEFAULT_CONFLICTS = new HashSet<>(Arrays.asList("DjangoTemplate", "Jinja2", "Web2Py",
                                                                                   "Handlebars", "Twig", "Blade"));

  public static String getInjectionStart(Project project) {
    return getInjectionDelimiter(project, START_KEY, DEFAULT_START);
  }

  public static String getInjectionEnd(Project project) {
    return getInjectionDelimiter(project, END_KEY, DEFAULT_END);
  }

  private static String getInjectionDelimiter(Project project, final Key<CachedValue<String>> key, final String defaultDelimiter) {
    return CachedValuesManager.getManager(project).getCachedValue(project, key, () -> {
      String id = key.toString();
      final JSImplicitElement delimiter = AngularIndexUtil.resolve(project, AngularInjectionDelimiterIndex.KEY,
                                                                   id.substring(id.lastIndexOf(".") + 1));
      if (delimiter != null) {
        return CachedValueProvider.Result.create(delimiter.getTypeString(), delimiter);
      }
      return CachedValueProvider.Result.create(defaultDelimiter, PsiModificationTracker.MODIFICATION_COUNT);
    }, false);
  }

  public static boolean hasConflicts(String start, String end, PsiElement element) {
    final Language elementLanguage = element.getLanguage();
    // JSP contains two roots that contain XmlText, don't inject anything in JSP root to prevent double injections
    if ("JSP".equals(elementLanguage.getDisplayName())) {
      return true;
    }

    PsiFile file = element.getContainingFile();
    if (DEFAULT_START.equals(start) || DEFAULT_END.equals(end)) {
      // JSX attributes don't contain AngularJS injections, {{}} is JSX injection with object inside
      if (elementLanguage.isKindOf(JavascriptLanguage.INSTANCE)) return true;

      for (Language language : file.getViewProvider().getLanguages()) {
        if (DEFAULT_CONFLICTS.contains(language.getDisplayName())) {
          return true;
        }
      }
    }
    return false;
  }
}
