// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSGenericTypesEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.*;
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider;
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.containers.Stack;
import com.intellij.xml.util.documentation.HtmlDescriptorsTable;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.*;
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl;
import org.angularjs.codeInsight.DirectiveUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Angular2Processor {
  private static volatile Map<String, String> TAG_TO_CLASS;

  public static final String $EVENT = "$event";
  public static final String NG_TEMPLATE = "ng-template";

  public static void process(final PsiElement element, final Consumer<? super JSPsiElementBase> consumer) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    if (!checkLanguage(original)) {
      return;
    }
    final PsiFile file = original.getContainingFile();

    final Angular2TemplateScope templateRootScope = CachedValuesManager.getCachedValue(file, () -> {
      final Angular2TemplateScope result = new Angular2TemplateScopeBuilder(file).getTopLevelScope();
      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });

    calculateElementScopes(element, templateRootScope)
      .forEach(s -> s.consumeElementsFromAllScopes(consumer));
  }

  private static boolean checkLanguage(PsiElement element) {
    return element.getLanguage().is(Angular2Language.INSTANCE)
           || element.getLanguage().is(Angular2HtmlLanguage.INSTANCE)
           || (element.getParent() != null
               && (element.getParent().getLanguage().is(Angular2Language.INSTANCE)
                   || element.getParent().getLanguage().is(Angular2HtmlLanguage.INSTANCE)
               ));
  }

  private static List<Angular2Scope> calculateElementScopes(PsiElement element,
                                                            Angular2TemplateScope rootTemplateScope) {
    List<Angular2Scope> scopes = new ArrayList<>();

    final JSClass clazz = Angular2IndexingHandler.findDirectiveClass(element);
    if (clazz != null && DialectDetector.isTypeScript(clazz)) {
      scopes.add(new Angular2ComponentScope(clazz));
    }
    scopes.add(Objects.requireNonNull(rootTemplateScope.findBestMatchingTemplateScope(element)));

    if (element instanceof JSElement
        || element.getParent() instanceof JSElement) {
      PsiElement attribute = element;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)
             && !(attribute instanceof XmlTag)) {
        attribute = attribute.getParent();
      }
      //if (attribute instanceof XmlAttribute) {
      //  scopes.add(new TagDirectivesScope(element));
      //}
      if (attribute instanceof Angular2HtmlEvent) {
        scopes.add(new Angular2EventScope((Angular2HtmlEvent)attribute));
      }
    }
    return ContainerUtil.reverse(scopes);
  }

  @NotNull
  @NonNls
  private static String getHtmlElementClass(@NotNull Project project, @NotNull @NonNls String tagName) {
    if (TAG_TO_CLASS == null) {
      initTagToClassMap(project);
    }
    return TAG_TO_CLASS.getOrDefault(tagName.toLowerCase(), "HTMLElement");
  }

  private static synchronized void initTagToClassMap(@NotNull Project project) {
    if (TAG_TO_CLASS != null) {
      return;
    }
    Map<String, String> tagToClass = new HashMap<>();
    Collection<VirtualFile> libs = JSCorePredefinedLibrariesProvider
      .getAllJSPredefinedLibraryFiles()
      .stream()
      .filter(lib -> TypeScriptLibraryProvider.LIB_DOM_D_TS.equals(lib.getName()))
      .collect(Collectors.toList());

    final List<JSClass> elements = TypeScriptClassResolver.getInstance().findClassesByQName(
      "HTMLElementTagNameMap", GlobalSearchScope.filesScope(project, libs));

    for (JSQualifiedNamedElement el : elements) {
      if (!(el instanceof TypeScriptInterface)) {
        continue;
      }
      TypeScriptInterface intf = (TypeScriptInterface)el;
      if (intf.getBody() == null) {
        continue;
      }
      for (TypeScriptTypeMember member : intf.getBody().getTypeMembers()) {
        if (!(member instanceof TypeScriptPropertySignature)) {
          continue;
        }
        TypeScriptPropertySignature sig = (TypeScriptPropertySignature)member;
        JSTypeDeclaration decl = sig.getTypeDeclaration();
        if (!(decl instanceof TypeScriptSingleType)) {
          continue;
        }
        @NonNls
        String tagName = sig.getMemberName();
        String className = ((TypeScriptSingleType)decl).getQualifiedTypeName();
        if (className != null && className.startsWith("HTML") && className.endsWith("Element")) {
          tagToClass.put(tagName.toLowerCase(), className);
        }
      }
    }
    TAG_TO_CLASS = tagToClass;
  }

  @NotNull
  private static List<JSPsiElementBase> resolveBindings(@NotNull Angular2HtmlTemplateBindings bindings) {
    Angular2TemplateBindings bindingsExpr = bindings.getBindings();
    List<JSPsiElementBase> result = new ArrayList<>();
    if (bindingsExpr != null) {
      JSRecordType templateContext = resolveTemplateContext(bindings.getTemplateName(), bindings.getBindings());
      for (Angular2TemplateBinding binding : bindingsExpr.getBindings()) {
        if (binding.keyIsVar()) {
          JSRecordType.PropertySignature prop = templateContext != null && binding.getName() != null ?
                                                templateContext.findPropertySignature(binding.getName())
                                                                                                     : null;
          result.add(createVariable(binding.getKey(), ObjectUtils.notNull(binding.getVariableDefinition(), binding),
                                    prop));
        }
      }
    }
    return result;
  }

  @Nullable
  private static JSRecordType resolveTemplateContext(@NotNull String templateName, @NotNull Angular2TemplateBindings bindings) {
    JSImplicitElement templateDirective = DirectiveUtil.getAttributeDirective(
      "*" + templateName, bindings.getProject());
    if (templateDirective == null) {
      return null;
    }
    AngularDirectiveMetadata metadata = AngularDirectiveMetadata.create(templateDirective);
    if (!(metadata.getDirectiveClass() instanceof TypeScriptClass)) {
      return null;
    }
    TypeScriptClass clazz = (TypeScriptClass)metadata.getDirectiveClass();
    JSType templateRefType = null;
    for (TypeScriptFunction fun : clazz.getConstructors()) {
      for (JSParameter param : fun.getParameterVariables()) {
        if (param.getType() != null && param.getType().getTypeText().startsWith("TemplateRef<")) {
          templateRefType = param.getType();
          break;
        }
      }
    }
    if (!(templateRefType instanceof JSGenericTypeImpl)) {
      return null;
    }
    JSGenericTypeImpl templateRefGeneric = (JSGenericTypeImpl)templateRefType;
    if (templateRefGeneric.getArguments().isEmpty()) {
      return null;
    }
    JSType templateContextType = templateRefGeneric.getArguments().get(0);
    return templateContextType instanceof JSGenericTypeImpl
           ? resolveTemplateContextTypeGeneric(metadata, (JSGenericTypeImpl)templateContextType, bindings)
           : templateContextType.asRecordType();
  }

  private static JSRecordType resolveTemplateContextTypeGeneric(AngularDirectiveMetadata metadata,
                                                                @NotNull JSGenericTypeImpl templateContextType,
                                                                @NotNull Angular2TemplateBindings bindings) {
    Map<String, Angular2TemplateBinding> bindingsMap = Arrays.stream(bindings.getBindings())
      .filter(b -> !b.keyIsVar())
      .collect(Collectors.toMap(Angular2TemplateBinding::getKey, Function.identity()));

    MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> genericArguments = MultiMap.createSmart();
    final ProcessingContext processingContext = JSTypeComparingContextService.getProcessingContextWithCache(metadata.getDirectiveClass());

    metadata.getInputs().forEach(info -> {
      Angular2TemplateBinding binding = bindingsMap.get(info.name);
      if (binding != null && info.signature != null) {
        JSType expressionType = JSResolveUtil.getExpressionJSType(binding.getExpression());
        JSType paramType = info.signature.getType();
        if (expressionType != null && paramType != null) {
          JSGenericTypesEvaluator.matchGenericTypes(genericArguments, processingContext, expressionType, paramType, null);
        }
      }
    });
    JSTypeSubstitutor substitutor = intersectGenerics(genericArguments, templateContextType);
    JSType resultType = JSTypeUtils.applyGenericArguments(templateContextType, substitutor, false);
    return resultType.asRecordType();
  }

  private static JSTypeSubstitutor intersectGenerics(MultiMap<JSTypeSubstitutor.JSTypeGenericId, JSType> arguments,
                                                     JSGenericTypeImpl templateContextType) {
    JSTypeSubstitutor result = new JSTypeSubstitutor();
    for (Map.Entry<JSTypeSubstitutor.JSTypeGenericId, Collection<JSType>> entry : arguments.entrySet()) {
      List<JSType> types = StreamEx.of(entry.getValue()).nonNull().toList();
      if (types.size() == 1) {
        result.put(entry.getKey(), types.get(0));
        continue;
      }
      JSType type = new JSCompositeTypeImpl(templateContextType.getSource(), types);
      result.put(entry.getKey(), JSCompositeTypeImpl.optimizeTypeIfComposite(type, true));
    }
    return result;
  }

  private static JSImplicitElement createVariable(@NotNull String name,
                                                  @NotNull PsiElement contributor,
                                                  JSRecordType.PropertySignature property) {
    if (property != null && property.getType() != null) {
      return new JSLocalImplicitElementImpl(name, property.getType(), contributor, JSImplicitElement.Type.Variable);
    }
    return new JSImplicitElementImpl.Builder(name, contributor)
      .setType(JSImplicitElement.Type.Variable).toImplicitElement();
  }

  private static JSImplicitElement createReference(@NotNull Angular2HtmlReference reference) {
    final HtmlTag tag = (HtmlTag)reference.getParent();
    final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(reference.getReferenceName(), reference)
      .setType(JSImplicitElement.Type.Variable);

    final String tagName = tag.getName();
    if (HtmlDescriptorsTable.getTagDescriptor(tagName) != null
        && reference.getValueElement() == null) {
      elementBuilder.setTypeString(getHtmlElementClass(tag.getProject(), tagName));
    }
    return elementBuilder.toImplicitElement();
  }

  private static class Angular2TemplateScopeBuilder extends Angular2HtmlRecursiveElementVisitor {

    @NotNull
    private final PsiFile myTemplateFile;
    private final Stack<Angular2TemplateScope> scopes = new Stack<>();

    public Angular2TemplateScopeBuilder(@NotNull PsiFile templateFile) {
      myTemplateFile = templateFile;
      scopes.add(new Angular2TemplateScope(templateFile, null));
    }

    public Angular2TemplateScope getTopLevelScope() {
      myTemplateFile.accept(this);
      assert scopes.size() == 1;
      return scopes.peek();
    }

    private Angular2TemplateScope currentScope() {
      return scopes.peek();
    }

    private void popScope() {
      scopes.pop();
    }

    private void pushScope(@NotNull XmlTag tag) {
      scopes.push(new Angular2TemplateScope(tag, currentScope()));
    }

    private void addElement(JSImplicitElement element) {
      currentScope().add(element);
    }

    private void addBindings(Angular2HtmlTemplateBindings bindings) {
      currentScope().add(bindings);
    }

    @Override
    public void visitXmlTag(XmlTag tag) {
      boolean isTemplateTag = Stream.of(tag.getChildren()).anyMatch(Angular2HtmlTemplateBindings.class::isInstance)
                              || tag.getName().equalsIgnoreCase(NG_TEMPLATE);
      if (isTemplateTag) {
        pushScope(tag);
      }
      super.visitXmlTag(tag);
      if (isTemplateTag) {
        popScope();
      }
    }

    @Override
    public void visitBoundAttribute(Angular2HtmlBoundAttribute boundAttribute) {
      //do not visit expressions
    }

    @Override
    public void visitReference(Angular2HtmlReference reference) {
      addElement(createReference(reference));
    }

    @Override
    public void visitVariable(Angular2HtmlVariable variable) {
      addElement(createVariable(variable.getVariableName(), variable, null));
    }

    @Override
    public void visitTemplateBindings(Angular2HtmlTemplateBindingsImpl bindings) {
      addBindings(bindings);
    }
  }


  private abstract static class Angular2Scope {

    @Nullable
    private final Angular2Scope myParent;
    private final List<Angular2Scope> children = new ArrayList<>();

    private Angular2Scope(@Nullable Angular2Scope parent) {
      myParent = parent;
      if (parent != null) {
        parent.add(this);
      }
    }

    @Nullable
    public final Angular2Scope getParent() {
      return myParent;
    }

    public List<Angular2Scope> getChildren() {
      return Collections.unmodifiableList(children);
    }

    private void add(Angular2Scope scope) {
      this.children.add(scope);
    }

    public final void consumeElementsFromAllScopes(@NotNull Consumer<? super JSPsiElementBase> consumer) {
      Angular2Scope scope = this;
      while (scope != null) {
        scope.getElements().forEach(el -> consumer.consume(el));
        scope = scope.getParent();
      }
    }

    @NotNull
    public abstract List<JSPsiElementBase> getElements();
  }

  private static class Angular2TemplateScope extends Angular2Scope {

    private final List<JSPsiElementBase> psiElements = new ArrayList<>();
    private final List<Angular2HtmlTemplateBindings> bindings = new ArrayList<>();

    @NotNull private final TextRange myRange;

    public Angular2TemplateScope(@NotNull PsiElement root, @Nullable Angular2TemplateScope parent) {
      super(parent);
      myRange = root.getTextRange();
      if (parent != null) {
        assert parent.myRange.contains(myRange);
      }
    }

    @Override
    @NotNull
    public List<JSPsiElementBase> getElements() {
      if (bindings.isEmpty()) {
        return psiElements;
      }
      List<JSPsiElementBase> result = new ArrayList<>(psiElements);
      for (Angular2HtmlTemplateBindings b : bindings) {
        JSTypeEvaluator.processWithEvaluationGuard(
          b, JSEvaluateContext.JSEvaluationPlace.DEFAULT, bindings ->
            result.addAll(CachedValuesManager.getCachedValue(bindings, () ->
              CachedValueProvider.Result.create(resolveBindings(bindings), PsiModificationTracker.MODIFICATION_COUNT))
            )
        );
      }
      return result;
    }

    public void add(@NotNull JSPsiElementBase element) {
      psiElements.add(element);
    }

    public void add(@NotNull Angular2HtmlTemplateBindings bindings) {
      this.bindings.add(bindings);
    }

    @Nullable
    public Angular2TemplateScope findBestMatchingTemplateScope(@NotNull PsiElement element) {
      if (!myRange.contains(element.getTextOffset())) {
        return null;
      }
      Angular2TemplateScope curScope = null;
      Angular2TemplateScope innerScope = this;
      while (innerScope != null) {
        curScope = innerScope;
        innerScope = null;
        for (Angular2Scope child : curScope.getChildren()) {
          if (child instanceof Angular2TemplateScope
              && ((Angular2TemplateScope)child).myRange.contains(element.getTextOffset())) {
            innerScope = (Angular2TemplateScope)child;
            break;
          }
        }
      }
      if (PsiTreeUtil.getParentOfType(element, Angular2HtmlTemplateBindings.class) != null
          && curScope != this) {
        curScope = (Angular2TemplateScope)curScope.getParent();
      }
      return curScope;
    }
  }

  private static class Angular2EventScope extends Angular2Scope {

    private final Angular2HtmlEvent myEvent;

    private Angular2EventScope(@NotNull Angular2HtmlEvent event) {
      super(null);
      myEvent = event;
    }

    @Override
    @NotNull
    public List<JSPsiElementBase> getElements() {
      return Collections.singletonList(new JSImplicitElementImpl.Builder($EVENT, myEvent).
        setType(JSImplicitElement.Type.Variable).toImplicitElement());
    }
  }

  private static class Angular2ComponentScope extends Angular2Scope {

    private final JSClass myJsClass;

    public Angular2ComponentScope(JSClass jsClass) {
      super(null);
      myJsClass = jsClass;
    }

    @NotNull
    @Override
    public List<JSPsiElementBase> getElements() {
      return TypeScriptTypeParser
        .buildTypeFromClass(myJsClass, false)
        .getProperties()
        .stream()
        .map(prop -> prop.getMemberSource().getSingleElement())
        .filter(el -> el instanceof JSPsiElementBase)
        .map(el -> (JSPsiElementBase)el)
        .collect(Collectors.toList());
    }
  }
}
