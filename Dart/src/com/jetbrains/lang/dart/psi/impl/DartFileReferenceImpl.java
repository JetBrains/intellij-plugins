package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.index.DartLibraryIndex;
import com.jetbrains.lang.dart.psi.DartPartStatement;
import com.jetbrains.lang.dart.psi.DartPathOrLibraryReference;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.psi.DartStringLiteralExpression;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.jetbrains.lang.dart.util.DartResolveUtil.PACKAGE_PREFIX;

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
    final String text = StringUtil.unquoteString(getText());
    if (text.startsWith(PACKAGE_PREFIX)) {
      final VirtualFile pubspecYamlFile = virtualFile == null ? null : PubspecYamlUtil.getPubspecYamlFile(getProject(), virtualFile);
      final String pubspecName = pubspecYamlFile == null ? null : PubspecYamlUtil.getPubspecName(pubspecYamlFile);
      final String prefix = pubspecName == null ? null : PACKAGE_PREFIX + pubspecName + "/";

      if (prefix != null && text.startsWith(prefix)) {
        final String relativePath = text.substring(prefix.length());
        final VirtualFile libFolder = pubspecYamlFile.getParent().findChild("lib");
        final VirtualFile sourceFile = libFolder == null ? null : VfsUtilCore.findRelativeFile(relativePath, libFolder);
        final PsiFile sourcePsiFile = sourceFile == null ? null : psiFile.getManager().findFile(sourceFile);
        return sourcePsiFile == null ? ResolveResult.EMPTY_ARRAY : new ResolveResult[]{new PsiElementResolveResult(sourcePsiFile)};
      }

      final VirtualFile packagesFolder = virtualFile == null ? null : PubspecYamlUtil.getDartPackagesFolder(getProject(), virtualFile);
      final String relativePath = text.substring(PACKAGE_PREFIX.length());
      final VirtualFile sourceFile = packagesFolder == null ? null : VfsUtilCore.findRelativeFile(relativePath, packagesFolder);
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
    if (path.startsWith(PACKAGE_PREFIX)) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(getContainingFile());
      if (file == null) return PsiReference.EMPTY_ARRAY;

      final VirtualFile pubspecYamlFile = PubspecYamlUtil.getPubspecYamlFile(getProject(), file);
      final String pubspecName = pubspecYamlFile == null ? null : PubspecYamlUtil.getPubspecName(pubspecYamlFile);
      final String prefix = pubspecName == null ? null : PACKAGE_PREFIX + pubspecName + "/";

      if (prefix != null && path.startsWith(prefix)) {
        final VirtualFile libFolder = pubspecYamlFile.getParent().findChild("lib");
        return getPackageReferences(file, libFolder, path.substring(prefix.length()), prefix.length() + 1);
      }
      else {
        final VirtualFile packagesFolder = PubspecYamlUtil.getDartPackagesFolder(getProject(), file);
        return getPackageReferences(file, packagesFolder, path.substring(PACKAGE_PREFIX.length()), PACKAGE_PREFIX.length() + 1);
      }
    }
    final FileReferenceSet referenceSet = new FileReferenceSet(path, this, 1, null, false, true);
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
    final int shift = startIndex - relPathFromContextFileToPackagesFolder.length();
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
