package org.angularjs.editor;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTargetedInjector;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.templateLanguages.OuterLanguageElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.lang.AngularJSLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSInjector implements MultiHostInjector, JSTargetedInjector {
  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
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
      final String start = AngularJSBracesUtil.getInjectionStart(project);
      final String end = AngularJSBracesUtil.getInjectionEnd(project);

      if (AngularJSBracesUtil.hasConflicts(start, end, context)) return;

      final String text = context.getText();
      int startIndex;
      int endIndex = -1;
      do {
        startIndex = text.indexOf(start, endIndex);
        int afterStart = startIndex + start.length();
        endIndex = startIndex >= 0 ? text.indexOf(end, afterStart) : -1;
        endIndex = endIndex > 0 ? endIndex : text.length() - 1;
        final PsiElement injectionCandidate = startIndex >= 0 ? context.findElementAt(startIndex) : null;
        if (injectionCandidate != null && injectionCandidate.getNode().getElementType() != XmlTokenType.XML_COMMENT_CHARACTERS &&
           !(injectionCandidate instanceof OuterLanguageElement)) {
          registrar.startInjecting(AngularJSLanguage.INSTANCE).
                    addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(afterStart, endIndex)).
                    doneInjecting();
        }
      } while (startIndex >= 0);
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlTextImpl.class, XmlAttributeValueImpl.class);
  }
}
