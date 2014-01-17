package org.angularjs.editor;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSTargetedInjector;
import org.angularjs.index.AngularDirectivesIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angularjs.codeInsight.attributes.AngularAttributeDescriptor;
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
    if (AngularIndexUtil.resolve(project, AngularDirectivesIndex.INDEX_ID, "ng-model") == null) return;

    if (context instanceof XmlAttributeValueImpl) {
      final XmlAttribute attribute = (XmlAttribute)context.getParent();
      if (isAngularAttribute(attribute, "ng-init")) {
        registrar.startInjecting(AngularJSLanguage.INSTANCE).
          addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(1, context.getTextLength() - 1)).
          doneInjecting();
        return;
      }
    }

    if (context instanceof XmlTextImpl || context instanceof XmlAttributeValueImpl) {
      final String text = context.getText();
      int startIndex;
      int endIndex = -1;
      do {
        startIndex = text.indexOf("{{", endIndex);
        endIndex = startIndex >= 0 ? text.indexOf("}}", startIndex) : -1;
        endIndex = endIndex > 0 ? endIndex : text.length();
        if (startIndex >= 0) {
          registrar.startInjecting(AngularJSLanguage.INSTANCE).
                    addPlace(null, null, (PsiLanguageInjectionHost)context, new TextRange(startIndex + 2, endIndex)).
                    doneInjecting();
        }
      } while (startIndex >= 0);
    }
  }

  private static boolean isAngularAttribute(XmlAttribute parent, final String name) {
    final XmlAttributeDescriptor descriptor = parent.getDescriptor();
    return descriptor instanceof AngularAttributeDescriptor && name.equals(descriptor.getName());
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlTextImpl.class, XmlAttributeValueImpl.class);
  }
}
