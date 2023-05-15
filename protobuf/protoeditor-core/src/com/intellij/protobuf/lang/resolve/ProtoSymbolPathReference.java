/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A reference to a type or symbol elsewhere in the file or in another imported file.
 *
 * <p>In the PSI tree, a TypeName is an optionally dot-prefixed SymbolPath, where the dot prefix, if
 * specified, indicates that the reference name is fully-qualified. A SymbolPath is a dot-delimited
 * sequence of tokens that represent a fully- or partially-qualified name. As part of a TypeName,
 * the SymbolPath refers to a target type, an intermediate (parent) type, or a package name.
 *
 * <p>The PSI structure for the type name ".foo.bar.MyParentType.MyType" looks like:
 *
 * <pre>
 * {
 *   type: TypeName
 *   isFullyQualified: true
 *   symbolPath: {
 *     type: SymbolPath
 *     name: MyType
 *     qualifier: {
 *       type: SymbolPath
 *       name: MyParentType
 *       qualifier: {
 *         type: SymbolPath
 *         name: bar
 *         qualifier: {
 *           type: SymbolPath
 *           name: foo
 *           qualifier: null
 *         }
 *       }
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>SymbolPath is a recursively-nested structure, and each constituent can be resolved
 * individually. For example, ctrl-clicking on "MyType" should navigate to "message MyType", whereas
 * ctrl-clicking on "MyParentType" should navigate to "message MyParentType". As such, each
 * constituent SymbolPath provides its own PsiReference instance, and requires its own {@link
 * PsiReference#resolve()} invocation.
 *
 * <p>Type resolution happens using one of two possible strategies:
 *
 * <ul>
 *   <li>Top-down: enumerate a list of PbFile resources from the containing file and its imports;
 *       search each file for a top-level type or package matching "foo"; search those results for a
 *       type or package matching "bar"; and so on until the end of the type name is reached.
 *   <li>Bottom-up: start from a more-qualified SymbolPath, get its referenced elements, and find
 *       parents of those elements that match the target name.
 * </ul>
 *
 * <p>The bottom-up approach is intended to limit intermediate names in a TypeName to those that
 * actually contain the target type. So for example, if two files are imported that both provide the
 * package "com.foo", but only one file provides the type "com.foo.MyType", ctrl-clicking on "foo"
 * in the TypeName ".com.foo.MyType" resolves to only the file containing MyType.
 */
public class ProtoSymbolPathReference extends PsiPolyVariantReferenceBase<PsiElement> {

  @NotNull private final ProtoSymbolPath symbolPath;
  @NotNull private final PbSymbolResolver resolver;
  @Nullable private final QualifiedName scope;
  @NotNull private final Condition<PbSymbol> resolveFilter;
  @Nullable private final Condition<PbSymbol> completionFilter;
  @NotNull private final Function<PbSymbol, LookupElement> lookupElementFactory;

  public ProtoSymbolPathReference(
      @NotNull ProtoSymbolPath element,
      @NotNull PbSymbolResolver resolver,
      @Nullable QualifiedName scope,
      @NotNull Condition<PbSymbol> resolveFilter,
      @Nullable Condition<PbSymbol> completionFilter) {
    this(element, resolver, scope, resolveFilter, completionFilter, PbSymbolLookupElement::new);
  }

  public ProtoSymbolPathReference(
      @NotNull ProtoSymbolPath element,
      @NotNull PbSymbolResolver resolver,
      @Nullable QualifiedName scope,
      @NotNull Condition<PbSymbol> resolveFilter,
      @Nullable Condition<PbSymbol> completionFilter,
      @NotNull Function<PbSymbol, LookupElement> lookupElementFactory) {
    this(
        element,
        calculateRangeInPath(element),
        element,
        resolver,
        scope,
        resolveFilter,
        completionFilter,
        lookupElementFactory);
  }

  public ProtoSymbolPathReference(
      @NotNull PsiElement element,
      @NotNull TextRange rangeInElement,
      @NotNull ProtoSymbolPath symbolPath,
      @NotNull PbSymbolResolver resolver,
      @Nullable QualifiedName scope,
      @NotNull Condition<PbSymbol> resolveFilter,
      @Nullable Condition<PbSymbol> completionFilter,
      @NotNull Function<PbSymbol, LookupElement> lookupElementFactory) {
    super(element);
    this.symbolPath = symbolPath;
    this.resolver = resolver;
    this.scope = scope;
    this.resolveFilter = resolveFilter;
    this.completionFilter = completionFilter;
    this.lookupElementFactory = lookupElementFactory;
    setRangeInElement(rangeInElement);
  }

  private static TextRange calculateRangeInPath(ProtoSymbolPath path) {
    TextRange outerRange = path.getTextRange();
    TextRange identifierRange = path.getSymbol().getTextRange();
    return TextRange.from(
        identifierRange.getStartOffset() - outerRange.getStartOffset(),
        identifierRange.getLength());
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    ResolveCache cache = ResolveCache.getInstance(myElement.getProject());
    return cache.resolveWithCaching(
        this,
        (ref, incompleteCode1) -> ref.multiResolveNoCache(),
        /* needToPreventRecursion= */ false,
        incompleteCode);
  }

  private ResolveResult @NotNull [] multiResolveNoCache() {
    QualifiedName name = symbolPath.getQualifiedName();
    PsiElement parent = symbolPath.getParent();

    ResolveResult[] results = resolveFromMoreQualifiedParent(parent, name);
    if (results != null) {
      return results;
    } else {
      return resolveTopDown(name);
    }
  }

  private static ResolveResult[] resolveFromMoreQualifiedParent(
      PsiElement parentPath, QualifiedName name) {

    // Not a symbol path parent; fall back into top-down
    if (!(parentPath instanceof ProtoSymbolPath)) {
      return null;
    }

    // A parent of a child SymbolPath that provides a ProtoSymbolPathReference should also provide
    // a ProtoSymbolPathReference.
    PsiReference ref = parentPath.getReference();
    if (!(ref instanceof ProtoSymbolPathReference pathReference)) {
      return null;
    }

    ResolveResult[] resolveResults = pathReference.multiResolve(false);
    if (resolveResults.length == 0) {
      return null;
    }

    List<ResolveResult> results = new ArrayList<>(resolveResults.length);

    for (ResolveResult result : pathReference.multiResolve(false)) {
      // Results should always be PbResolveResult instances.
      if (!(result instanceof PbResolveResult pbResult)) {
        continue;
      }

      // Start with the more qualified element's name.
      QualifiedName refElementScope = pbResult.getName();
      if (refElementScope == null) {
        continue;
      }

      // Pop off the last component of the name to create our fully-qualified target name.
      // Sanity check to make sure the fully-qualified target matches our possibly unqualified name.
      QualifiedName nextName = refElementScope.removeLastComponent();
      if (!nextName
          .subQualifiedName(
              nextName.getComponentCount() - name.getComponentCount(), nextName.getComponentCount())
          .equals(name)) {
        continue;
      }

      PbSymbol symbol = pbResult.getElement();
      if (symbol == null) {
        continue;
      }
      PbSymbolOwner nextOwner = symbol.getSymbolOwner();
      // Walk up the tree looking for the element that provides the target name.
      // Some types, such as oneofs, have names but don't create their own scopes. Such elements
      // will be skipped.
      while (nextOwner != null) {
        PbResolveResult newResult = resolveForElement(nextOwner, nextName);
        if (newResult != null) {
          results.add(newResult);
          break;
        }
        nextOwner = nextOwner instanceof PbSymbol ? ((PbSymbol) nextOwner).getSymbolOwner() : null;
      }
    }

    return results.toArray(ResolveResult.EMPTY_ARRAY);
  }

  private ResolveResult[] resolveTopDown(QualifiedName name) {
    List<PbResolveResult> results;
    if (scope != null) {
      results = resolver.resolveRelativeName(name, scope, resolveFilter);
    } else {
      results = resolver.resolveName(name, resolveFilter);
    }
    return results.toArray(ResolveResult.EMPTY_ARRAY);
  }

  @Override
  public Object @NotNull [] getVariants() {
    if (completionFilter == null) {
      return LookupElement.EMPTY_ARRAY;
    }

    // If we know for certain the parent scope from the qualifier, just grab its children.
    ProtoSymbolPath qualifier = symbolPath.getQualifier();
    if (qualifier != null) {
      return PbPsiUtil.multiResolveRefToType(qualifier.getReference(), PbSymbolOwner.class)
          .stream()
          .map(PbSymbolOwner::getSymbols)
          .flatMap(Collection::stream)
          .filter(completionFilter::value)
          .map(lookupElementFactory)
          .distinct()
          .toArray();
    }

    Collection<PbSymbol> results;
    if (scope != null) {
      // Resolve all relative names available within the current scope. Starting with an empty
      // package name (all top-level symbols), we iterate down the scope's qualified name, resolving
      // children at each level. Children at deeper levels in the tree take precedence, replacing
      // and children with the same name found at previous levels.
      Multimap<String, PbSymbol> allSymbols = LinkedListMultimap.create();
      for (int i = 0; i <= scope.getComponentCount(); i++) {
        QualifiedName subScope = scope.subQualifiedName(0, i);
        Multimap<String, PbSymbol> childSymbols = resolver.findChildren(subScope, completionFilter);
        for (Map.Entry<String, Collection<PbSymbol>> entry : childSymbols.asMap().entrySet()) {
          allSymbols.replaceValues(entry.getKey(), entry.getValue());
        }
      }
      results = allSymbols.values();
    } else {
      // Find children of the empty package, which includes all top-level symbols visible to the
      // file.
      Multimap<String, PbSymbol> topLevelSymbols =
          resolver.findChildren(PbPsiUtil.EMPTY_QUALIFIED_NAME, completionFilter);
      results = topLevelSymbols.values();
    }
    return results.stream().map(lookupElementFactory).distinct().toArray();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    ASTNode node = symbolPath.getNode().findChildByType(ProtoTokenTypes.IDENTIFIER_LITERAL);
    if (node instanceof LeafElement) {
      ((LeafElement) node).replaceWithText(newElementName);
      return symbolPath;
    }
    return super.handleElementRename(newElementName);
  }

  /**
   * Returns a {@link PbResolveResult} for the given element iff it is a symbol matching the given
   * qualified name.
   *
   * @param element The candidate element.
   * @param name The target qualified name.
   * @return a {@link PbResolveResult} if the element matches the name, else <code>null</code>.
   */
  @Nullable
  private static PbResolveResult resolveForElement(PbElement element, QualifiedName name) {
    if (name == null) {
      return null;
    }
    if (element instanceof PbSymbol symbol) {
      if (name.equals(symbol.getQualifiedName())) {
        return PbResolveResult.create(symbol);
      }
    }
    return null;
  }
}
