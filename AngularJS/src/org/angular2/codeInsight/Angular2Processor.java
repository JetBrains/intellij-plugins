// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature;
import com.intellij.lang.javascript.psi.ecma6.impl.JSLocalImplicitElementImpl;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitParameterStructure;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import com.intellij.xml.XmlAttributeDescriptor;
import one.util.streamex.StreamEx;
import org.angular2.Angular2InjectionUtils;
import org.angular2.codeInsight.tags.Angular2TagDescriptorsProvider;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.intellij.util.ObjectUtils.notNull;

public class Angular2Processor {

  @NonNls public static final String $EVENT = "$event";
  @NonNls public static final String $ANY = "$any";

  @NonNls private static final String LEGACY_TEMPLATE_TAG = "template";
  @NonNls private static final String HTML_ELEMENT_CLASS_NAME = "HTMLElement";
  @NonNls private static final String HTML_ELEMENT_TAG_NAME_MAP_CLASS_NAME = "HTMLElementTagNameMap";

  public static boolean isTemplateTag(@Nullable String tagName) {
    return Angular2TagDescriptorsProvider.NG_TEMPLATE.equalsIgnoreCase(tagName)
           || LEGACY_TEMPLATE_TAG.equalsIgnoreCase(tagName);
  }

  public static void process(final @NotNull PsiElement element, @NotNull final Consumer<? super ResolveResult> consumer) {
    PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    if (!checkLanguage(original)) {
      return;
    }
    boolean expressionIsInjected = original.getContainingFile().getLanguage().is(Angular2Language.INSTANCE);
    final PsiElement hostElement;
    if (expressionIsInjected) {
      //we are working within injection
      hostElement = InjectedLanguageManager.getInstance(element.getProject()).getInjectionHost(element);
      if (hostElement == null) {
        return;
      }
      original = CompletionUtil.getOriginalOrSelf(hostElement);
    }
    else {
      hostElement = element;
    }
    final PsiFile hostFile = original.getContainingFile();
    final Angular2TemplateScope templateRootScope = CachedValuesManager.getCachedValue(hostFile, () -> {
      final Angular2TemplateScope result;
      if (!expressionIsInjected) {
        result = new Angular2TemplateScopeBuilder(hostFile).getTopLevelScope();
      }
      else {
        result = new Angular2ForeignTemplateScopeBuilder(hostFile).getTopLevelScope();
      }
      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });

    calculateElementScopes(element, hostElement, templateRootScope)
      .forEach(s -> s.consumeResultsFromAllScopes(consumer));
  }

  private static boolean checkLanguage(@NotNull PsiElement element) {
    return element.getLanguage().is(Angular2Language.INSTANCE)
           || element.getLanguage().is(Angular2HtmlLanguage.INSTANCE)
           || (element.getParent() != null
               && (element.getParent().getLanguage().is(Angular2Language.INSTANCE)
                   || element.getParent().getLanguage().is(Angular2HtmlLanguage.INSTANCE)
               ));
  }

  @NotNull
  private static List<Angular2Scope> calculateElementScopes(@NotNull PsiElement element,
                                                            @NotNull PsiElement hostElement,
                                                            @NotNull Angular2TemplateScope rootTemplateScope) {
    List<Angular2Scope> scopes = new ArrayList<>();

    final TypeScriptClass clazz = Angular2IndexingHandler.findComponentClass(element);
    if (clazz != null) {
      scopes.add(new Angular2ComponentScope(clazz));
    }
    scopes.add(Objects.requireNonNull(rootTemplateScope.findBestMatchingTemplateScope(hostElement)));
    scopes.add(new Angular2$AnyScope(element.getContainingFile()));

    if (element != hostElement) {
      PsiElement attribute = hostElement;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)
             && !(attribute instanceof XmlTag)) {
        attribute = attribute.getParent();
      }
      if (attribute instanceof XmlAttribute) {
        AttributeInfo info = Angular2AttributeNameParser.parse(((XmlAttribute)attribute).getName(), true);
        if (info.type == Angular2AttributeType.EVENT) {
          scopes.add(new Angular2EventScope(((XmlAttribute)attribute)));
        }
      }
    }
    else if (element instanceof JSElement
             || element.getParent() instanceof JSElement) {
      PsiElement attribute = element;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)
             && !(attribute instanceof XmlTag)) {
        attribute = attribute.getParent();
      }
      if (attribute instanceof Angular2HtmlEvent) {
        scopes.add(new Angular2EventScope((Angular2HtmlEvent)attribute));
      }
    }
    return ContainerUtil.reverse(scopes);
  }

  @Nullable
  @NonNls
  public static JSType getHtmlElementClassType(@NotNull PsiElement context, @NotNull @NonNls String tagName) {
    JSTypeSource typeSource = JSTypeSourceFactory.createTypeSource(
      notNull(Angular2IndexingHandler.findComponentClass(context), context), true);
    return Optional
      .ofNullable(JSTypeUtils.createType(HTML_ELEMENT_TAG_NAME_MAP_CLASS_NAME, typeSource))
      .map(tagNameMap -> tagNameMap.asRecordType().findPropertySignature(tagName.toLowerCase()))
      .map(JSRecordType.PropertySignature::getJSType)
      .orElseGet(() -> JSTypeUtils.createType(HTML_ELEMENT_CLASS_NAME, typeSource));
  }

  private static JSImplicitElement createVariable(@NotNull String name,
                                                  @NotNull PsiElement contributor) {
    return new JSImplicitElementImpl.Builder(name, contributor)
      .setType(JSImplicitElement.Type.Variable).toImplicitElement();
  }

  private static class Angular2BaseScopeBuilder extends Angular2HtmlRecursiveElementVisitor {

    @NotNull
    private final PsiFile myTemplateFile;
    private final Stack<Angular2TemplateScope> scopes = new Stack<>();

    Angular2BaseScopeBuilder(@NotNull PsiFile templateFile) {
      myTemplateFile = templateFile;
      scopes.add(new Angular2TemplateScope(templateFile, null));
    }

    @NotNull
    public Angular2TemplateScope getTopLevelScope() {
      myTemplateFile.accept(this);
      assert scopes.size() == 1;
      return scopes.peek();
    }

    Angular2TemplateScope currentScope() {
      return scopes.peek();
    }

    void popScope() {
      scopes.pop();
    }

    void pushScope(@NotNull XmlTag tag) {
      scopes.push(new Angular2TemplateScope(tag, currentScope()));
    }

    void addElement(@NotNull JSPsiElementBase element) {
      currentScope().add(element);
    }

    @NotNull
    Angular2TemplateScope prevScope() {
      return scopes.get(scopes.size() - 2);
    }
  }

  private static class Angular2TemplateScopeBuilder extends Angular2BaseScopeBuilder {

    Angular2TemplateScopeBuilder(@NotNull PsiFile templateFile) {
      super(templateFile);
    }

    @Override
    public void visitXmlTag(XmlTag tag) {
      boolean isTemplateTag = Stream.of(tag.getChildren()).anyMatch(Angular2HtmlTemplateBindings.class::isInstance)
                              || isTemplateTag(tag.getName());
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
      JSVariable var = reference.getVariable();
      if (var != null) {
        if (isTemplateTag(reference.getParent().getName())) {
          // References on ng-template are visible within parent scope
          prevScope().add(var);
        }
        else {
          currentScope().add(var);
        }
      }
    }

    @Override
    public void visitVariable(Angular2HtmlVariable variable) {
      addElement(createVariable(variable.getVariableName(), variable));
    }

    @Override
    public void visitTemplateBindings(Angular2HtmlTemplateBindings bindings) {
      for (Angular2TemplateBinding b : bindings.getBindings().getBindings()) {
        if (b.keyIsVar() && b.getVariableDefinition() != null) {
          addElement(b.getVariableDefinition());
        }
      }
    }
  }

  private static class Angular2ForeignTemplateScopeBuilder extends Angular2BaseScopeBuilder {

    Angular2ForeignTemplateScopeBuilder(@NotNull PsiFile templateFile) {
      super(templateFile);
    }

    @Override
    public void visitXmlTag(XmlTag tag) {
      boolean isTemplateTag = StreamEx.of(tag.getChildren())
                                .select(XmlAttribute.class)
                                .anyMatch(attr -> attr.getName().startsWith("*"))
                              || isTemplateTag(tag.getName());
      if (isTemplateTag) {
        pushScope(tag);
      }
      super.visitXmlTag(tag);
      if (isTemplateTag) {
        popScope();
      }
    }

    @Override
    public void visitXmlAttribute(XmlAttribute attribute) {
      if (attribute.getParent() == null) {
        return;
      }
      boolean isTemplateTag = isTemplateTag(attribute.getParent().getName());
      AttributeInfo info = Angular2AttributeNameParser.parse(
        attribute.getName(), isTemplateTag);
      switch (info.type) {
        case REFERENCE:
          addReference(attribute, info, isTemplateTag);
          break;
        case VARIABLE:
          addVariable(attribute, info);
          break;
        case TEMPLATE_BINDINGS:
          addTemplateBindings(attribute);
          break;
        default:
      }
    }

    public void addReference(@NotNull XmlAttribute attribute, @NotNull AttributeInfo info, boolean isTemplateTag) {
      JSImplicitElement var = createVariable(info.name, attribute);
      if (isTemplateTag) {
        // References on ng-template are visible within parent scope
        prevScope().add(var);
      }
      else {
        currentScope().add(var);
      }
    }

    public void addVariable(@NotNull XmlAttribute attribute, @NotNull AttributeInfo info) {
      addElement(createVariable(info.name, attribute));
    }

    public void addTemplateBindings(@NotNull XmlAttribute attribute) {
      Angular2TemplateBindings bindings = Angular2InjectionUtils.findInjectedAngularExpression(attribute, Angular2TemplateBindings.class);
      if (bindings != null) {
        for (Angular2TemplateBinding b : bindings.getBindings()) {
          if (b.keyIsVar() && b.getVariableDefinition() != null) {
            addElement(b.getVariableDefinition());
          }
        }
      }
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

    @NotNull
    public List<Angular2Scope> getChildren() {
      return Collections.unmodifiableList(children);
    }

    private void add(Angular2Scope scope) {
      this.children.add(scope);
    }

    public final void consumeResultsFromAllScopes(@NotNull Consumer<? super ResolveResult> consumer) {
      Angular2Scope scope = this;
      while (scope != null) {
        scope.resolve(consumer);
        scope = scope.getParent();
      }
    }

    public abstract void resolve(@NotNull Consumer<? super ResolveResult> consumer);
  }

  private static class Angular2$AnyScope extends Angular2Scope {

    private final JSImplicitElement $any;

    private Angular2$AnyScope(@NotNull PsiElement context) {
      super(null);
      //noinspection HardCodedStringLiteral
      $any = new JSImplicitElementImpl.Builder($ANY, context)
        .setTypeString("*")
        .setParameters(Collections.singletonList(
          new JSImplicitParameterStructure("arg", "*", false, false, true)
        ))
        .setType(JSImplicitElement.Type.Function)
        .toImplicitElement();
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      consumer.accept(new JSResolveResult($any));
    }
  }

  private static class Angular2TemplateScope extends Angular2Scope {

    private final List<JSPsiElementBase> elements = new ArrayList<>();

    @NotNull private final TextRange myRange;

    private Angular2TemplateScope(@NotNull PsiElement root, @Nullable Angular2TemplateScope parent) {
      super(parent);
      myRange = root.getTextRange();
      if (parent != null) {
        assert parent.myRange.contains(myRange);
      }
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      elements.forEach(el -> consumer.accept(new JSResolveResult(el)));
    }

    public void add(@NotNull JSPsiElementBase element) {
      elements.add(element);
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

    private final XmlAttribute myEvent;

    private Angular2EventScope(@NotNull Angular2HtmlEvent event) {
      super(null);
      myEvent = event;
    }

    private Angular2EventScope(@NotNull XmlAttribute event) {
      super(null);
      myEvent = event;
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      consumer.accept(new JSResolveResult(new Angular2EventImplicitElement(myEvent)));
    }
  }

  private static class Angular2EventImplicitElement extends JSLocalImplicitElementImpl {
    @Nullable private final PsiElement myDeclaration;

    private Angular2EventImplicitElement(@NotNull XmlAttribute attribute) {
      super($EVENT, Angular2TypeEvaluator.resolveEventType(attribute), attribute, JSImplicitElement.Type.Variable);
      XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      myDeclaration = descriptor != null ? descriptor.getDeclaration() : null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Angular2EventImplicitElement element = (Angular2EventImplicitElement)o;
      if (!myName.equals(element.myName)) return false;
      if (!Objects.equals(myDeclaration, element.myDeclaration)) return false;
      if (!Objects.equals(myProvider, element.myProvider)) return false;
      if (myKind != element.myKind) return false;
      return true;
    }

    @Override
    public int hashCode() {
      return Objects.hash(getClass(), myDeclaration, myName, myProvider, myKind);
    }
  }

  private static class Angular2ComponentScope extends Angular2Scope {

    private final TypeScriptClass myClass;

    private Angular2ComponentScope(@NotNull TypeScriptClass aClass) {
      super(null);
      myClass = aClass;
    }

    @Override
    public void resolve(@NotNull Consumer<? super ResolveResult> consumer) {
      StreamEx.of(TypeScriptTypeParser
                    .buildTypeFromClass(myClass, false)
                    .getProperties())
        .mapToEntry(prop -> prop.getMemberSource().getAllSourceElements())
        .flatMapValues(Collection::stream)
        .selectValues(JSPsiElementBase.class)
        .filterValues(el -> !(el instanceof TypeScriptFunctionSignature))
        .filterValues(el -> !(el instanceof TypeScriptFunction) || !((TypeScriptFunction)el).isOverloadImplementation())
        .map(entry -> new Angular2ComponentPropertyResolveResult(entry.getValue(), entry.getKey()))
        .forEach(consumer);
    }
  }
}
