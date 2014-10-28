package com.jetbrains.lang.dart.util;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.resolve.DartResolveProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DartPsiImplUtil {

  @NotNull
  public static String getPath(@NotNull DartPartStatement partStatement) {
    final DartExpression expression = partStatement.getPathOrLibraryReference();
    return FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static String getLibraryName(@NotNull DartLibraryStatement libraryStatement) {
    final DartQualifiedComponentName componentName = libraryStatement.getQualifiedComponentName();
    return StringUtil.notNullize(componentName.getName());
  }

  @NotNull
  public static String getUri(@NotNull DartImportOrExportStatement importStatement) {
    final DartExpression expression = importStatement.getLibraryExpression();
    return StringUtil.unquoteString(expression.getText());
  }

  @NotNull
  public static String getLibraryName(@NotNull DartPartOfStatement importStatement) {
    final DartLibraryId expression = importStatement.getLibraryId();
    return FileUtil.toSystemIndependentName(StringUtil.unquoteString(expression.getText()));
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartLabel label) {
    return PsiTreeUtil.getChildrenOfTypeAsList(label, DartMetadata.class);
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartEnumConstantDeclaration label) {
    return PsiTreeUtil.getChildrenOfTypeAsList(label, DartMetadata.class);
  }

  @NotNull
  public static List<DartMetadata> getMetadataList(@NotNull DartVarDeclarationListPart varDeclarationListPart) {
    return PsiTreeUtil.getChildrenOfTypeAsList(varDeclarationListPart, DartMetadata.class);
  }

  @Nullable
  public static PsiElement resolveReference(@NotNull DartType dartType) {
    final DartExpression expression = dartType.getReferenceExpression();
    final String typeName = expression.getText();
    if (typeName.indexOf('.') != -1) {
      return ((DartReference)expression).resolve();
    }
    List<DartComponentName> result = new ArrayList<DartComponentName>();
    final DartResolveProcessor dartResolveProcessor = new DartResolveProcessor(result, typeName);

    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(dartType.getContainingFile());
    if (virtualFile != null) {
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, virtualFile, typeName);
    }
    // find type parameter
    if (result.isEmpty()) {
      PsiTreeUtil.treeWalkUp(dartResolveProcessor, dartType, null, ResolveState.initial());
      for (Iterator<DartComponentName> iterator = result.iterator(); iterator.hasNext(); ) {
        if (!(iterator.next().getParent() instanceof DartTypeParameter)) {
          iterator.remove();
        }
      }
    }
    // global
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartResolveUtil.findLibrary(dartType.getContainingFile());
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, libraryFile, typeName);
    }
    // dart:core
    if (result.isEmpty()) {
      final List<VirtualFile> libraryFile = DartLibraryIndex.findLibraryClass(dartType, "dart:core");
      DartResolveUtil.processTopLevelDeclarations(dartType, dartResolveProcessor, libraryFile, typeName);
    }
    return result.isEmpty() ? null : result.iterator().next();
  }

  @Nullable
  public static DartComponentName findComponentName(final @NotNull DartNormalFormalParameter normalFormalParameter) {
    final DartFunctionSignature functionDeclaration = normalFormalParameter.getFunctionSignature();
    final DartFieldFormalParameter fieldFormalParameter = normalFormalParameter.getFieldFormalParameter();
    final DartSimpleFormalParameter simpleFormalParameter = normalFormalParameter.getSimpleFormalParameter();

    if (functionDeclaration != null) return functionDeclaration.getComponentName();
    if (fieldFormalParameter != null) return null;
    if (simpleFormalParameter != null) return simpleFormalParameter.getComponentName();

    assert false : normalFormalParameter.getText();
    return null;
  }

  public static DartExpression getParameterReferenceExpression(DartNamedArgument argument) {
    return PsiTreeUtil.getChildOfType(argument, DartExpression.class);
  }

  public static DartExpression getExpression(DartNamedArgument argument) {
    final DartExpression[] expressions = PsiTreeUtil.getChildrenOfType(argument, DartExpression.class);
    return expressions != null && expressions.length > 1 ? expressions[expressions.length - 1] : null;
  }

  @Nullable
  public static DartBlock getBlock(DartFunctionBody functionBody) {
    return PsiTreeUtil.getChildOfType(functionBody, DartBlock.class);
  }
}
