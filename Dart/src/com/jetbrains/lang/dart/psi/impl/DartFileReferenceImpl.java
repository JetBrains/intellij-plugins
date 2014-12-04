package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.psi.DartPartStatement;
import com.jetbrains.lang.dart.psi.DartPathOrLibraryReference;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.jetbrains.lang.dart.util.DartUrlResolver.*;

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
    final DartPartStatement sourceStatement =
      DartElementGenerator.createPartStatementFromPath(getProject(), newPath);
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
    final VirtualFile parentFolder = virtualFile == null ? null : virtualFile.getParent();
    final VirtualFile destinationVirtualFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
    if (parentFolder != null && destinationVirtualFile != null) {
      setNewPath(FileUtil.getRelativePath(parentFolder.getPath(), destinationVirtualFile.getPath(), '/'));
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
    if (virtualFile == null) return ResolveResult.EMPTY_ARRAY;

    final String text = StringUtil.unquoteString(getText());
    final VirtualFile result = text.startsWith(PACKAGE_PREFIX) || text.startsWith(DART_PREFIX) || text.startsWith(FILE_PREFIX)
                               ? DartUrlResolver.getInstance(getProject(), virtualFile).findFileByDartUrl(text)
                               : VfsUtilCore.findRelativeFile(text, virtualFile);

    final PsiFile sourcePsiFile = result == null ? null : getManager().findFile(result);
    return sourcePsiFile == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(sourcePsiFile)};
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    final String url = StringUtil.unquoteString(getText());
    if (url.startsWith(PACKAGE_PREFIX)) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(getContainingFile());
      if (file == null) return PsiReference.EMPTY_ARRAY;

      final DartUrlResolver dartUrlResolver = DartUrlResolver.getInstance(getProject(), file);

      final int slashIndex = url.indexOf('/');
      if (slashIndex > 0) {
        final String packageName = url.substring(PACKAGE_PREFIX.length(), slashIndex);
        final VirtualFile packageDir = dartUrlResolver.getPackageDirIfLivePackageOrFromPubListPackageDirs(packageName);
        if (packageDir != null) {
          return getPackageReferences(file, packageDir, url.substring(slashIndex + 1), slashIndex + 1);
        }
      }

      final String relPath = url.substring(PACKAGE_PREFIX.length());
      final VirtualFile[] packageRoots = dartUrlResolver.getPackageRoots();

      if (packageRoots.length == 0) return PsiReference.EMPTY_ARRAY;
      if (packageRoots.length == 1) return getPackageReferences(file, packageRoots[0], relPath, PACKAGE_PREFIX.length());

      final Collection<PsiReference> result = new SmartList<PsiReference>();
      for (VirtualFile packageRoot : packageRoots) {
        if (packageRoot.findFileByRelativePath(relPath) != null) {
          ContainerUtil.addAll(result, getPackageReferences(file, packageRoot, relPath, PACKAGE_PREFIX.length()));
        }
      }

      return result.toArray(new PsiReference[result.size()]);
    }

    final FileReferenceSet referenceSet = new FileReferenceSet(url, this, 1, null, false, true);
    return ArrayUtil.mergeArrays(super.getReferences(), referenceSet.getAllReferences());
  }

  @NotNull
  private PsiReference[] getPackageReferences(final @NotNull VirtualFile contextFile,
                                              final @Nullable VirtualFile packagesFolder,
                                              final @NotNull String relPathFromPackagesFolderToReferencedFile,
                                              final int startIndex) {
    final VirtualFile parentFile = contextFile.getParent();
    if (packagesFolder == null || parentFile == null) return PsiReference.EMPTY_ARRAY;

    String relPathFromContextFileToPackagesFolder = FileUtil.getRelativePath(parentFile.getPath(), packagesFolder.getPath(), '/');
    if (relPathFromContextFileToPackagesFolder == null) return PsiReference.EMPTY_ARRAY;

    relPathFromContextFileToPackagesFolder += "/";
    final FileReferenceSet referenceSet =
      new FileReferenceSet(relPathFromContextFileToPackagesFolder + relPathFromPackagesFolderToReferencedFile, this, 0, null, false, true);
    final FileReference[] references = referenceSet.getAllReferences();

    final int nestedLevel = StringUtil.countChars(relPathFromContextFileToPackagesFolder, '/');
    final int shift = startIndex - relPathFromContextFileToPackagesFolder.length() + 1;
    return references.length < nestedLevel ?
           PsiReference.EMPTY_ARRAY :
           shiftReferences(Arrays.copyOfRange(references, nestedLevel, references.length), shift);
  }

  private static FileReference[] shiftReferences(FileReference[] references, final int shift) {
    return ContainerUtil.map(references, new Function<FileReference, FileReference>() {
      @Override
      public FileReference fun(FileReference reference) {
        return new FileReference(
          reference.getFileReferenceSet(),
          reference.getRangeInElement().shiftRight(shift),
          reference.getIndex(),
          reference.getText()
        );
      }
    }, FileReference.EMPTY);
  }

  public static class DartPathOrLibraryManipulator implements ElementManipulator<DartPathOrLibraryReference> {
    @Override
    public DartPathOrLibraryReference handleContentChange(@NotNull DartPathOrLibraryReference element,
                                                          @NotNull TextRange range,
                                                          String newContent)
      throws IncorrectOperationException {
      return element;
    }

    @Override
    public DartPathOrLibraryReference handleContentChange(@NotNull DartPathOrLibraryReference element, String newContent)
      throws IncorrectOperationException {
      return element;
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull DartPathOrLibraryReference element) {
      return element.getTextRange();
    }
  }
}
