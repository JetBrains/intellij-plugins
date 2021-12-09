package org.angularjs.editor;

import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.javascript.JSInjectionBracesUtil;
import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.xml.XmlAttributeValueImpl;
import com.intellij.psi.impl.source.xml.XmlTextImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.codeInsight.DirectiveUtil;
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
  public static final NullableFunction<PsiElement, Pair<String, String>> BRACES_FACTORY = JSInjectionBracesUtil
    .delimitersFactory(AngularJSLanguage.INSTANCE.getDisplayName(),
                       (project, key) -> {
                         final JSImplicitElement element = AngularIndexUtil.resolve(project, AngularInjectionDelimiterIndex.KEY, key);
                         return element != null ? Pair.create(element.getTypeString(), element) : null;
                       });

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final Language language = context.getLanguage();
    if (language == XMLLanguage.INSTANCE || language.isKindOf(JavascriptLanguage.INSTANCE)) return;
    // check that we have angular directives indexed before injecting
    final Project project = context.getProject();

    final PsiElement parent = context.getParent();
    if (context instanceof XmlAttributeValueImpl && parent instanceof XmlAttribute &&
        ((XmlAttributeValueImpl)context).isValidHost()) {
      if (!AngularIndexUtil.hasAngularJS(project)) return;
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

    if (context instanceof XmlTextImpl && !nonBindable((XmlTextImpl)context) || context instanceof XmlAttributeValueImpl) {
      if (!AngularIndexUtil.hasAngularJS(project)) return;
      final Pair<String, String> braces = BRACES_FACTORY.fun(context);
      if (braces == null) return;

      JSInjectionBracesUtil
        .injectInXmlTextByDelimiters(registrar, context, AngularJSLanguage.INSTANCE, braces.getFirst(), braces.getSecond());
    }
  }

  private static boolean nonBindable(final @NotNull XmlTextImpl xmlText) {
    final XmlTag parentTag = xmlText.getParentTag();
    return parentTag != null && ContainerUtil.find(parentTag.getAttributes(),
                                                   attr -> "ngNonBindable".equals(DirectiveUtil.normalizeAttributeName(attr.getName()))) !=
                                null;
  }

  @Override
  public @NotNull List<Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlTextImpl.class, XmlAttributeValueImpl.class);
  }
}
