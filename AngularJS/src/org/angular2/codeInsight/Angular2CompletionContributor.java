// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupElementInsertHandler;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser.FunctionTypeWithKind;
import com.intellij.lang.javascript.ecmascript6.types.OverloadPriority;
import com.intellij.lang.javascript.psi.JSFunctionType;
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.impl.source.xml.XmlAttributeReference;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import com.intellij.xml.XmlExtension;
import icons.AngularJSIcons;
import one.util.streamex.StreamEx;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeInsertHandler;
import org.angular2.codeInsight.attributes.Angular2AttributesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributesProvider.CompletionResultsConsumer;
import org.angular2.css.Angular2CssAttributeNameCompletionProvider;
import org.angular2.css.Angular2CssExpressionCompletionProvider;
import org.angular2.entities.*;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.Angular2LangUtil;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angular2.lang.expr.psi.Angular2PipeLeftSideArgument;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.codeInsight.completion.XmlAttributeReferenceCompletionProvider.isValidVariant;
import static com.intellij.lang.javascript.psi.types.JSCompositeTypeBaseImpl.isNullOrUndefinedType;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.patterns.StandardPatterns.string;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.newHashSet;

public class Angular2CompletionContributor extends CompletionContributor {

  private static final JSLookupPriority NG_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY;
  private static final JSLookupPriority NG_PRIVATE_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC;
  private static final JSLookupPriority NG_$ANY_PRIORITY = JSLookupPriority.TOP_LEVEL_SYMBOLS_FROM_OTHER_FILES;

  private static final Set<String> NG_LIFECYCLE_HOOKS = newHashSet(
    "ngOnChanges", "ngOnInit", "ngDoCheck", "ngOnDestroy", "ngAfterContentInit",
    "ngAfterContentChecked", "ngAfterViewInit", "ngAfterViewChecked");

  public Angular2CompletionContributor() {

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           new Angular2CssExpressionCompletionProvider());

    extend(CompletionType.BASIC,
           psiElement().inside(XmlPatterns.xmlAttribute()),
           new Angular2CssAttributeNameCompletionProvider());

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           new TemplateExpressionCompletionProvider());

    extend(CompletionType.BASIC,
           psiElement().inside(XmlPatterns.xmlAttribute()),
           new AttributeNameCompletionProvider());
  }

  private static <T extends PsiElement> PatternCondition<T> language(@NotNull Language language) {
    return new PatternCondition<T>("language(" + language.getID() + ")") {
      @Override
      public boolean accepts(@NotNull T t, ProcessingContext context) {
        return language.is(PsiUtilCore.findLanguageFromElement(t));
      }
    };
  }

  private static class TemplateExpressionCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull final CompletionParameters parameters,
                                  @NotNull final ProcessingContext context,
                                  @NotNull final CompletionResultSet result) {
      PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      if (ref instanceof PsiMultiReference) {
        ref = ContainerUtil.find(((PsiMultiReference)ref).getReferences(), r ->
          r instanceof Angular2PipeReferenceExpression
          || r instanceof JSReferenceExpressionImpl);
      }
      if (ref instanceof Angular2PipeReferenceExpression) {
        Set<Angular2Pipe> scope = getModuleScope(parameters.getPosition());
        JSType actualType = calcActualType(((Angular2PipeReferenceExpression)ref));
        for (Map.Entry<String, List<Angular2Pipe>> pipeEntry : Angular2EntitiesProvider
          .getAllPipes(((Angular2PipeReferenceExpression)ref).getProject()).entrySet()) {
          List<Angular2Pipe> candidates = pipeEntry.getValue();
          if (candidates.isEmpty()) {
            return;
          }
          Angular2Pipe match = scope == null
                               ? candidates.get(0)
                               : ContainerUtil.find(pipeEntry.getValue(), scope::contains);
          LookupElementBuilder builder = LookupElementBuilder.create(pipeEntry.getKey())
            .withIcon(AngularJSIcons.Angular2)
            .withTypeText("pipe", null, true)
            .withInsertHandler(new JSLookupElementInsertHandler(false, null));
          Consumer<LookupElementBuilder> addResult = el ->
            result.consume(PrioritizedLookupElement.withPriority(el, NG_VARIABLE_PRIORITY.getPriorityValue()));
          if (match != null) {
            List<TypeScriptFunction> transformMethods = ContainerUtil.newArrayList(match.getTransformMethods());
            if (!transformMethods.isEmpty() && actualType != null) {
              Collections.sort(transformMethods,
                               Comparator.comparingInt(f -> isNullOrUndefinedType(f.getReturnType()) ? 1 : 0));
              List<FunctionTypeWithKind> resolveResults = new JSTypeSignatureChooser(
                parameters.getPosition(), Collections.singletonList(actualType), null, JSTypeDeclaration.EMPTY_ARRAY
              ).chooseOverload(ContainerUtil.map(transformMethods, TypeScriptTypeParser::buildFunctionType), true);
              for (FunctionTypeWithKind resolved : resolveResults) {
                if (resolved.getOverloadType().isAssignable()
                    || resolved.getOverloadType() == OverloadPriority.INCORRECT_PARAMETERS_LENGTH) {
                  JSFunctionType f = resolved.getJsFunction();
                  addResult.accept(builder.withTypeText(renderPipeTypeText(f, pipeEntry.getKey()), true));
                  break;
                }
              }
            }
            else {
              addResult.accept(builder);
            }
          }
          else {
            addResult.accept(builder.withItemTextForeground(SimpleTextAttributes.GRAYED_ATTRIBUTES.getFgColor()));
          }
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
              element, name, calcPriority(element), false,
              false));
          }
        });
        result.stopHere();
      }
    }

    private static JSType calcActualType(Angular2PipeReferenceExpression ref) {
      Angular2PipeExpression pipeCall = (Angular2PipeExpression)ref.getParent();
      Angular2PipeLeftSideArgument leftSideArgument = pipeCall.getLeftSideArgument();
      return leftSideArgument != null ? JSResolveUtil.getExpressionJSType(leftSideArgument.getExpression())
                                      : null;
    }

    private static String renderPipeTypeText(JSFunctionType f, String pipeName) {
      StringBuilder result = new StringBuilder();
      result.append('[');
      boolean first = true;
      for (JSParameterTypeDecorator param : f.getParameters()) {
        JSType type = param.getSimpleType();
        result.append("<")
          .append(type == null ? "*" : type.getTypeText()
            .replaceAll("\\|(null|undefined)", "")
            .replaceAll("String\\((.*?)\\)", "$1"))
          .append(param.isOptional() ? "?" : "")
          .append(">");
        if (first) {
          result.append(" | ")
            .append(pipeName);
          first = false;
        }
        result.append(":");
      }
      result.setLength(result.length() - 1);
      JSType type = f.getReturnType();
      return StringUtil.shortenTextWithEllipsis(
        result
          .append("] : <")
          .append(type == null ? "?" : type.getTypeText()
            .replaceAll("\\|(null|undefined)", ""))
          .append(">")
          .toString(),
        50, 0, true);
    }

    private static JSLookupPriority calcPriority(@NotNull JSPsiElementBase element) {
      if (Angular2Processor.$ANY.equals(element.getName())) {
        return NG_$ANY_PRIORITY;
      }
      return Angular2DecoratorUtil.isPrivateMember(element)
             ? NG_PRIVATE_VARIABLE_PRIORITY
             : NG_VARIABLE_PRIORITY;
    }

    @Nullable
    private static Set<Angular2Pipe> getModuleScope(@NotNull PsiElement element) {
      Angular2Module module = doIfNotNull(Angular2EntitiesProvider.getComponent(
        Angular2IndexingHandler.findComponentClass(element)), Angular2Component::getModule);
      return module != null ? module.getPipesInScope() : null;
    }
  }

  private static class AttributeNameCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {
      if (!Angular2LangUtil.isAngular2Context(parameters.getPosition())) {
        return;
      }
      PsiReference reference = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      if (reference instanceof PsiMultiReference) {
        reference = ContainerUtil.find(((PsiMultiReference)reference).getReferences(),
                                       XmlAttributeReference.class::isInstance);
      }
      if (reference instanceof XmlAttributeReference) {
        final XmlAttribute attribute = ((XmlAttributeReference)reference).getElement();
        final XmlTag tag = attribute.getParent();
        final XmlElementDescriptor parentDescriptor = tag.getDescriptor();
        if (parentDescriptor != null) {
          List<Angular2AttributesProvider> providers =
            Angular2AttributesProvider.ANGULAR_ATTRIBUTES_PROVIDER_EP.getExtensionList();

          List<Angular2AttributeDescriptor> descriptors = new ArrayList<>();
          MyCompletionResultsConsumer consumer = new MyCompletionResultsConsumer(result, descriptors);

          providers.forEach(provider -> provider.contributeCompletionResults(
            consumer, tag, result.getPrefixMatcher().getPrefix()));

          consumer.flushChanges();

          final PsiFile file = tag.getContainingFile();
          final XmlExtension extension = XmlExtension.getExtension(file);

          Set<Angular2Directive> moduleScope = getModuleScope(tag);
          final XmlAttribute[] attributes = tag.getAttributes();
          Set<String> providedAttributes = StreamEx.of(attributes)
            .map(attr -> attr.getDescriptor())
            .nonNull()
            .flatCollection(attr -> StreamEx.of(providers).toFlatList(provider -> provider.getRelatedAttributes(attr)))
            .toSet();
          for (XmlAttributeDescriptor descriptor : descriptors) {
            if (descriptor instanceof Angular2AttributeDescriptor) {
              if (!providedAttributes.contains(descriptor.getName())
                  && isValidVariant(attribute, descriptor, attributes, extension)) {
                Pair<LookupElement, String> elementWithPrefix =
                  ((Angular2AttributeDescriptor)descriptor).getLookupElementWithPrefix(
                    result.getPrefixMatcher(), moduleScope);
                if (elementWithPrefix.first == null) {
                  providedAttributes.add(elementWithPrefix.second);
                }
                else {
                  providedAttributes.add(elementWithPrefix.first.getLookupString());
                  result.withPrefixMatcher(result.getPrefixMatcher().cloneWithPrefix(elementWithPrefix.second))
                    .addElement(elementWithPrefix.first);
                }
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

    @Nullable
    private static Set<Angular2Directive> getModuleScope(@NotNull PsiElement element) {
      Angular2Module module = doIfNotNull(Angular2EntitiesProvider.getComponent(
        Angular2IndexingHandler.findComponentClass(element)), Angular2Component::getModule);
      return module != null ? module.getDirectivesInScope() : null;
    }
  }

  private static class MyCompletionResultsConsumer implements CompletionResultsConsumer {

    private final CompletionResultSet myResult;
    private final List<Angular2AttributeDescriptor> myDescriptors;
    private final Set<String> myPrefixes = new HashSet<>();

    private MyCompletionResultsConsumer(CompletionResultSet result, List<Angular2AttributeDescriptor> descriptors) {
      myResult = result;
      myDescriptors = descriptors;
    }

    public void flushChanges() {
      myResult.restartCompletionOnPrefixChange(string().oneOf(myPrefixes));
    }

    @Override
    public void addDescriptors(@NotNull List<Angular2AttributeDescriptor> descriptorsInner) {
      myDescriptors.addAll(descriptorsInner);
    }

    @Override
    public void addAbbreviation(@NotNull List<String> lookupNames,
                                @NotNull Angular2AttributeDescriptor.AttributePriority priority,
                                @Nullable String hidePrefix,
                                @Nullable String suffix) {
      assert !lookupNames.isEmpty();
      myPrefixes.addAll(lookupNames);
      CompletionResultSet resultSet = myResult;
      if (hidePrefix != null) {
        lookupNames = ContainerUtil.map(lookupNames, name -> StringUtil.trimStart(name, hidePrefix));
        resultSet = myResult.withPrefixMatcher(myResult.getPrefixMatcher().cloneWithPrefix(
          StringUtil.trimStart(myResult.getPrefixMatcher().getPrefix(), hidePrefix)));
      }
      resultSet.addElement(
        PrioritizedLookupElement.withPriority(
          LookupElementBuilder
            .create(lookupNames.get(0))
            .withLookupStrings(lookupNames)
            .withPresentableText(lookupNames.get(0) + "…" + (hidePrefix == null ? StringUtil.notNullize(suffix) : ""))
            .withIcon(AngularJSIcons.Angular2)
            .withInsertHandler((@NotNull InsertionContext context, @NotNull LookupElement item) -> {
              if (suffix != null) {
                new Angular2AttributeInsertHandler(false, false, suffix)
                  .handleInsert(context, item);
              }
              context.setLaterRunnable(() -> CodeCompletionHandlerBase.createHandler(CompletionType.BASIC)
                .invokeCompletion(context.getProject(), context.getEditor()));
            }),
          priority.getValue()));
    }
  }
}
