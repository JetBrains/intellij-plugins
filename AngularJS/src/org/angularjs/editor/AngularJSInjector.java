package org.angularjs.editor;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.InjectorMatchingEndFinder;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.NullableFunction;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularInjectionDelimiterIndex;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSInjector implements MultiHostInjector {
  private static final Logger LOG = Logger.getInstance(AngularJSInjector.class);
  public static final NullableFunction<PsiElement, Pair<String, String>> BRACES_FACTORY = JSInjectionBracesUtil
    .createDelimitersFactory(AngularJSLanguage.INSTANCE.getDisplayName(),
                             (project, key) -> AngularIndexUtil.resolve(project, AngularInjectionDelimiterIndex.KEY, key));

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    if (context.getLanguage() == XMLLanguage.INSTANCE) return;
    // check that we have angular directives indexed before injecting
    final Project project = context.getProject();
    if (!AngularIndexUtil.hasAngularJS(project)) return;

    final PsiElement parent = context.getParent();
    if (context instanceof XmlAttributeValueImpl && parent instanceof XmlAttribute) {
      final String value = context.getText();
      final int start = value.startsWith("'") || value.startsWith("\"") ? 1 : 0;
      final int end = value.endsWith("'") || value.endsWith("\"") ? 1 : 0;
      final int length = value.length();
      if (AngularAttributesRegistry.isAngularExpressionAttribute((XmlAttribute)parent) && length > 1) {
        registrar.startInjecting(AngularJSLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(start, length - end)).
          doneInjecting();
        return;
      }
      if (AngularAttributesRegistry.isJSONAttribute((XmlAttribute)parent) && length > 1) {
        registrar.startInjecting(JsonLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(start, length - end)).
          doneInjecting();
        return;
      }
    }

    if (context instanceof XmlTextImpl || context instanceof XmlAttributeValueImpl) {
      final Pair<String, String> braces = BRACES_FACTORY.fun(context);
      if (braces == null) return;
      final String start = braces.getFirst();
      final String end = braces.getSecond();

      final String text = context.getText();
      int endIndex = -1;
      int afterStart = -1;
      int searchStart = -2;
      while (Math.max(endIndex, afterStart) > searchStart) {
        searchStart = Math.max(endIndex, afterStart);
        final int startIdx = text.indexOf(start, searchStart);
        if (startIdx < 0) return;
        afterStart = startIdx + start.length();
        assert afterStart >= 0;

        endIndex = afterStart;
        endIndex = InjectorMatchingEndFinder.findMatchingEnd(start, end, text, endIndex);
        endIndex = endIndex > 0 ? endIndex : ElementManipulators.getValueTextRange(context).getEndOffset();
        final PsiElement injectionCandidate = context.findElementAt(afterStart);
        if (injectionCandidate != null && injectionCandidate.getNode().getElementType() != XmlTokenType.XML_COMMENT_CHARACTERS &&
           !(injectionCandidate instanceof OuterLanguageElement)) {
          if (afterStart > endIndex) {
            if (ApplicationManager.getApplication().isInternal()) {
              LOG.error("Braces: " + start + "," + end + "\n" +
                        "Text: \"" + text + "\"" + "\n" +
                        "Interval: (" + afterStart + "," + endIndex + ")" + "\n" +
                        "File: " + context.getContainingFile().getName() + ", language:" + context.getContainingFile().getLanguage());
            }
            return;
          }
          registrar.startInjecting(AngularJSLanguage.INSTANCE).
                    addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(afterStart, endIndex)).
                    doneInjecting();
        }
      }
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlTextImpl.class, XmlAttributeValueImpl.class);
  }
}
