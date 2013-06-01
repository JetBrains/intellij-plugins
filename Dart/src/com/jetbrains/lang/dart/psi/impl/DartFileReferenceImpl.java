package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.DartPathOrLibraryReference;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.psi.DartSourceStatement;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Fedor.Korotkov
 */
public class DartFileReferenceImpl extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartFileReferenceImpl(ASTNode node) {
    super(node);
  }

  @Override
  public PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @Override
  public TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    setNewPath(getNewPath(newElementName));
    return this;
  }

  private void setNewPath(String newPath) {
    final DartSourceStatement sourceStatement =
      DartElementGenerator.createSourceStatementFromPath(getProject(), "'" + newPath + "'");
    final DartPathOrLibraryReference newPathReference = sourceStatement == null ? null : sourceStatement.getPathOrLibraryReference();
    final DartStringLiteralExpression myStringLiteralExpression = PsiTreeUtil.getChildOfType(this, DartStringLiteralExpression.class);
    if (newPathReference != null && myStringLiteralExpression != null) {
      getNode().replaceChild(myStringLiteralExpression.getNode(), newPathReference.getStringLiteralExpression().getNode());
    }
  }

  private String getNewPath(String name) {
    final String path = StringUtil.unquoteString(getText());
    final int index = Math.max(path.lastIndexOf('\\'), path.lastIndexOf('/'));
    return index == -1 ? name : path.substring(0, index + 1) + name;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(getContainingFile());
    final VirtualFile destinationVirtualFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
    if (virtualFile != null && destinationVirtualFile != null) {
      setNewPath(FileUtil.getRelativePath(virtualFile.getParent().getPath(), destinationVirtualFile.getPath(), '/'));
    }
    return this;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return resolve() == element;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    // DartLibraryNameCompletionContributor
    return ArrayUtil.EMPTY_OBJECT_ARRAY;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);

    return resolveResults.length == 0 ||
           resolveResults.length > 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @NotNull
  @Override
  public DartClassResolveResult resolveDartClass() {
    return DartClassResolveResult.EMPTY;
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    final PsiFile psiFile = getContainingFile();
    final VirtualFile virtualFile = DartResolveUtil.getRealVirtualFile(psiFile);
    final String text = StringUtil.unquoteString(getText());
    if (text.startsWith(DartResolveUtil.PACKAGE_PREFIX)) {
      final VirtualFile packagesFolder = DartResolveUtil.findPackagesFolder(this);
      String relativePath = FileUtil.toSystemIndependentName(text.substring(DartResolveUtil.PACKAGE_PREFIX.length()));
      final VirtualFile sourceFile = packagesFolder == null
                                     ? null
                                     : VfsUtil.findRelativeFile(packagesFolder, relativePath.split("/"));
      final PsiFile sourcePsiFile = sourceFile == null ? null : psiFile.getManager().findFile(sourceFile);
      return sourcePsiFile == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(sourcePsiFile)};
    }
    VirtualFile sourceFile = virtualFile == null ? null : DartResolveUtil.findRelativeFile(virtualFile, text);
    sourceFile = sourceFile != null ? sourceFile : VirtualFileManager.getInstance().findFileByUrl(text);

    if (sourceFile != null) {
      final PsiFile sourcePsiFile = psiFile.getManager().findFile(sourceFile);
      return sourcePsiFile == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(sourcePsiFile)};
    }
    return tryResolveLibraries();
  }

  private ResolveResult[] tryResolveLibraries() {
    final String libraryName = StringUtil.unquoteString(getText());
    final List<VirtualFile> virtualFiles = DartLibraryIndex.findLibraryClass(this, libraryName);
    final List<PsiElementResolveResult> result = new ArrayList<PsiElementResolveResult>();
    for (VirtualFile virtualFile : virtualFiles) {
      final PsiFile psiFile = getManager().findFile(virtualFile);
      if (psiFile == null) {
        continue;
      }
      result.add(new PsiElementResolveResult(psiFile));
    }
    return result.toArray(new ResolveResult[result.size()]);
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    final String path = StringUtil.unquoteString(getText());
    if (path.startsWith(DartResolveUtil.PACKAGE_PREFIX)) {
      int length = DartResolveUtil.PACKAGE_PREFIX.length();
      return getPackageReferences(path.substring(length), length + 1);
    }
    final FileReferenceSet referenceSet = new FileReferenceSet(path, this, 1, null, false, true);
    return ArrayUtil.mergeArrays(super.getReferences(), referenceSet.getAllReferences());
  }

  private PsiReference[] getPackageReferences(String path, int startIndex) {
    VirtualFile packagesFolder = DartResolveUtil.findPackagesFolder(this);
    VirtualFile file = DartResolveUtil.getRealVirtualFile(getContainingFile());
    if (packagesFolder == null || file == null) {
      return PsiReference.EMPTY_ARRAY;
    }
    String prefix = FileUtil.getRelativePath(file.getParent().getPath(), packagesFolder.getPath(), '/');
    if (prefix == null) {
      return null;
    }
    prefix += "/";
    int index = startIndex - prefix.length();
    final FileReferenceSet referenceSet = new FileReferenceSet(prefix + path, this, index, null, false, true);
    FileReference[] references = referenceSet.getAllReferences();
    int nestedLevel = StringUtil.countChars(prefix, '/');
    return references.length < nestedLevel ? PsiReference.EMPTY_ARRAY : Arrays.copyOfRange(references, nestedLevel, references.length);
  }

  private int getNestedLevel(int startIndex, VirtualFile packagesFolder) {
    return new FileReferenceSet(packagesFolder.getPath(), this, startIndex, null, false, true).getAllReferences().length;
  }

  public static class DartPathOrLibraryManipulator implements ElementManipulator<DartPathOrLibraryReference> {
    @Override
    public DartPathOrLibraryReference handleContentChange(DartPathOrLibraryReference element, TextRange range, String newContent)
      throws IncorrectOperationException {
      return element;
    }

    @Override
    public DartPathOrLibraryReference handleContentChange(DartPathOrLibraryReference element, String newContent)
      throws IncorrectOperationException {
      return element;
    }

    @Override
    public TextRange getRangeInElement(DartPathOrLibraryReference element) {
      return element.getTextRange();
    }
  }
}
