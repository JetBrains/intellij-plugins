// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.ecmascript6.ES6QualifiedNamedElementRenderer;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Entity;
import org.angular2.inspections.actions.Angular2ActionFactory;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.intellij.util.ObjectUtils.notNull;
import static com.intellij.util.ObjectUtils.tryCast;
import static com.intellij.util.containers.ContainerUtil.exists;
import static com.intellij.util.containers.ContainerUtil.find;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity.*;

public class Angular2FixesFactory {

  @TestOnly
  @NonNls public static final Key<String> DECLARATION_TO_CHOOSE = Key.create("declaration.to.choose");

  public static void ensureDeclarationResolvedAfterCodeCompletion(@NotNull PsiElement element, @NotNull Editor editor) {
    MultiMap<DeclarationProximity, Angular2Declaration> candidates = getCandidatesForResolution(element, true);
    if (!candidates.get(EXPORTED_BY_PUBLIC_MODULE).isEmpty()) {
      Angular2ActionFactory.createNgModuleImportAction(editor, element, true).execute();
    }
    else if (!candidates.get(NOT_DECLARED_IN_ANY_MODULE).isEmpty()) {
      selectAndRun(editor, Angular2Bundle.message("angular.quickfix.ngmodule.declare.select.declarable"),
                   candidates.get(NOT_DECLARED_IN_ANY_MODULE), candidate ->
                     Angular2ActionFactory.createAddNgModuleDeclarationAction(editor, element, candidate, true));
    }
    else if (!candidates.get(NOT_EXPORTED_BY_MODULE).isEmpty()) {
      selectAndRun(editor, Angular2Bundle.message("angular.quickfix.ngmodule.export.select.declarable"),
                   candidates.get(NOT_EXPORTED_BY_MODULE), candidate ->
                     Angular2ActionFactory.createExportNgModuleDeclarationAction(editor, element, candidate, true));
    }
  }

  public static void addUnresolvedDeclarationFixes(@NotNull PsiElement element, @NotNull List<LocalQuickFix> fixes) {
    MultiMap<DeclarationProximity, Angular2Declaration> candidates = getCandidatesForResolution(element, false);
    if (candidates.containsKey(IN_SCOPE)) {
      return;
    }
    if (!candidates.get(EXPORTED_BY_PUBLIC_MODULE).isEmpty()) {
      fixes.add(new AddNgModuleImportQuickFix(element, candidates.get(EXPORTED_BY_PUBLIC_MODULE)));
    }
    for (Angular2Declaration declaration : candidates.get(NOT_DECLARED_IN_ANY_MODULE)) {
      AddNgModuleDeclarationQuickFix.add(element, declaration, fixes);
    }
    for (Angular2Declaration declaration : candidates.get(NOT_EXPORTED_BY_MODULE)) {
      ExportNgModuleDeclarationQuickFix.add(element, declaration, fixes);
    }
  }

  @NotNull
  public static MultiMap<DeclarationProximity, Angular2Declaration> getCandidatesForResolution(@NotNull PsiElement element,
                                                                                               boolean codeCompletion) {
    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(element);
    if (scope.getModule() == null || !scope.isInSource(scope.getModule())) {
      return MultiMap.empty();
    }
    Ref<Predicate<Angular2Declaration>> filter = new Ref<>(declaration -> true);
    final Supplier<List<? extends Angular2Declaration>> provider;
    final Supplier<List<? extends Angular2Declaration>> secondaryProvider;
    if (element instanceof XmlAttribute) {
      Angular2AttributeDescriptor attributeDescriptor = tryCast(((XmlAttribute)element).getDescriptor(),
                                                                Angular2AttributeDescriptor.class);
      if (attributeDescriptor == null) {
        return MultiMap.empty();
      }
      AttributeInfo info = attributeDescriptor.getInfo();
      provider = new Angular2ApplicableDirectivesProvider(((XmlAttribute)element).getParent())::getMatched;
      secondaryProvider = info.type == Angular2AttributeType.REFERENCE ? null : attributeDescriptor::getSourceDirectives;

      switch (info.type) {
        case PROPERTY_BINDING:
          if (((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType != PropertyBindingType.PROPERTY) {
            return MultiMap.empty();
          }
          filter.set(declaration -> declaration instanceof Angular2Directive
                                    && exists(((Angular2Directive)declaration).getInputs(),
                                              input -> info.name.equals(input.getName())));
          break;
        case EVENT:
          if (((Angular2AttributeNameParser.EventInfo)info).eventType != Angular2HtmlEvent.EventType.REGULAR) {
            return MultiMap.empty();
          }
          filter.set(declaration -> declaration instanceof Angular2Directive
                                    && exists(((Angular2Directive)declaration).getOutputs(),
                                              output -> info.name.equals(output.getName())));
        case BANANA_BOX_BINDING:
          filter.set(declaration -> declaration instanceof Angular2Directive
                                    && exists(((Angular2Directive)declaration).getInOuts(),
                                              inout -> info.name.equals(inout.first.getName())));
          break;
        case REGULAR:
          filter.set(declaration -> declaration instanceof Angular2Directive
                                    && (exists(((Angular2Directive)declaration).getInputs(),
                                               input -> info.name.equals(input.getName())
                                                        && Angular2AttributeDescriptor.isOneTimeBindingProperty(input))
                                        || exists(((Angular2Directive)declaration).getSelector().getSimpleSelectors(),
                                                  selector -> exists(selector.getAttrNames(), info.name::equals))));
          break;
        case REFERENCE:
          String exportName = ((XmlAttribute)element).getValue();
          if (exportName == null || exportName.isEmpty()) {
            return MultiMap.empty();
          }
          filter.set(declaration -> declaration instanceof Angular2Directive
                                    && ((Angular2Directive)declaration).getExportAsList().contains(exportName));
          break;
        default:
          return MultiMap.empty();
      }
    }
    else if (element instanceof XmlTag) {
      provider = new Angular2ApplicableDirectivesProvider((XmlTag)element, true)::getMatched;
      secondaryProvider = null;
    }
    else if (element instanceof Angular2TemplateBinding) {
      provider = new Angular2ApplicableDirectivesProvider((Angular2TemplateBindings)element.getParent())::getMatched;
      secondaryProvider = createSecondaryProvider((Angular2TemplateBindings)element.getParent());
      if (((Angular2TemplateBinding)element).keyIsVar()) {
        return MultiMap.empty();
      }
      String key = ((Angular2TemplateBinding)element).getKey();
      filter.set(declaration -> declaration instanceof Angular2Directive
                                && exists(((Angular2Directive)declaration).getInputs(),
                                          input -> key.equals(input.getName())));
    }
    else if (element instanceof Angular2TemplateBindings) {
      provider = new Angular2ApplicableDirectivesProvider((Angular2TemplateBindings)element)::getMatched;
      secondaryProvider = createSecondaryProvider((Angular2TemplateBindings)element);
    }
    else if (element instanceof Angular2PipeReferenceExpression) {
      String referencedName = ((Angular2PipeReferenceExpression)element).getReferenceName();
      if (referencedName == null || referencedName.isEmpty()) {
        return MultiMap.empty();
      }
      provider = () -> Angular2EntitiesProvider.findPipes(element.getProject(), referencedName);
      secondaryProvider = null;
    }
    else {
      throw new IllegalArgumentException(element.getClass().getName());
    }
    MultiMap<DeclarationProximity, Angular2Declaration> result = new MultiMap<>();

    Consumer<Supplier<List<? extends Angular2Declaration>>> declarationProcessor = p -> StreamEx.of(p.get())
      .filter(filter.get())
      .forEach(declaration -> result.putValue(scope.getDeclarationProximity(declaration), declaration));

    declarationProcessor.consume(provider);
    if (result.isEmpty() && codeCompletion && secondaryProvider != null) {
      declarationProcessor.consume(secondaryProvider);
    }
    return result;
  }

  @NotNull
  private static Supplier<List<? extends Angular2Declaration>> createSecondaryProvider(@NotNull Angular2TemplateBindings bindings) {
    return () -> Optional.of(notNull(InjectedLanguageManager.getInstance(bindings.getProject()).getInjectionHost(bindings), bindings))
      .map(element -> PsiTreeUtil.getParentOfType(element, XmlAttribute.class))
      .map(XmlAttribute::getDescriptor)
      .map(d -> tryCast(d, Angular2AttributeDescriptor.class))
      .map(Angular2AttributeDescriptor::getSourceDirectives)
      .orElse(Collections.emptyList());
  }


  private static void selectAndRun(@NotNull Editor editor,
                                   @NotNull String title,
                                   @NotNull Collection<Angular2Declaration> declarations,
                                   @NotNull Function<Angular2Declaration, QuestionAction> actionFactory) {
    if (declarations.isEmpty()) {
      return;
    }

    ApplicationManager.getApplication().assertIsDispatchThread();
    Map<JSElement, Angular2Declaration> elementMap = StreamEx.of(declarations)
      .mapToEntry(Angular2Declaration::getTypeScriptClass, Function.identity())
      .selectKeys(JSElement.class)
      .toMap();

    PsiElementProcessor<JSElement> processor = new PsiElementProcessor<JSElement>() {
      @Override
      public boolean execute(@NotNull final JSElement element) {
        Optional.ofNullable(elementMap.get(element))
          .map(actionFactory)
          .ifPresent(QuestionAction::execute);
        return false;
      }
    };

    if (ApplicationManager.getApplication().isUnitTestMode()) {
      //noinspection TestOnlyProblems
      processor.execute(
        Optional.ofNullable(editor.getUserData(DECLARATION_TO_CHOOSE))
          .map(name -> find(declarations, declaration -> declaration.getName().equals(name)))
          .map(Angular2Entity::getTypeScriptClass)
          .orElseThrow(
            () -> new AssertionError("Declaration name must be specified in test mode. Available names: " +
                                     StreamEx.of(declarations)
                                       .filter(decl -> decl.getTypeScriptClass() != null)
                                       .map(Angular2Declaration::getName)
                                       .joining(",")
            ))
      );
      return;
    }
    if (editor.isDisposed()) return;

    NavigationUtil.getPsiElementPopup(elementMap.keySet().toArray(JSElement.EMPTY_ARRAY),
                                      new ES6QualifiedNamedElementRenderer<>(),
                                      title,
                                      processor)
      .showInBestPositionFor(editor);
  }
}
