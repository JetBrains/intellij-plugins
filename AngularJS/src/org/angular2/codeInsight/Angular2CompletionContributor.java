// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.resolve.HtmlCssClassOrIdReference;
import com.intellij.psi.impl.source.xml.XmlAttributeReference;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlExtension;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeNameVariantsBuilder;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.intellij.codeInsight.completion.XmlAttributeReferenceCompletionProvider.isValidVariant;
import static com.intellij.patterns.PlatformPatterns.psiElement;

public class Angular2CompletionContributor extends CompletionContributor {
  private static final JSLookupPriority NG_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY;
  private static final JSLookupPriority NG_PRIVATE_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC;

  private static final Set<String> NG_LIFECYCLE_HOOKS = ContainerUtil.newHashSet(
    "ngOnChanges", "ngOnInit", "ngDoCheck", "ngOnDestroy", "ngAfterContentInit",
    "ngAfterContentChecked", "ngAfterViewInit", "ngAfterViewChecked");

  public Angular2CompletionContributor() {
    extend(CompletionType.BASIC, psiElement().with(language(Angular2Language.INSTANCE)), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        addTemplateExpressionCompletions(parameters, result);
      }
    });
    extend(CompletionType.BASIC, psiElement().inside(XmlPatterns.xmlAttribute()), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull CompletionParameters parameters,
                                    @NotNull ProcessingContext context,
                                    @NotNull CompletionResultSet result) {
        if (Angular2LangUtil.isAngular2Context(parameters.getOriginalFile())) {
          addAttributeNameCompletions(parameters, result);
        }
      }
    });
  }

  private static void addTemplateExpressionCompletions(@NotNull final CompletionParameters parameters,
                                                       @NotNull final CompletionResultSet result) {
    PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
    if (ref instanceof Angular2PipeReferenceExpression) {
      for (String controller : Angular2EntitiesProvider.getAllPipeNames(((Angular2PipeReferenceExpression)ref).getProject())) {
        result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(null, controller, NG_VARIABLE_PRIORITY, false, false));
      }
      result.stopHere();
    }
    else if (ref instanceof JSReferenceExpressionImpl && ((JSReferenceExpressionImpl)ref).getQualifier() == null) {
      final Set<String> contributedElements = new HashSet<>();
      Angular2Processor.process(parameters.getPosition(), element -> {
        final String name = element.getName();
        if (name != null && !NG_LIFECYCLE_HOOKS.contains(name)
            && contributedElements.add(name + "#" + JSLookupUtilImpl.getTypeAndTailTexts(element, null))) {
          result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(
            element, name, Angular2DecoratorUtil.isPrivateMember(element) ? NG_PRIVATE_VARIABLE_PRIORITY : NG_VARIABLE_PRIORITY, false,
            false));
        }
      });
      result.stopHere();
    }
    else if (ref instanceof HtmlCssClassOrIdReference) {
      ((HtmlCssClassOrIdReference)ref).addCompletions(parameters, result);
      result.stopHere();
    }
  }

  private static void addAttributeNameCompletions(@NotNull final CompletionParameters parameters,
                                                  @NotNull final CompletionResultSet result) {
    PsiReference reference = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
    if (reference instanceof XmlAttributeReference) {
      final XmlAttribute attribute = ((XmlAttributeReference)reference).getElement();
      final XmlTag tag = attribute.getParent();
      final XmlElementDescriptor parentDescriptor = tag.getDescriptor();
      if (parentDescriptor != null) {
        final XmlAttribute[] attributes = tag.getAttributes();
        XmlAttributeDescriptor[] descriptors = parentDescriptor.getAttributesDescriptors(tag);

        final PsiFile file = tag.getContainingFile();
        final XmlExtension extension = XmlExtension.getExtension(file);

        Set<String> providedAttributes = StreamEx.of(attributes)
          .map(attr -> attr.getDescriptor())
          .select(Angular2AttributeDescriptor.class)
          .flatCollection(Angular2CompletionContributor::getRelatedAttributes)
          .toSet();
        for (XmlAttributeDescriptor descriptor : descriptors) {
          if (descriptor instanceof Angular2AttributeDescriptor) {
            if (!providedAttributes.contains(descriptor.getName())
                && isValidVariant(attribute, descriptor, attributes, extension)) {
              LookupElement element = ((Angular2AttributeDescriptor)descriptor).getLookupElement();
              providedAttributes.add(element.getLookupString());
              result.addElement(element);
            }
          }
        }
        Set<String> standardHtmlEvents = new HashSet<>(Angular2AttributeDescriptorsProvider.getStandardTagEventAttributeNames(tag));
        result.runRemainingContributors(parameters, toPass -> {
          for (String str : toPass.getLookupElement().getAllLookupStrings()) {
            if (standardHtmlEvents.contains(str)
                || providedAttributes.contains(str)) {
              return;
            }
          }
          result.passResult(toPass);
        });
      }
    }
  }

  @NotNull
  private static Collection<String> getRelatedAttributes(@NotNull Angular2AttributeDescriptor descriptor) {
    Angular2AttributeNameParser.AttributeInfo info = descriptor.getInfo();
    if (info.type == Angular2AttributeType.BANANA_BOX_BINDING
        || info.type == Angular2AttributeType.REGULAR
        || (info instanceof PropertyBindingInfo
            && ((PropertyBindingInfo)info).bindingType == PropertyBindingType.PROPERTY)
    ) {
      return Angular2AttributeNameVariantsBuilder.forTypes(
        info.name, true, true,
        Angular2AttributeType.REGULAR,
        Angular2AttributeType.PROPERTY_BINDING,
        Angular2AttributeType.BANANA_BOX_BINDING);
    }
    return Angular2AttributeNameVariantsBuilder.forTypes(
      info.getFullName(), true, true,
      info.type);
  }

  private static <T extends PsiElement> PatternCondition<T> language(@NotNull Language language) {
    return new PatternCondition<T>("language(" + language.getID() + ")") {
      @Override
      public boolean accepts(@NotNull T t, ProcessingContext context) {
        return language.is(PsiUtilCore.findLanguageFromElement(t));
      }
    };
  }
}
