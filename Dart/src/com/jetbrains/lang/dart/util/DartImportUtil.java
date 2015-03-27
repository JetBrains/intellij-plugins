package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.DartResolveScopeProvider;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartImportUtil {
  private DartImportUtil() {
  }

  public static void insertImport(@NotNull final PsiFile context, @NotNull final String componentName, @NotNull final String urlToImport) {
    final PsiManager psiManager = context.getManager();
    libraryRootLoop:
    for (VirtualFile libraryRoot : DartResolveUtil.findLibrary(context)) {
      final PsiFile file = psiManager.findFile(libraryRoot);
      if (file == null) {
        continue;
      }
      final DartImportStatement[] importStatements = PsiTreeUtil.getChildrenOfType(file, DartImportStatement.class);
      if (importStatements != null) {
        for (DartImportStatement importStatement : importStatements) {
          if (urlToImport.equals(importStatement.getUriString())) {
            addShowOrRemoveHide(importStatement, componentName);
            continue libraryRootLoop;
          }
        }
      }

      final PsiElement toAdd = DartElementGenerator.createTopLevelStatementFromText(file.getProject(), "import '" + urlToImport + "';");
      if (toAdd != null) {
        final PsiElement anchor = findAnchorForImportStatement(file, importStatements);
        if (anchor == null) {
          final PsiElement child = file.getFirstChild();
          file.addBefore(toAdd, child);
          if (!(child instanceof PsiWhiteSpace)) {
            file.getNode().addLeaf(DartTokenTypesSets.WHITE_SPACE, "\n", child.getNode());
          }
        }
        else {
          file.addAfter(toAdd, anchor);
        }
      }
    }
  }

  private static void addShowOrRemoveHide(@NotNull final DartImportStatement importStatement, @NotNull final String componentName) {
    // try to remove hide
    for (DartHideCombinator hideCombinator : importStatement.getHideCombinatorList()) {
      final DartLibraryReferenceList libraryReferenceList = hideCombinator.getLibraryReferenceList();
      if (libraryReferenceList != null) {
        final List<DartLibraryComponentReferenceExpression> libraryComponents =
          libraryReferenceList.getLibraryComponentReferenceExpressionList();
        for (DartLibraryComponentReferenceExpression libraryComponentReferenceExpression : libraryComponents) {
          if (componentName.equals(libraryComponentReferenceExpression.getText())) {
            if (libraryComponents.size() == 1) {
              hideCombinator.delete();
            }
            else {
              final PsiElement nextSibling =
                UsefulPsiTreeUtil.getNextSiblingSkippingWhiteSpacesAndComments(libraryComponentReferenceExpression);
              if (nextSibling != null && nextSibling.getNode().getElementType() == DartTokenTypes.COMMA) {
                nextSibling.delete();
              }
              else {
                final PsiElement prevSibling =
                  UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpacesAndComments(libraryComponentReferenceExpression, true);
                if (prevSibling != null && prevSibling.getNode().getElementType() == DartTokenTypes.COMMA) {
                  prevSibling.delete();
                }
              }

              libraryComponentReferenceExpression.delete();
            }
            return;
          }
        }
      }
    }

    // add show
    final List<DartShowCombinator> showCombinators = importStatement.getShowCombinatorList();
    if (showCombinators.isEmpty()) {
      // something wrong
      return;
    }
    final DartShowCombinator combinatorToAdd = showCombinators.iterator().next();
    final DartLibraryComponentReferenceExpression libraryComponentReference =
      DartElementGenerator.createLibraryComponentReference(importStatement.getProject(), componentName);
    final DartLibraryReferenceList libraryReferenceList = combinatorToAdd.getLibraryReferenceList();
    if (libraryComponentReference != null && libraryReferenceList != null) {
      libraryReferenceList.getNode().addLeaf(DartTokenTypes.COMMA, ",", null);
      libraryReferenceList.getNode().addLeaf(DartTokenTypesSets.WHITE_SPACE, " ", null);
      libraryReferenceList.add(libraryComponentReference);
    }
  }

  private static PsiElement findAnchorForImportStatement(@NotNull PsiFile psiFile,
                                                         @Nullable DartImportStatement[] importStatements) {
    if (importStatements != null && importStatements.length > 0) {
      return importStatements[importStatements.length - 1];
    }
    return PsiTreeUtil.getChildOfType(psiFile, DartLibraryStatement.class);
  }

  @Nullable
  public static String getUrlToImport(@NotNull final PsiElement context, @NotNull final String libraryName) {
    final VirtualFile contextFile = context.getContainingFile().getVirtualFile();
    if (contextFile == null) return null;

    final DartUrlResolver urlResolver = DartUrlResolver.getInstance(context.getProject(), contextFile);

    final GlobalSearchScope scope = DartResolveScopeProvider.getDartScope(context.getProject(), contextFile, true);
    if (scope == null) return null;

    for (VirtualFile libraryFile : DartLibraryIndex.getFilesByLibName(scope, libraryName)) {
      String urlToImport = urlResolver.getDartUrlForFile(libraryFile);

      if (urlToImport.startsWith(DartUrlResolver.DART_PREFIX) && urlToImport.contains("/")) {
        // HtmlElement class is declared in 2 files: html_dartium.dart and html_dart2js.dart.
        // Url to import for the 1st one is "dart:html" - that's what we need. For the 2nd it is "dart:html/dart2js/html_dart2js.dart - that's not what we want.
        continue;
      }

      if (urlToImport.startsWith(DartUrlResolver.FILE_PREFIX)) {
        final String relativePath = FileUtil.getRelativePath(contextFile.getParent().getPath(), libraryFile.getPath(), '/');
        if (relativePath != null) {
          urlToImport = relativePath;
        }
        else {
          continue;
        }
      }

      return urlToImport;
    }

    return null;
  }
}
