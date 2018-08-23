// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSSourceElement;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider;
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Consumer;
import com.intellij.util.containers.Stack;
import com.intellij.xml.util.documentation.HtmlDescriptorsTable;
import org.angular2.lang.expr.Angular2Language;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
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
    if (!original.getLanguage().is(Angular2Language.INSTANCE)) {
      return;
    }
    final XmlFile file = (XmlFile)original.getContainingFile();

    final Angular2TemplateScope templateRootScope = CachedValuesManager.getCachedValue(file, () -> {
      final Angular2TemplateScope result = new ScopeBuilder(file).getTopLevelScope();
      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });

    calculateElementScopes(element, templateRootScope)
      .forEach(s -> s.consumeElementsFromAllScopes(consumer));
  }

  private static List<Angular2Scope> calculateElementScopes(PsiElement element, Angular2TemplateScope rootTemplateScope) {
    List<Angular2Scope> scopes = new ArrayList<>();
    scopes.add(Objects.requireNonNull(rootTemplateScope.findBestMatchingTemplateScope(element)));

    if (element instanceof JSSourceElement) {
      PsiElement attribute = element;
      while (attribute != null
             && !(attribute instanceof XmlAttribute)) {
        attribute = attribute.getParent();
      }
      if (attribute instanceof Angular2HtmlEvent) {
        scopes.add(new Angular2EventScope((Angular2HtmlEvent)attribute));
      }
    }
    return scopes;
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

  private static class ScopeBuilder extends Angular2HtmlRecursiveVisitor {

    @NotNull
    private final PsiFile myTemplateFile;
    private final Stack<Angular2TemplateScope> scopes = new Stack<>();

    public ScopeBuilder(@NotNull PsiFile templateFile) {
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

    private void addElement(JSImplicitElementImpl element) {
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
    public void visitReference(Angular2HtmlReference reference) {
      addElement(createReference(reference).toImplicitElement());
    }

    @Override
    public void visitVariable(Angular2HtmlVariable variable) {
      addElement(createVariable(variable.getVariableName(), variable).toImplicitElement());
    }

    @Override
    public void visitTemplateBindings(Angular2HtmlTemplateBindingsImpl bindings) {
      Angular2TemplateBindings bindingsExpr = bindings.getBindings();
      if (bindingsExpr != null) {
        for (Angular2TemplateBinding binding : bindingsExpr.getBindings()) {
          if (binding.keyIsVar()) {
            addElement(createVariable(binding.getKey(), binding).toImplicitElement());
          }
        }
      }
    }

    private static JSImplicitElementImpl.Builder createVariable(@NotNull String name, @NotNull PsiElement contributor) {
      return new JSImplicitElementImpl.Builder(name, contributor)
        .setType(JSImplicitElement.Type.Variable);
    }

    private static JSImplicitElementImpl.Builder createReference(@NotNull Angular2HtmlReference reference) {
      final HtmlTag tag = (HtmlTag)reference.getParent();
      final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(reference.getReferenceName(), reference)
        .setType(JSImplicitElement.Type.Variable);

      final String tagName = tag.getName();
      if (HtmlDescriptorsTable.getTagDescriptor(tagName) != null
          && reference.getValueElement() == null) {
        elementBuilder.setTypeString(getHtmlElementClass(tag.getProject(), tagName));
      }
      return elementBuilder;
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

    @NotNull private final PsiElement myRoot;

    public Angular2TemplateScope(@NotNull PsiElement root, @Nullable Angular2TemplateScope parent) {
      super(parent);
      myRoot = root;
      if (parent != null) {
        assert parent.myRoot.getTextRange().contains(myRoot.getTextRange());
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
      if (!myRoot.getTextRange().contains(element.getTextRange())) {
        return null;
      }
      Angular2TemplateScope curScope = null;
      Angular2TemplateScope innerScope = this;
      while (innerScope != null) {
        curScope = innerScope;
        innerScope = null;
        for (Angular2Scope child : curScope.getChildren()) {
          if (child instanceof Angular2TemplateScope
              && ((Angular2TemplateScope)child).myRoot.getTextRange().contains(element.getTextRange())) {
            innerScope = (Angular2TemplateScope)child;
            break;
          }
        }
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
}
