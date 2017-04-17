package org.angularjs.codeInsight;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.Processor;
import org.angularjs.index.AngularDirectivesDocIndex;
import org.angularjs.index.AngularDirectivesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.openapi.util.text.StringUtil.trimEnd;
import static com.intellij.openapi.util.text.StringUtil.trimStart;

/**
 * @author Dennis.Ushakov
 */
public class DirectiveUtil {
  public static String getAttributeName(final String text) {
    final String[] split = StringUtil.unquoteString(text).split("(?=[A-Z])");
    for (int i = 0; i < split.length; i++) {
      split[i] = StringUtil.decapitalize(split[i]);
    }
    return StringUtil.join(split, "-");
  }

  public static String normalizeAttributeName(String name) {
    if (name == null) return null;
    if (name.startsWith("data-")) {
      name = name.substring(5);
    }
    else name = trimStart(name, "x-");
    name = name.replace(':', '-');
    name = name.replace('_', '-');
    if (name.endsWith("-start")) {
      name = name.substring(0, name.length() - 6);
    }
    else name = trimEnd(name, "-end");
    return name;
  }

  public static boolean isAngular2Directive(final PsiElement directive) {
    return directive instanceof JSImplicitElement && (directive.getParent() instanceof JSCallExpression ||
                                                      directive.getParent() instanceof ES6Decorator);
  }

  public static String attributeToDirective(final PsiElement directive, final String name) {
    if (isAngular2Directive(directive)) {
      return name;
    }
    final String[] words = name.split("-");
    for (int i = 1; i < words.length; i++) {
      words[i] = StringUtil.capitalize(words[i]);
    }
    return StringUtil.join(words);
  }

  public static boolean processTagDirectives(final Project project,
                                             Processor<JSImplicitElement> processor) {
    final Collection<String> docDirectives = AngularIndexUtil.getAllKeys(AngularDirectivesDocIndex.KEY, project);
    for (String directiveName : docDirectives) {
      final JSImplicitElement directive = getTagDirective(project, directiveName, AngularDirectivesDocIndex.KEY);
      if (directive != null) {
        if (!processor.process(directive)) {
          return false;
        }
      }
    }
    final Collection<String> directives = AngularIndexUtil.getAllKeys(AngularDirectivesIndex.KEY, project);
    for (String directiveName : directives) {
      if (!docDirectives.contains(directiveName)) {
        final JSImplicitElement directive = getTagDirective(project, directiveName, AngularDirectivesIndex.KEY);
        if (directive != null) {
          if (!processor.process(directive)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static JSImplicitElement getTagDirective(String directiveName, Project project) {
    final JSImplicitElement directive = getTagDirective(project, directiveName, AngularDirectivesDocIndex.KEY);
    return directive == null ? getTagDirective(project, directiveName, AngularDirectivesIndex.KEY) : directive;
  }

  private static JSImplicitElement getTagDirective(Project project, String directiveName, final StubIndexKey<String, JSImplicitElementProvider> index) {
    final JSImplicitElement directive = AngularIndexUtil.resolve(project, index, directiveName);
    final String restrictions = directive != null ? directive.getTypeString() : null;
    if (restrictions != null) {
      final String[] split = restrictions.split(";", -1);
      final String restrict = AngularIndexUtil.convertRestrictions(project, split[0]);
      if (!StringUtil.isEmpty(restrict) && StringUtil.containsIgnoreCase(restrict, "E")) {
        return directive;
      }
    }
    return null;
  }

  @Nullable
  public static JSImplicitElement getDirective(@Nullable PsiElement element) {
    if (element instanceof JSImplicitElement) {
      return getDirective(element, ((JSImplicitElement)element).getName());
    }
    if (element instanceof JSLiteralExpression && ((JSLiteralExpression)element).isQuotedLiteral()) {
      return getDirective(element, StringUtil.unquoteString(element.getText()));
    }
    return null;
  }

  @Nullable
  private static JSImplicitElement getDirective(@NotNull PsiElement element, final String name) {
    final String directiveName = getAttributeName(name);
    final JSImplicitElement directive = AngularIndexUtil.resolve(element.getProject(), AngularDirectivesIndex.KEY, directiveName);
    if (directive != null && directive.isEquivalentTo(element)) {
      return directive;
    }
    return null;
  }
}
