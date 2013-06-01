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
  public static String getPath(@NotNull DartSourceStatement sourceStatement) {
    final DartExpression expression = sourceStatement.getPathOrLibraryReference();
    return expression == null ? "" : FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static String getPath(@NotNull DartResourceStatement resourceStatement) {
    final DartExpression expression = resourceStatement.getPathOrLibraryReference();
    return expression == null ? "" : FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static String getPath(@NotNull DartNativeStatement nativeStatement) {
    final DartExpression expression = nativeStatement.getPathOrLibraryReference();
    return expression == null ? "" : FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartLibraryStatement resourceStatement) {
    final DartExpression expression = resourceStatement.getPathOrLibraryReference();
    if (expression != null) {
      return FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
    }
    final DartQualifiedComponentName componentName = resourceStatement.getQualifiedComponentName();
    if (componentName != null) {
      return StringUtil.notNullize(componentName.getName());
    }
    return "";
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

  @NotNull
  public static String getLibraryName(@NotNull DartImportStatement importStatement) {
    final DartExpression expression = importStatement.getLibraryExpression();
    return expression == null ? "" : FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @Nullable
  public static PsiElement resolveReference(@NotNull DartType dartType) {
    final DartExpression expression = dartType.getReferenceExpression();
    final String typeName = expression.getText();
    if (typeName.indexOf('.') != -1) {
      return expression instanceof DartReference ? ((DartReference)expression).resolve() : null;
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
}
