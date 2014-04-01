package org.angularjs.editor;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTargetedInjector;
import com.intellij.psi.xml.XmlTokenType;
import org.angularjs.codeInsight.attributes.AngularAttributesRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.xml.XmlAttribute;
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
    // inject only in non-template languages, because otherwise we can get conflict with other templating mechanisms (e.g. Handlebars)
    if (context.getContainingFile().getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider) return;

    // check that we have angular directives indexed before injecting
    final Project project = context.getProject();
    if (!AngularIndexUtil.hasAngularJS(project)) return;

    final PsiElement parent = context.getParent();
    if (context instanceof XmlAttributeValueImpl && parent instanceof XmlAttribute) {
      final int length = context.getTextLength();
      if (AngularAttributesRegistry.isAngularExpressionAttribute((XmlAttribute)parent) && length > 1) {
        registrar.startInjecting(AngularJSLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(1, length - 1)).
          doneInjecting();
        return;
      }
    }

    if (context instanceof XmlTextImpl || context instanceof XmlAttributeValueImpl) {
      final String text = context.getText();
      int startIndex;
      int endIndex = -1;
      do {
        startIndex = text.indexOf(AngularJSBracesUtil.getInjectionStart(project), endIndex);
        endIndex = startIndex >= 0 ? text.indexOf(AngularJSBracesUtil.getInjectionEnd(project), startIndex) : -1;
        endIndex = endIndex > 0 ? endIndex : text.length();
        final PsiElement injectionCandidate = startIndex >= 0 ? context.findElementAt(startIndex) : null;
        if (injectionCandidate != null && injectionCandidate.getNode().getElementType() != XmlTokenType.XML_COMMENT_CHARACTERS) {
          registrar.startInjecting(AngularJSLanguage.INSTANCE).
                    addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(startIndex + 2, endIndex)).
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
