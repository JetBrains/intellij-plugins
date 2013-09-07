package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartPsiImplUtil {
  @NotNull
  public static String normalizeLibraryName(@NotNull String libraryName) {
    return libraryName.startsWith("dart.") ? "dart:" + libraryName.substring("dart.".length()) : libraryName;
  }

  @NotNull
  public static String getPath(@NotNull DartPartStatement partStatement) {
    final DartExpression expression = partStatement.getPathOrLibraryReference();
    return FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartLibraryStatement libraryStatement) {
    final DartQualifiedComponentName componentName = libraryStatement.getQualifiedComponentName();
    return normalizeLibraryName(StringUtil.notNullize(componentName.getName()));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartImportStatement importStatement) {
    final DartExpression expression = importStatement.getLibraryExpression();
    return FileUtil.toSystemIndependentName(normalizeLibraryName(StringUtil.unquoteString(expression.getText())));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartPartOfStatement importStatement) {
    final DartLibraryId expression = importStatement.getLibraryId();
    return FileUtil.toSystemIndependentName(normalizeLibraryName(StringUtil.unquoteString(expression.getText())));
  }

  @Nullable
  public static PsiElement getLibraryPrefix(@NotNull DartImportStatement resourceStatement) {
    final DartComponentName componentName = resourceStatement.getComponentName();
    if (componentName != null) {
      return componentName;
    }
    final DartExpression[] childrenOfType = PsiTreeUtil.getChildrenOfType(resourceStatement, DartExpression.class);
    return childrenOfType == null || childrenOfType.length < 2 ? null : childrenOfType[1];
  }

  @Nullable
  public static PsiElement resolveReference(@NotNull DartType dartType) {
    final DartExpression expression = dartType.getReferenceExpression();
    final String typeName = expression.getText();
    if (typeName.indexOf('.') != -1) {
      return ((DartReference)expression).resolve();
    }
    List<DartComponentName> result = new ArrayList<DartComponentName>();
    final ResolveScopeProcessor resolveScopeProcessor = new ResolveScopeProcessor(result, typeName);

    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(dartType.getContainingFile());
    if (virtualFile != null) {
      DartResolveUtil.processTopLevelDeclarations(dartType, resolveScopeProcessor,
                                                  virtualFile, typeName);
    }
    // find type parameter
    if (result.isEmpty()) {
      PsiTreeUtil.treeWalkUp(resolveScopeProcessor, dartType, null, new ResolveState());
      for (Iterator<DartComponentName> iterator = result.iterator(); iterator.hasNext(); ) {
        if (!(iterator.next().getParent() instanceof DartTypeParameter)) {
          iterator.remove();
        }
      }
    }
    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartResolveUtil.findLibrary(dartType.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(dartType, resolveScopeProcessor, libraryFile, typeName);
    }
    // dart:core
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartLibraryIndex.findLibraryClass(dartType, "dart:core");
      DartResolveUtil.processTopLevelDeclarations(dartType, resolveScopeProcessor, libraryFile, typeName);
    }
    return result.isEmpty() ? null : result.iterator().next();
  }

  @Nullable
  public static DartComponentName findComponentName(@NotNull DartNormalFormalParameter namedFormalParameters) {
    final DartVarDeclaration varDeclaration = namedFormalParameters.getVarDeclaration();
    if (varDeclaration != null) {
      return varDeclaration.getVarAccessDeclaration().getComponentName();
    }
    final DartFunctionDeclaration functionDeclaration = namedFormalParameters.getFunctionDeclaration();
    if (functionDeclaration != null) {
      return functionDeclaration.getComponentName();
    }
    return namedFormalParameters.getComponentName();
  }

  public static DartExpression getParameterReferenceExpression(DartNamedArgument argument) {
    return PsiTreeUtil.getChildOfType(argument, DartExpression.class);
  }

  public static DartExpression getExpression(DartNamedArgument argument) {
    final DartExpression[] expressions = PsiTreeUtil.getChildrenOfType(argument, DartExpression.class);
    return expressions != null && expressions.length > 1 ? expressions[expressions.length - 1] : null;
  }
}
