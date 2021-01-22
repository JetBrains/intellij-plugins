// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartControlFlowUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.jetbrains.lang.dart.ide.index.DartImportOrExportInfo.Kind;

public final class DartIndexUtil {
  // inc when change parser
  public static final int INDEX_VERSION = 25;

  private static final Key<DartFileIndexData> ourDartCachesData = Key.create("dart.caches.index.data");

  public static DartFileIndexData indexFile(FileContent content) {
    DartFileIndexData indexData = content.getUserData(ourDartCachesData);
    if (indexData != null) return indexData;
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (content) {
      indexData = content.getUserData(ourDartCachesData);
      if (indexData != null) return indexData;
      indexData = indexFileRoots(content.getPsiFile());
    }

    return indexData;
  }

  private static DartFileIndexData indexFileRoots(PsiFile psiFile) {
    DartFileIndexData result = new DartFileIndexData();

    final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(psiFile, DartLibraryStatement.class);
    if (libraryStatement != null) {
      result.setLibraryName(libraryStatement.getLibraryNameElement().getName());
    }

    result.setIsPart(PsiTreeUtil.getChildOfType(psiFile, DartPartOfStatement.class) != null);

    if (psiFile instanceof DartFile) {
      PsiElement[] children = psiFile.getChildren();

      for (DartComponentName componentName : DartControlFlowUtil.getSimpleDeclarations(children, null, false)) {
        final String name = componentName.getName();
        if (name == null) {
          continue;
        }

        result.addSymbol(name);

        PsiElement parent = componentName.getParent();
        final DartComponentType type = DartComponentType.typeOf(parent);
        if (type != null) {
          result.addComponentInfo(name, new DartComponentInfo(type, result.getLibraryName()));
        }
        if (parent instanceof DartClass) {
          result.addClassName(name);

          if (((DartClass)parent).isEnum()) {
            for (DartEnumConstantDeclaration enumConstantDeclaration : ((DartClass)parent).getEnumConstantDeclarationList()) {
              result.addSymbol(enumConstantDeclaration.getName());
            }
          }
          else {
            for (DartComponent subComponent : DartResolveUtil.getNamedSubComponents((DartClass)parent)) {
              result.addSymbol(subComponent.getName());
            }
          }
        }
      }

      for (PsiElement child : children) {
        if (child instanceof DartImportOrExportStatement) {
          processImportOrExportStatement(result, (DartImportOrExportStatement)child);
        }
        if (child instanceof DartPartStatement) {
          result.addPartUri(((DartPartStatement)child).getUriString());
        }
      }
    }
    return result;
  }

  private static void processImportOrExportStatement(final @NotNull DartFileIndexData result,
                                                     final @NotNull DartImportOrExportStatement importOrExportStatement) {
    final String uri = importOrExportStatement.getUriString();

    final Set<String> showComponentNames = new HashSet<>();
    for (DartShowCombinator showCombinator : importOrExportStatement.getShowCombinatorList()) {
      final DartLibraryReferenceList libraryReferenceList = showCombinator.getLibraryReferenceList();
      if (libraryReferenceList != null) {
        for (DartExpression expression : libraryReferenceList.getLibraryComponentReferenceExpressionList()) {
          showComponentNames.add(expression.getText());
        }
      }
    }

    final Set<String> hideComponentNames = new HashSet<>();
    for (DartHideCombinator hideCombinator : importOrExportStatement.getHideCombinatorList()) {
      final DartLibraryReferenceList libraryReferenceList = hideCombinator.getLibraryReferenceList();
      if (libraryReferenceList != null) {
        for (DartExpression expression : libraryReferenceList.getLibraryComponentReferenceExpressionList()) {
          hideComponentNames.add(expression.getText());
        }
      }
    }

    final DartComponentName importPrefixComponent = importOrExportStatement instanceof DartImportStatement
                                                    ? ((DartImportStatement)importOrExportStatement).getImportPrefix()
                                                    : null;
    final String importPrefix = importPrefixComponent != null ? importPrefixComponent.getName() : null;

    final Kind kind = importOrExportStatement instanceof DartImportStatement ? Kind.Import : Kind.Export;
    result.addImportInfo(new DartImportOrExportInfo(kind, uri, importPrefix, showComponentNames, hideComponentNames));
    result.addComponentInfo(importPrefix, new DartComponentInfo(DartComponentType.LABEL, null));
  }
}
