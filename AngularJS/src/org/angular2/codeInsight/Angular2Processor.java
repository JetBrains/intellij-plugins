// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser;
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider;
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.Stack;
import org.angular2.index.Angular2IndexingHandler;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.html.Angular2HtmlLanguage;
import org.angular2.lang.html.psi.*;
import org.angular2.lang.html.psi.impl.Angular2HtmlTemplateBindingsImpl;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
      if (attribute instanceof Angular2HtmlEvent) {
        scopes.add(new Angular2EventScope((Angular2HtmlEvent)attribute));
      }
    }
    return ContainerUtil.reverse(scopes);
  }

  @NotNull
  @NonNls
  public static String getHtmlElementClass(@NotNull Project project, @NotNull @NonNls String tagName) {
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
      TypeScriptInterface tsInterface = (TypeScriptInterface)el;
      if (tsInterface.getBody() == null) {
        continue;
      }
      for (TypeScriptTypeMember member : tsInterface.getBody().getTypeMembers()) {
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

  private static JSImplicitElement createVariable(@NotNull String name,
                                                  @NotNull PsiElement contributor) {
    return new JSImplicitElementImpl.Builder(name, contributor)
      .setType(JSImplicitElement.Type.Variable).toImplicitElement();
  }

  private static class Angular2TemplateScopeBuilder extends Angular2HtmlRecursiveElementVisitor {

    @NotNull
    private final PsiFile myTemplateFile;
    private final Stack<Angular2TemplateScope> scopes = new Stack<>();

    private Angular2TemplateScopeBuilder(@NotNull PsiFile templateFile) {
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

    private void addElement(@NotNull JSPsiElementBase element) {
      currentScope().add(element);
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
      JSVariable var = reference.getVariable();
      if (var != null) {
        if (reference.getParent().getName().equalsIgnoreCase(NG_TEMPLATE)) {
          // References on ng-template are visible within parent scope
          prevScope().add(var);
        }
        else {
          currentScope().add(var);
        }
      }
    }

    @NotNull
    private Angular2TemplateScope prevScope() {
      return scopes.get(scopes.size() - 2);
    }

    @Override
    public void visitVariable(Angular2HtmlVariable variable) {
      addElement(createVariable(variable.getVariableName(), variable));
    }

    @Override
    public void visitTemplateBindings(Angular2HtmlTemplateBindingsImpl bindings) {
      if (bindings.getBindings() != null) {
        for (Angular2TemplateBinding b : bindings.getBindings().getBindings()) {
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
    @NotNull
    public List<JSPsiElementBase> getElements() {
      return elements;
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

    private Angular2ComponentScope(JSClass jsClass) {
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
        .map(prop -> prop.getMemberSource().getAllSourceElements())
        .flatMap(Collection::stream)
        .filter(el -> el instanceof JSPsiElementBase)
        .map(el -> (JSPsiElementBase)el)
        .collect(Collectors.toList());
    }
  }
}
