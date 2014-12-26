package com.jetbrains.lang.dart.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.DartImportOrExportStatement;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference to 'dart:lib_from_sdk' in import or export directive
 */
public class DartSdkLibReference implements PsiReference {
  @NotNull private final PsiElement myElement;
  @NotNull private final String myUri;
  @NotNull private final TextRange myRange;

  public DartSdkLibReference(@NotNull final DartUriElementBase uriRefExpr, @NotNull final String uri) {
    assert uri.startsWith(DartUrlResolver.DART_PREFIX) : uri;
    final int offset = uriRefExpr.getText().indexOf(uri);
    assert offset >= 0 : uriRefExpr.getText() + " doesn't contain " + uri;

    myElement = uriRefExpr;
    myUri = uri;
    myRange = TextRange.create(offset, offset + uri.length());
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @Override
  public TextRange getRangeInElement() {
    return myRange;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String libName = myUri.substring(DartUrlResolver.DART_PREFIX.length());
    final VirtualFile targetFile = DartLibraryIndex.getStandardLibraryFromSdk(myElement.getProject(), libName);
    return targetFile == null ? null : myElement.getManager().findFile(targetFile);
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myUri;
  }

  @Override
  public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
    throw new IncorrectOperationException("Can't rename library from Dart SDK");
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    throw new IncorrectOperationException("Library from Dart SDK can't be renamed");
  }

  @Override
  public boolean isReferenceTo(final PsiElement element) {
    return element != null && element.equals(resolve());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return getSdkLibNamesAsCompletionVariants(myElement);
  }

  public static Object[] getSdkLibNamesAsCompletionVariants(final PsiElement element) {
    // do not suggest dart:lib_from_sdk for 'part of' directive
    if (element.getParent() instanceof DartImportOrExportStatement) {
      return ContainerUtil.map2Array(DartLibraryIndex.getAllStandardLibrariesFromSdk(element.getProject()),
                                     new Function<String, Object>() {
                                       @Override
                                       public Object fun(final String sdkLib) {
                                         return DartUrlResolver.DART_PREFIX + sdkLib;
                                       }
                                     });
    }

    return EMPTY_ARRAY;
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
