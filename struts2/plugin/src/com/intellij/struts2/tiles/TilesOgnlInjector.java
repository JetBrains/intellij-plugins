package com.intellij.struts2.tiles;

import com.intellij.jam.model.util.JamCommonUtil;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.lang.ognl.OgnlLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts.dom.tiles.Add;
import com.intellij.struts.dom.tiles.Definition;
import com.intellij.struts.dom.tiles.Put;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * {@code OGNL:...} expressions in {@code tiles.xml}.
 * <p/>
 * TODO this really belongs to StrutsAssistant/Tiles, but would introduce cyclic plugin dependency
 *
 * @author Yann C&eacute;bron
 */
public class TilesOgnlInjector implements MultiHostInjector {
  private static final String OGNL_PREFIX = "OGNL:";

  @Override
  public void getLanguagesToInject(@NotNull MultiHostRegistrar registrar, @NotNull PsiElement context) {
    final PsiFile containingFile = context.getContainingFile();
   if (!JamCommonUtil.isPlainXmlFile(containingFile)) {
      return;
    }

    assert context instanceof XmlAttributeValue;
    if (!((XmlAttributeValue)context).getValue().startsWith(OGNL_PREFIX)) {
      return;
    }
    
    PsiElement parent = context.getParent();
    if (parent instanceof XmlAttribute) {
      String name = ((XmlAttribute)parent).getLocalName();
      if ("expression".equals(name) || "templateExpression".equals(name)) {
        DomElement domElement = DomManager.getDomManager(context.getProject()).getDomElement((XmlTag)parent.getParent());
        if (domElement instanceof Put || domElement instanceof Add || domElement instanceof Definition) {
          final TextRange attributeTextRange = ElementManipulators.getValueTextRange(context);
          final TextRange ognlTextRange = TextRange.from(attributeTextRange.getStartOffset() + OGNL_PREFIX.length(),
                                                         attributeTextRange.getLength() - OGNL_PREFIX.length());
          registrar.startInjecting(OgnlLanguage.INSTANCE)
            .addPlace(OgnlLanguage.EXPRESSION_PREFIX, OgnlLanguage.EXPRESSION_SUFFIX,
                      (PsiLanguageInjectionHost)context, ognlTextRange)
            .doneInjecting();
          
        }
      }
    }
  }

  @NotNull
  @Override
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }
}
