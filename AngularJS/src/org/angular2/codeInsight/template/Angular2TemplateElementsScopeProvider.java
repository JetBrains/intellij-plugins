// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.template;

import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
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
import one.util.streamex.StreamEx;
import org.angular2.Angular2InjectionUtils;
import org.angular2.lang.expr.psi.Angular2TemplateBinding;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.intellij.util.ObjectUtils.notNull;
import static org.angular2.web.Angular2WebSymbolsRegistryExtension.ELEMENT_NG_TEMPLATE;

public class Angular2TemplateElementsScopeProvider extends Angular2TemplateScopesProvider {

  @NonNls private static final String LEGACY_TEMPLATE_TAG = "template";

  public static boolean isTemplateTag(@Nullable XmlTag tag) {
    return tag != null && isTemplateTag(tag.getLocalName());
  }

  public static boolean isTemplateTag(@Nullable String tagName) {
    return ELEMENT_NG_TEMPLATE.equalsIgnoreCase(tagName)
           || LEGACY_TEMPLATE_TAG.equalsIgnoreCase(tagName);
  }

  @Override
  public @NotNull List<? extends Angular2TemplateScope> getScopes(@NotNull PsiElement element, @Nullable PsiElement hostElement) {
    final PsiFile hostFile = CompletionUtil.getOriginalOrSelf(notNull(hostElement, element)).getContainingFile();

    boolean isInjected = hostElement != null;
    final Angular2TemplateElementScope templateRootScope = CachedValuesManager.getCachedValue(hostFile, () -> {
      final Angular2TemplateElementScope result;
      if (!isInjected) {
        result = new Angular2TemplateScopeBuilder(hostFile).getTopLevelScope();
      }
      else {
        result = new Angular2ForeignTemplateScopeBuilder(hostFile).getTopLevelScope();
      }
      return CachedValueProvider.Result.create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
    return Collections.singletonList(templateRootScope.findBestMatchingTemplateScope(notNull(hostElement, element)));
  }

  private static final class Angular2TemplateElementScope extends Angular2TemplateScope {

    private final List<JSPsiElementBase> elements = new ArrayList<>();

    private final @NotNull TextRange myRange;

    private Angular2TemplateElementScope(@NotNull PsiElement root, @Nullable Angular2TemplateElementScope parent) {
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

    public @Nullable Angular2TemplateElementScope findBestMatchingTemplateScope(@NotNull PsiElement element) {
      if (!myRange.contains(element.getTextOffset())) {
        return null;
      }
      Angular2TemplateElementScope curScope = null;
      Angular2TemplateElementScope innerScope = this;
      while (innerScope != null) {
        curScope = innerScope;
        innerScope = null;
        for (Angular2TemplateScope child : curScope.getChildren()) {
          if (child instanceof Angular2TemplateElementScope
              && ((Angular2TemplateElementScope)child).myRange.contains(element.getTextOffset())) {
            innerScope = (Angular2TemplateElementScope)child;
            break;
          }
        }
      }
      if (PsiTreeUtil.getParentOfType(element, Angular2HtmlTemplateBindings.class) != null
          && curScope != this) {
        curScope = (Angular2TemplateElementScope)curScope.getParent();
      }
      return curScope;
    }
  }

  private static JSImplicitElement createVariable(@NotNull String name,
                                                  @NotNull PsiElement contributor) {
    return new JSImplicitElementImpl.Builder(name, contributor)
      .setType(JSImplicitElement.Type.Variable).toImplicitElement();
  }

  private static class Angular2BaseScopeBuilder extends Angular2HtmlRecursiveElementVisitor {

    private final @NotNull PsiFile myTemplateFile;
    private final Stack<Angular2TemplateElementScope> scopes = new Stack<>();

    Angular2BaseScopeBuilder(@NotNull PsiFile templateFile) {
      myTemplateFile = templateFile;
      scopes.add(new Angular2TemplateElementScope(templateFile, null));
    }

    public @NotNull Angular2TemplateElementScope getTopLevelScope() {
      myTemplateFile.accept(this);
      assert scopes.size() == 1;
      return scopes.peek();
    }

    Angular2TemplateElementScope currentScope() {
      return scopes.peek();
    }

    void popScope() {
      scopes.pop();
    }

    void pushScope(@NotNull XmlTag tag) {
      scopes.push(new Angular2TemplateElementScope(tag, currentScope()));
    }

    void addElement(@NotNull JSPsiElementBase element) {
      currentScope().add(element);
    }

    @NotNull
    Angular2TemplateElementScope prevScope() {
      return scopes.get(scopes.size() - 2);
    }
  }

  private static class Angular2TemplateScopeBuilder extends Angular2BaseScopeBuilder {

    Angular2TemplateScopeBuilder(@NotNull PsiFile templateFile) {
      super(templateFile);
    }

    @Override
    public void visitXmlTag(@NotNull XmlTag tag) {
      boolean isTemplateTag = ContainerUtil.or(tag.getChildren(), Angular2HtmlTemplateBindings.class::isInstance)
                              || isTemplateTag(tag);
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
        if (isTemplateTag(reference.getParent())) {
          // References on ng-template are visible within parent scope
          prevScope().add(var);
        }
        else {
          currentScope().add(var);
        }
      }
    }

    @Override
    public void visitLet(Angular2HtmlLet let) {
      JSVariable var = let.getVariable();
      if (var != null) {
        addElement(var);
      }
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
    public void visitXmlTag(@NotNull XmlTag tag) {
      boolean isTemplateTag = StreamEx.of(tag.getChildren())
                                .select(XmlAttribute.class)
                                .anyMatch(attr -> attr.getName().startsWith("*"))
                              || isTemplateTag(tag);
      if (isTemplateTag) {
        pushScope(tag);
      }
      super.visitXmlTag(tag);
      if (isTemplateTag) {
        popScope();
      }
    }

    @Override
    public void visitXmlAttribute(@NotNull XmlAttribute attribute) {
      if (attribute.getParent() == null) {
        return;
      }
      Angular2AttributeNameParser.AttributeInfo info = Angular2AttributeNameParser.parse(
        attribute.getName(), attribute.getParent());
      switch (info.type) {
        case REFERENCE -> addReference(attribute, info, isTemplateTag(attribute.getParent()));
        case LET -> addVariable(attribute, info);
        case TEMPLATE_BINDINGS -> addTemplateBindings(attribute);
        default -> {}
      }
    }

    public void addReference(@NotNull XmlAttribute attribute,
                             @NotNull Angular2AttributeNameParser.AttributeInfo info,
                             boolean isTemplateTag) {
      JSImplicitElement var = createVariable(info.name, attribute);
      if (isTemplateTag) {
        // References on ng-template are visible within parent scope
        prevScope().add(var);
      }
      else {
        currentScope().add(var);
      }
    }

    public void addVariable(@NotNull XmlAttribute attribute, @NotNull Angular2AttributeNameParser.AttributeInfo info) {
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
}
