// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.library.JSCorePredefinedLibrariesProvider;
import com.intellij.lang.javascript.psi.JSDefinitionExpression;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecma6.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.typescript.library.TypeScriptLibraryProvider;
import com.intellij.lang.typescript.resolve.TypeScriptClassResolver;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.html.HtmlTag;
import com.intellij.psi.impl.source.html.HtmlEmbeddedContentImpl;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.Consumer;
import com.intellij.xml.util.documentation.HtmlDescriptorsTable;
import org.angular2.lang.expr.psi.Angular2RecursiveVisitor;
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor;
import org.angular2.lang.html.psi.Angular2HtmlEvent;
import org.angular2.lang.html.psi.Angular2HtmlReference;
import org.angular2.lang.html.psi.Angular2HtmlVariable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dennis.Ushakov
 */
public class Angular2Processor {
  private static volatile Map<String, String> TAG_TO_CLASS;

  public static final String $EVENT = "$event";

  public static void process(final PsiElement element, final Consumer<JSPsiElementBase> consumer) {
    final PsiElement original = CompletionUtil.getOriginalOrSelf(element);
    PsiFile hostFile = FileContextUtil.getContextFile(original != element ? original : element.getContainingFile().getOriginalFile());
    if (!(hostFile instanceof XmlFile)) {
      hostFile = original.getContainingFile();
    }
    if (!(hostFile instanceof XmlFile)) return;

    final XmlFile file = (XmlFile)hostFile;

    final Collection<JSPsiElementBase> cache = CachedValuesManager.getCachedValue(file, () -> {
      final Collection<JSPsiElementBase> result = new ArrayList<>();
      processDocument(file.getDocument(), result);

      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
    for (JSPsiElementBase namedElement : cache) {
      if (scopeMatches(original, namedElement)){
        consumer.consume(namedElement);
      }
    }
  }

  private static void processDocument(XmlDocument document, final Collection<JSPsiElementBase> result) {
    if (document == null) return;
    final AngularInjectedFilesVisitor visitor = new AngularInjectedFilesVisitor(result);

    for (XmlTag tag : PsiTreeUtil.getChildrenOfTypeAsList(document, XmlTag.class)) {
      new XmlBackedJSClassImpl.InjectedScriptsVisitor(tag, null, true, true, visitor, true){
        @Override
        public boolean execute(@NotNull PsiElement element) {
          if (element instanceof HtmlEmbeddedContentImpl) {
            processDocument(PsiTreeUtil.findChildOfType(element, XmlDocument.class), result);
          }
          if (element instanceof XmlAttribute) {
            visitor.accept(element);
          }
          return super.execute(element);
        }
      }.go();
    }
  }

  private static boolean scopeMatches(PsiElement element, PsiElement declaration) {
    final InjectedLanguageManager injector = InjectedLanguageManager.getInstance(element.getProject());
    if (declaration instanceof JSImplicitElement) {
      if ($EVENT.equals(((JSImplicitElement)declaration).getName())) {
        return eventScopeMatches(injector, element, declaration.getParent());
      }
      if (declaration.getParent() instanceof Angular2HtmlReference) {
        return true;
      }
      declaration = declaration.getParent();
    }
    final PsiLanguageInjectionHost elementContainer = injector.getInjectionHost(element);
    final XmlTagChild elementTag = PsiTreeUtil.getNonStrictParentOfType(element, XmlTag.class, XmlText.class);
    final PsiLanguageInjectionHost declarationContainer = injector.getInjectionHost(declaration);
    final XmlTagChild declarationTag = PsiTreeUtil.getNonStrictParentOfType(declaration, XmlTag.class);

    if (declarationContainer != null && elementContainer != null && elementTag != null && declarationTag != null) {
      return PsiTreeUtil.isAncestor(declarationTag, elementTag, true) ||
             PsiTreeUtil.isAncestor(declarationTag, elementTag, false) ||
             PsiTreeUtil.isAncestor(declarationTag, elementTag, false) &&
             declarationContainer.getTextOffset() < elementContainer.getTextOffset() ||
             isInRepeatStartEnd(declarationTag, declarationContainer, elementContainer);
    }
    return true;
  }

  private static boolean isInRepeatStartEnd(XmlTagChild declarationTag,
                                            PsiLanguageInjectionHost declarationContainer,
                                            PsiLanguageInjectionHost elementContainer) {
    PsiElement parent = declarationContainer.getParent();
    if (parent instanceof XmlAttribute && "ng-repeat-start".equals(((XmlAttribute)parent).getName())) {
      XmlTagChild next = declarationTag.getNextSiblingInTag();
      while (next != null) {
        if (PsiTreeUtil.isAncestor(next, elementContainer, true)) return true;
        if (next instanceof XmlTag && ((XmlTag)next).getAttribute("ng-repeat-end") != null) break;
        next = next.getNextSiblingInTag();
      }
    }
    return false;
  }

  private static boolean eventScopeMatches(InjectedLanguageManager injector, PsiElement element, PsiElement parent) {
    XmlAttribute attribute = PsiTreeUtil.getNonStrictParentOfType(element, XmlAttribute.class);
    if (attribute == null) {
      final PsiLanguageInjectionHost elementContainer = injector.getInjectionHost(element);
      attribute = PsiTreeUtil.getNonStrictParentOfType(elementContainer, XmlAttribute.class);
    }
    return attribute != null && CompletionUtil.getOriginalOrSelf(attribute) == CompletionUtil.getOriginalOrSelf(parent);
  }

  public static JSImplicitElementImpl.Builder createTagReference(HtmlTag tag, Angular2HtmlReference reference) {
    final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(reference.getReferenceName(), reference)
      .setType(JSImplicitElement.Type.Variable);

    final String tagName = tag.getName();
    if (HtmlDescriptorsTable.getTagDescriptor(tagName) != null) {
      elementBuilder.setTypeString(getHtmlElementClass(tag.getProject(), tagName));
    }
    return elementBuilder;
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

  private static class AngularInjectedFilesVisitor extends JSResolveUtil.JSInjectedFilesVisitor {
    private final Collection<JSPsiElementBase> myResult;

    public AngularInjectedFilesVisitor(Collection<JSPsiElementBase> result) {
      myResult = result;
    }

    @Override
    protected void process(JSFile file) {
      accept(file);
    }

    protected void accept(PsiElement element) {

      element.accept(new Angular2RecursiveVisitor() {
        @Override
        public void visitJSDefinitionExpression(JSDefinitionExpression node) {
          myResult.add(node);
          super.visitJSDefinitionExpression(node);
        }

        @Override
        public void visitJSVariable(JSVariable node) {
          myResult.add(node);
          super.visitJSVariable(node);
        }
      });
      element.accept(new Angular2HtmlElementVisitor() {

        @Override
        public void visitReference(Angular2HtmlReference reference) {
          final JSImplicitElementImpl.Builder builder = createTagReference(
            (HtmlTag)reference.getParent(), reference);
          myResult.add(builder.toImplicitElement());
        }

        @Override
        public void visitVariable(Angular2HtmlVariable variable) {
          final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(variable.getVariableName(), variable).
            setType(JSImplicitElement.Type.Variable);
          myResult.add(builder.toImplicitElement());
        }

        @Override
        public void visitEvent(Angular2HtmlEvent event) {
          final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder($EVENT, event).
            setType(JSImplicitElement.Type.Variable);
          myResult.add(builder.toImplicitElement());
        }

      });
    }
  }
}
