package com.intellij.struts2.tiles;

import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.jsp.jspXml.JspXmlFile;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts.dom.tiles.Add;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts.dom.tiles.Put;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.DomPatterns.domElement;
import static com.intellij.patterns.DomPatterns.withDom;
import static com.intellij.patterns.StandardPatterns.or;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.patterns.XmlPatterns.xmlAttributeValue;

/**
 * {@code OGNL:...} expressions in {@code tiles.xml}.
 * <p/>
 * TODO this really belongs to StrutsAssistant/Tiles, but would introduce cyclic plugin dependency
 *
 * @author Yann C&eacute;bron
 */
public class TilesOgnlInjector implements MultiHostInjector {

  private static final String OGNL_PREFIX = "OGNL:";

  private static final ElementPattern<XmlAttributeValue> EXPRESSION_PATTERN =
    xmlAttributeValue()
      .withLocalName("expression")
      .withValue(string().startsWith(OGNL_PREFIX))
      .withSuperParent(2, withDom(or(domElement(Put.class), domElement(Add.class))));

  private static final ElementPattern<XmlAttributeValue> TEMPLATE_EXPRESSION_PATTERN =
    xmlAttributeValue()
      .withLocalName("templateExpression")
      .withValue(string().startsWith(OGNL_PREFIX))
      .withSuperParent(2, withDom(domElement(Definition.class)));

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final PsiFile containingFile = context.getContainingFile();
    if (!(containingFile instanceof XmlFile) ||
        containingFile instanceof JspFile ||
        containingFile instanceof JspXmlFile) {
      return;
    }

    if (EXPRESSION_PATTERN.accepts(context) ||
        TEMPLATE_EXPRESSION_PATTERN.accepts(context)) {
      final TextRange attributeTextRange = ElementManipulators.getValueTextRange(context);
      final TextRange ognlTextRange = TextRange.from(attributeTextRange.getStartOffset() + OGNL_PREFIX.length(),
                                                     attributeTextRange.getLength() - OGNL_PREFIX.length());
      registrar.startInjecting(OgnlLanguage.INSTANCE)
        .addPlace(OgnlLanguage.EXPRESSION_PREFIX, OgnlLanguage.EXPRESSION_SUFFIX,
                  (PsiLanguageInjectionHost)context, ognlTextRange)
        .doneInjecting();
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }
}
