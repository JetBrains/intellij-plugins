// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.javascript.web.css.CssInBindingExpressionCompletionProvider;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.completion.JSLookupElementInsertHandler;
import com.intellij.lang.javascript.completion.JSLookupPriority;
import com.intellij.lang.javascript.completion.JSLookupUtilImpl;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser;
import com.intellij.lang.javascript.ecmascript6.types.JSTypeSignatureChooser.FunctionTypeWithKind;
import com.intellij.lang.javascript.ecmascript6.types.OverloadStrictness;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.JSTypeDeclaration;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.impl.JSReferenceExpressionImpl;
import com.intellij.lang.javascript.psi.types.JSFunctionTypeImpl;
import com.intellij.lang.javascript.psi.types.JSPsiBasedTypeOfType;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiMultiReference;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import icons.AngularJSIcons;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.template.Angular2StandardSymbolsScopesProvider;
import org.angular2.codeInsight.template.Angular2TemplateScopesResolver;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static com.intellij.lang.javascript.psi.JSTypeUtils.isNullOrUndefinedType;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.containers.ContainerUtil.newHashSet;

public class Angular2CompletionContributor extends CompletionContributor {

  private static final JSLookupPriority NG_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY;
  private static final JSLookupPriority NG_PRIVATE_VARIABLE_PRIORITY = JSLookupPriority.LOCAL_SCOPE_MAX_PRIORITY_EXOTIC;
  private static final JSLookupPriority NG_$ANY_PRIORITY = JSLookupPriority.TOP_LEVEL_SYMBOLS_FROM_OTHER_FILES;

  @NonNls private static final Set<String> NG_LIFECYCLE_HOOKS = newHashSet(
    "ngOnChanges", "ngOnInit", "ngDoCheck", "ngOnDestroy", "ngAfterContentInit",
    "ngAfterContentChecked", "ngAfterViewInit", "ngAfterViewChecked");

  public Angular2CompletionContributor() {

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           new CssInBindingExpressionCompletionProvider());

    extend(CompletionType.BASIC,
           psiElement().with(language(Angular2Language.INSTANCE)),
           new TemplateExpressionCompletionProvider());
  }

  private static <T extends PsiElement> PatternCondition<T> language(@NotNull Language language) {
    return new PatternCondition<>("language(" + language.getID() + ")") {
      @Override
      public boolean accepts(@NotNull T t, ProcessingContext context) {
        return language.is(PsiUtilCore.findLanguageFromElement(t));
      }
    };
  }

  private static class TemplateExpressionCompletionProvider extends CompletionProvider<CompletionParameters> {

    @Override
    protected void addCompletions(final @NotNull CompletionParameters parameters,
                                  final @NotNull ProcessingContext context,
                                  final @NotNull CompletionResultSet result) {
      PsiReference ref = parameters.getPosition().getContainingFile().findReferenceAt(parameters.getOffset());
      if (ref instanceof PsiMultiReference) {
        ref = ContainerUtil.find(((PsiMultiReference)ref).getReferences(), r ->
          r instanceof Angular2PipeReferenceExpression
          || r instanceof JSReferenceExpressionImpl);
      }
      if (ref instanceof Angular2PipeReferenceExpression) {
        Angular2DeclarationsScope scope = new Angular2DeclarationsScope(parameters.getPosition());
        JSType actualType = calcActualType(((Angular2PipeReferenceExpression)ref));
        for (Map.Entry<String, List<Angular2Pipe>> pipeEntry : Angular2EntitiesProvider
          .getAllPipes(((Angular2PipeReferenceExpression)ref).getProject()).entrySet()) {
          Pair<Angular2Pipe, DeclarationProximity> bestMatch = scope.getClosestDeclaration(pipeEntry.getValue());
          if (bestMatch == null || bestMatch.second == DeclarationProximity.NOT_REACHABLE) {
            continue;
          }
          Angular2Pipe match = bestMatch.first;
          LookupElementBuilder builder = LookupElementBuilder.create(pipeEntry.getKey())
            .withIcon(AngularJSIcons.Angular2)
            .withTypeText(Angular2Bundle.message("angular.description.pipe"), null, true)
            .withInsertHandler(new JSLookupElementInsertHandler(false, null));
          if (bestMatch.second != DeclarationProximity.IN_SCOPE) {
            builder = Angular2CodeInsightUtils.wrapWithImportDeclarationModuleHandler(
              Angular2CodeInsightUtils.decorateLookupElementWithModuleSource(builder, Collections.singletonList(bestMatch.first),
                                                                             bestMatch.second, scope),
              Angular2PipeReferenceExpression.class);
          }
          Consumer<LookupElementBuilder> addResult = el ->
            result.consume(PrioritizedLookupElement.withPriority(el, bestMatch.second == DeclarationProximity.IN_SCOPE
                                                                     || bestMatch.second == DeclarationProximity.IMPORTABLE
                                                                     ? NG_VARIABLE_PRIORITY.getPriorityValue()
                                                                     : NG_PRIVATE_VARIABLE_PRIORITY.getPriorityValue()));
          List<TypeScriptFunction> transformMethods = new ArrayList<>(match.getTransformMethods());
          if (!transformMethods.isEmpty() && actualType != null) {
            transformMethods.sort(Comparator.
                                    <TypeScriptFunction>comparingInt(f -> isNullOrUndefinedType(f.getReturnType()) ? 1 : 0)
                                    .thenComparingInt(f -> f.isOverloadDeclaration() ? 0 : 1));
            Map<JSFunctionType, TypeScriptFunction> converted2Original = new LinkedHashMap<>();
            transformMethods.forEach(f -> {
              JSFunctionType type = TypeScriptTypeParser.buildFunctionType(f);
              converted2Original.put(toTypeWithOneParam(type), f);
            });
            List<FunctionTypeWithKind> resolveResults = new JSTypeSignatureChooser(
              parameters.getPosition(), Collections.singletonList(actualType), null, JSTypeDeclaration.EMPTY_ARRAY
            ).chooseOverload(converted2Original.keySet(), OverloadStrictness.FULL);
            for (FunctionTypeWithKind resolved : resolveResults) {
              if (resolved.getOverloadType().isAssignable()) {
                JSFunctionType f = resolved.getJsFunction();
                addResult.accept(builder.withTypeText(renderPipeTypeText(converted2Original.get(f), pipeEntry.getKey()), true));
                break;
              }
            }
          }
          else {
            addResult.accept(builder);
          }
        }
        result.stopHere();
      }
      else if (ref instanceof JSReferenceExpressionImpl
               && (((JSReferenceExpressionImpl)ref).getQualifier() == null
                   || ((JSReferenceExpressionImpl)ref).getQualifier() instanceof JSThisExpression)) {
        final Set<String> contributedElements = new HashSet<>();
        Angular2TemplateScopesResolver.resolve(parameters.getPosition(), resolveResult -> {
          final JSPsiElementBase element = ObjectUtils.tryCast(resolveResult.getElement(), JSPsiElementBase.class);
          if (element == null) {
            return true;
          }
          final String name = element.getName();
          if (name != null && !NG_LIFECYCLE_HOOKS.contains(name)
              && contributedElements.add(name + "#" + JSLookupUtilImpl.getTypeAndTailTexts(element, null).getTailAndType())) {
            result.consume(JSLookupUtilImpl.createPrioritizedLookupItem(
              element, name, calcPriority(element)
            ));
          }
          return true;
        });
        result.stopHere();
      }
    }

    private static JSFunctionType toTypeWithOneParam(@NotNull JSFunctionType type) {
      return type.getParameters().size() <= 1
             ? type
             : new JSFunctionTypeImpl(type.getSource(), Collections.singletonList(type.getParameters().get(0)),
                                      type.getReturnType());
    }

    private static @Nullable JSType calcActualType(Angular2PipeReferenceExpression ref) {
      Angular2PipeExpression pipeCall = (Angular2PipeExpression)ref.getParent();
      return doIfNotNull(ArrayUtil.getFirstElement(pipeCall.getArguments()),
                         expression -> new JSPsiBasedTypeOfType(expression, true));
    }

    private static String renderPipeTypeText(@NotNull TypeScriptFunction f, @NotNull String pipeName) {
      StringBuilder result = new StringBuilder();
      result.append('[');
      boolean first = true;
      for (JSParameterListElement param : f.getParameters()) {
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
      if (Angular2StandardSymbolsScopesProvider.$ANY.equals(element.getName())) {
        return NG_$ANY_PRIORITY;
      }
      return Angular2DecoratorUtil.isPrivateMember(element)
             ? NG_PRIVATE_VARIABLE_PRIORITY
             : NG_VARIABLE_PRIORITY;
    }
  }
}
