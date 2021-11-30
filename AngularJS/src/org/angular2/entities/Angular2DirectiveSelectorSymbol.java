// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.UsageHandler;
import com.intellij.javascript.web.codeInsight.html.WebSymbolsHtmlAdditionalContextProvider;
import com.intellij.javascript.web.symbols.WebSymbol;
import com.intellij.lang.documentation.DocumentationTarget;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.model.Pointer;
import com.intellij.navigation.NavigationRequest;
import com.intellij.navigation.NavigationTarget;
import com.intellij.navigation.TargetPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.api.RenameTarget;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlElementDescriptor;
import org.angular2.entities.impl.TypeScriptElementDocumentationTarget;
import org.angular2.web.Angular2Symbol;
import org.angular2.web.Angular2SymbolOrigin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.intellij.javascript.web.codeInsight.html.WebSymbolsHtmlAdditionalContextProvider.getHtmlNSDescriptor;
import static com.intellij.javascript.web.symbols.WebSymbolsUtils.createPsiRangeNavigationItem;
import static org.angular2.Angular2DecoratorUtil.getClassForDecoratorElement;
import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS;
import static org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.KIND_NG_DIRECTIVE_ELEMENT_SELECTORS;

public class Angular2DirectiveSelectorSymbol implements Angular2Symbol, SearchTarget, RenameTarget {

  private static final Pattern TAG_NAME_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*");
  private static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("[^\\p{Space}\"'>/=\\p{Cntrl}]+");

  private final Angular2DirectiveSelectorImpl myParent;
  private final TextRange myRange;
  private final String myName;
  private final String myElementSelector;
  private final boolean myIsElement;

  public Angular2DirectiveSelectorSymbol(@NotNull Angular2DirectiveSelectorImpl parent,
                                         @NotNull TextRange range,
                                         @NotNull String name,
                                         @Nullable String elementSelector,
                                         boolean isElement) {
    myParent = parent;
    myRange = range;
    myName = name;
    myElementSelector = elementSelector;
    myIsElement = isElement;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Nullable
  @Override
  public Priority getPriority() {
    return Priority.LOWEST;
  }

  @NotNull
  @Override
  public DocumentationTarget getDocumentationTarget() {
    var clazz = PsiTreeUtil.getContextOfType(getSource(), TypeScriptClass.class);
    if (clazz == null) {
      return Angular2Symbol.super.getDocumentationTarget();
    }
    return new TypeScriptElementDocumentationTarget(getName(), clazz);
  }

  @Nullable
  @Override
  public PsiElement getPsiContext() {
    return myParent.getPsiParent();
  }

  public @NotNull PsiElement getSource() {
    return myParent.getPsiParent();
  }

  @Override
  public @NotNull Project getProject() {
    return getSource().getProject();
  }

  @NotNull
  @Override
  public Namespace getNamespace() {
    return Namespace.JS;
  }

  @NotNull
  @Override
  public String getKind() {
    return myIsElement ? KIND_NG_DIRECTIVE_ELEMENT_SELECTORS : KIND_NG_DIRECTIVE_ATTRIBUTE_SELECTORS;
  }

  @NotNull
  @Override
  public Pointer<Angular2DirectiveSelectorSymbol> createPointer() {
    var parent = myParent.createPointer();
    var range = myRange;
    var name = myName;
    var elementName = myElementSelector;
    var isElement = myIsElement;
    return () -> {
      var newParent = parent.dereference();
      return newParent != null ? new Angular2DirectiveSelectorSymbol(newParent, range, name, elementName, isElement) : null;
    };
  }

  @NotNull
  @Override
  public Origin getOrigin() {
    return new Angular2SymbolOrigin(this);
  }

  public boolean isDeclaration() {
    return !ContainerUtil.exists(getReferencedSymbols(), symbol -> !(symbol instanceof Angular2Symbol));
  }

  public List<WebSymbol> getReferencedSymbols() {
    var psiElement = getSource();
    var nsDescriptor = getHtmlNSDescriptor(psiElement.getProject());
    if (nsDescriptor != null) {
      if (isElementSelector()) {
        var elementDescriptor = nsDescriptor.getElementDescriptorByName(getName());
        if (elementDescriptor != null) {
          return Collections.singletonList(
            new WebSymbolsHtmlAdditionalContextProvider.HtmlElementDescriptorBasedSymbol(elementDescriptor, null));
        }
      }
      else {
        XmlElementDescriptor elementDescriptor = null;
        String tagName = myElementSelector;
        if (myElementSelector != null) {
          elementDescriptor = nsDescriptor.getElementDescriptorByName(myElementSelector);
        }
        if (elementDescriptor == null) {
          elementDescriptor = nsDescriptor.getElementDescriptorByName("div");
          tagName = "div";
        }
        if (elementDescriptor != null) {
          var attributeDescriptor = elementDescriptor.getAttributeDescriptor(getName(), null);
          if (attributeDescriptor != null) {
            return Collections.singletonList(
              new WebSymbolsHtmlAdditionalContextProvider.HtmlAttributeDescriptorBasedSymbol(attributeDescriptor, tagName));
          }
        }
      }
    }
    return Collections.singletonList(this);
  }

  public int getTextOffset() {
    return myParent.getPsiParent().getTextOffset() + myRange.getStartOffset();
  }

  public @NotNull TextRange getTextRangeInSource() {
    return myRange;
  }

  public boolean isElementSelector() {
    return myIsElement;
  }

  public boolean isAttributeSelector() {
    return !myIsElement;
  }

  @Override
  public String toString() {
    return (myIsElement ? "ElementDirectiveSelector" : "AttributeDirectiveSelector") + "<" + myName + ">";
  }

  @Override
  public @NotNull Collection<NavigationTarget> getNavigationTargets(@NotNull Project project) {
    return Collections.singletonList(new DirectiveSelectorSymbolNavigationTarget(this));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2DirectiveSelectorSymbol symbol = (Angular2DirectiveSelectorSymbol)o;
    return myIsElement == symbol.myIsElement &&
           myParent.equals(symbol.myParent) &&
           myRange.equals(symbol.myRange) &&
           myName.equals(symbol.myName) &&
           Objects.equals(myElementSelector, symbol.myElementSelector);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myParent, myRange, myName, myElementSelector, myIsElement);
  }

  @Override
  public @NotNull TargetPresentation getPresentation() {
    PsiElement parent = myParent.getPsiParent();
    TypeScriptClass clazz = getClassForDecoratorElement(parent);
    return TargetPresentation.builder(getName())
      .icon(getIcon())
      .locationText(parent.getContainingFile().getName())
      .containerText(clazz != null ? clazz.getName() : null)
      .presentation();
  }

  @NotNull
  @Override
  public UsageHandler<?> getUsageHandler() {
    return UsageHandler.createEmptyUsageHandler(getName());
  }

  @NotNull
  @Override
  public String getTargetName() {
    return getName();
  }

  @Nullable
  @Override
  public SearchScope getMaximalSearchScope() {
    return SearchTarget.super.getMaximalSearchScope();
  }

  @Nullable
  @Override
  public String validateName(@NotNull String name) {
    if (myIsElement) {
      return TAG_NAME_PATTERN.matcher(name).matches() ? null : name +" is not a valid HTML element name.";
    }
    return ATTRIBUTE_NAME_PATTERN.matcher(name).matches() ? null : name + " is not a valid HTML attribute name.";
  }

  private static class DirectiveSelectorSymbolNavigationTarget implements NavigationTarget {

    private final Angular2DirectiveSelectorSymbol mySymbol;

    private DirectiveSelectorSymbolNavigationTarget(Angular2DirectiveSelectorSymbol symbol) {
      mySymbol = symbol;
    }

    @Override
    public final @NotNull Pointer<? extends NavigationTarget> createPointer() {
      return Pointer.delegatingPointer(
        mySymbol.createPointer(),
        DirectiveSelectorSymbolNavigationTarget.class,
        DirectiveSelectorSymbolNavigationTarget::new
      );
    }

    @Override
    public @NotNull Navigatable getNavigatable() {
      return createPsiRangeNavigationItem(this, mySymbol.getSource(), mySymbol.myRange.getStartOffset());
    }

    @Override
    public @NotNull TargetPresentation getTargetPresentation() {
      return mySymbol.getPresentation();
    }

    @Override
    public @Nullable NavigationRequest navigationRequest() {
      return getNavigatable().navigationRequest();
    }
  }
}
