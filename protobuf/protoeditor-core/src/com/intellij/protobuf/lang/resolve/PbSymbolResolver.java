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

import com.google.common.collect.*;
import com.intellij.openapi.util.Condition;
import com.intellij.protobuf.lang.psi.PbFile;
import com.intellij.protobuf.lang.psi.PbSymbol;
import com.intellij.protobuf.lang.psi.PbSymbolOwner;
import com.intellij.psi.util.QualifiedName;

import java.util.*;
import java.util.stream.Collectors;

/** Utilities for finding PbSymbol elements using protobuf's scoping and resolution rules. */
public class PbSymbolResolver {

  private final Multimap<QualifiedName, PbSymbol> symbols;

  private PbSymbolResolver(Multimap<QualifiedName, PbSymbol> symbols) {
    this.symbols = symbols;
  }

  /** Returns a PbSymbolResolver that can resolve symbols in the given file and its imports. */
  public static PbSymbolResolver forFile(PbFile file) {
    return new PbSymbolResolver(convertJdkMapToGuava(file.getExportedQualifiedSymbolMap()));
  }

  /** Returns a PbSymbolResolver that can resolve symbols exported by the given file. */
  public static PbSymbolResolver forFileExports(PbFile file) {
    return new PbSymbolResolver(convertJdkMapToGuava(file.getExportedQualifiedSymbolMap()));
  }

  /** Returns a PbSymbolResolver that can resolve symbols exported by the given files. */
  public static PbSymbolResolver forFileExports(List<PbFile> files) {
    ImmutableSetMultimap.Builder<QualifiedName, PbSymbol> builder = ImmutableSetMultimap.builder();
    for (PbFile file : files) {
      Multimap<QualifiedName, PbSymbol> multimap = convertJdkMapToGuava(file.getExportedQualifiedSymbolMap());
      builder.putAll(multimap);
    }
    return new PbSymbolResolver(builder.build());
  }

  private static Multimap<QualifiedName, PbSymbol> convertJdkMapToGuava(Map<QualifiedName, Collection<PbSymbol>> jdkMap) {
    SetMultimap<QualifiedName, PbSymbol> multimap = Multimaps.newSetMultimap(new HashMap<>(), HashSet::new);
    jdkMap.forEach((key, value) -> multimap.putAll(key, value));
    return multimap;
  }

  /** Returns an empty PbSymbolResolver. */
  public static PbSymbolResolver empty() {
    return new PbSymbolResolver(ImmutableMultimap.of());
  }

  public List<PbResolveResult> resolveRelativeName(
    QualifiedName name, QualifiedName scope, Condition<PbSymbol> condition) {

    if (name.getComponentCount() == 0) {
      // Empty name given. No results.
      return Collections.emptyList();
    }

    if (scope == null) {
      // This isn't a valid condition in which we can find a reference. An empty scope (e.g.,
      // top-level) would be a QualifiedName with no components.
      return Collections.emptyList();
    }

    // If name is something like "Foo.Bar.baz", and symbols named "Foo" are
    // defined in multiple parent scopes, we only want to find "Bar.baz" in the
    // innermost one.  E.g., the following should produce an error:
    //   message Bar { message Baz {} }
    //   message Foo {
    //     message Bar {
    //     }
    //     optional Bar.Baz baz = 1;
    //   }
    // So, we look for just "Foo" first, then look for "Bar.baz" within it if
    // found.
    while (scope.getComponentCount() > 0) {
      if (name.getComponentCount() == 1) {
        // For completely unqualified names (e.g., "MyType"), simply search for the symbol using
        // the provided predicate at this scope.
        List<PbResolveResult> results = resolveName(scope.append(name), condition);
        if (!results.isEmpty()) {
          return results;
        }
      } else {
        // For partially-qualified names, find the first symbol owner ancestor matching the name's
        // first component, and search only from there.
        if (symbolOwnerExists(scope.append(name.getFirstComponent()))) {
          // We found a symbol owner (i.e., package, message, enum, service) at the current scope
          // whose name is equal to the first component of the target name. Return any results from
          // this point in the tree.
          return resolveName(scope.append(name), condition);
        }
      }
      scope = scope.removeLastComponent();
    }

    // One last attempt without any scope.
    return resolveName(name, condition);
  }

  public List<PbResolveResult> resolveName(QualifiedName name, Condition<PbSymbol> condition) {
    return symbols
      .get(name)
      .stream()
      .filter(condition::value)
      .map(PbResolveResult::create)
      .collect(Collectors.toList());
  }

  public ImmutableMultimap<String, PbSymbol> findChildren(
    QualifiedName parentName, Condition<PbSymbol> condition) {
    if (parentName.getComponentCount() == 0) {
      // Special case - return all top-level symbols.
      return findTopLevelSymbols(condition);
    }
    List<PbResolveResult> parentResults = resolveName(parentName, ResolveFilters.packageOrType());
    ImmutableMultimap.Builder<String, PbSymbol> builder = ImmutableMultimap.builder();
    parentResults
      .stream()
      .map(PbResolveResult::getElement)
      .filter(element -> element instanceof PbSymbolOwner)
      .map(element -> ((PbSymbolOwner) element).getSymbols())
      .flatMap(Collection::stream)
      .filter(condition::value)
      .forEach(
        symbol -> {
          String name = symbol.getName();
          if (name != null) {
            builder.put(name, symbol);
          }
        });
    return builder.build();
  }

  private ImmutableMultimap<String, PbSymbol> findTopLevelSymbols(Condition<PbSymbol> condition) {
    // First, filter the symbols map to the collection of top-level symbols matching the given
    // predicate.
    Multimap<QualifiedName, PbSymbol> filtered =
      Multimaps.filterEntries(
        symbols,
        e -> e != null && e.getKey().getComponentCount() == 1 && condition.value(e.getValue()));

    // Next, convert the map into a Multimap<String, PbSymbol>
    ImmutableMultimap.Builder<String, PbSymbol> builder = ImmutableMultimap.builder();
    for (Map.Entry<QualifiedName, Collection<PbSymbol>> entry : filtered.asMap().entrySet()) {
      QualifiedName name = entry.getKey();
      if (name == null) {
        continue;
      }
      String first = name.getFirstComponent();
      if (first == null) {
        continue;
      }
      builder.putAll(first, entry.getValue());
    }
    return builder.build();
  }

  private boolean symbolOwnerExists(QualifiedName symbol) {
    return !resolveName(symbol, ResolveFilters.symbolOwner()).isEmpty();
  }
}
