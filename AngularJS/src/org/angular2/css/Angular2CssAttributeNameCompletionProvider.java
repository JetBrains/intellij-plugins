// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssElementDescriptorProvider;
import com.intellij.psi.css.impl.util.table.CssDescriptorsUtil;
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.impl.source.xml.XmlAttributeReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeInsertHandler;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class Angular2CssAttributeNameCompletionProvider extends CompletionProvider<CompletionParameters> {


  @Override
  protected void addCompletions(@NotNull CompletionParameters parameters,
                                @NotNull ProcessingContext context,
                                @NotNull CompletionResultSet result) {
    if (!(parameters.getPosition().getParent() instanceof XmlAttribute)
        || !Angular2LangUtil.isAngular2Context(parameters.getPosition())) {
      return;
    }
    PsiReference reference = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
    List<PsiReference> references;
    if (reference instanceof PsiMultiReference) {
      references = asList(((PsiMultiReference)reference).getReferences());
    }
    else {
      references = singletonList(reference);
    }
    reference = ContainerUtil.find(references, HtmlCssClassOrIdReference.class::isInstance);
    if (reference == null) {
      reference = ContainerUtil.find(references, XmlAttributeReference.class::isInstance);
      if (reference == null) {
        return;
      }
      final XmlAttribute attribute = ((XmlAttributeReference)reference).getElement();
      String attributeName = attribute.getName();
      if (!attributeName.startsWith(Angular2CssBoundAttributeProvider.SHORT_PREFIX)) {
        return;
      }
      CssElementDescriptorProvider descriptorProvider = CssDescriptorsUtil.findDescriptorProvider(
        parameters.getPosition());
      assert descriptorProvider != null;
      reference = ObjectUtils.tryCast(
        descriptorProvider.getStyleReference(
          parameters.getPosition(), 6, attributeName.length() - 6, true),
        HtmlCssClassOrIdReference.class);
      result = result.withPrefixMatcher(
        result.getPrefixMatcher().cloneWithPrefix(
          result.getPrefixMatcher().getPrefix().substring(7)));
    }
    if (reference instanceof HtmlCssClassOrIdReference) {
      processHtmlCssReference(parameters, result, (HtmlCssClassOrIdReference)reference);
    }
  }

  private static void processHtmlCssReference(final @NotNull CompletionParameters parameters,
                                              final @NotNull CompletionResultSet result,
                                              @NotNull HtmlCssClassOrIdReference reference) {
    reference.addCompletions(parameters, result.getPrefixMatcher(), element -> {
      if (element instanceof PrioritizedLookupElement) {
        LookupElementBuilder builder = ObjectUtils.tryCast(((PrioritizedLookupElement)element).getDelegate(),
                                                           LookupElementBuilder.class);
        if (builder != null) {
          Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(
            parameters.getPosition().getText());
          element = PrioritizedLookupElement.withPriority(
            builder.withInsertHandler(new Angular2AttributeInsertHandler(true, () -> true, info.isCanonical ? "" : "]")),
            ((PrioritizedLookupElement)element).getPriority());
        }
      }
      result.addElement(element);
    });
    result.stopHere();
  }
}
