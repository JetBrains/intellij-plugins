package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import com.jetbrains.lang.dart.psi.DartLibraryStatement;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartLibraryIdBase extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartLibraryIdBase(ASTNode node) {
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
  public PsiElement handleElementRename(String newLibraryName) throws IncorrectOperationException {
    final DartLibraryNameElement libraryNameElement = DartElementGenerator.createLibraryNameElementFromText(getProject(), newLibraryName);
    if (libraryNameElement != null) {
      getNode().replaceAllChildrenToChildrenOf(libraryNameElement.getNode());
    }
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return this;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return resolve() == element;
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
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    if (DartResolver.isServerDrivenResolution()) {
      final List<? extends PsiElement> elements =
        ResolveCache.getInstance(getProject()).resolveWithCaching(this, DartResolver.INSTANCE, true, incompleteCode);
      return DartResolveUtil.toCandidateInfoArray(elements);
    }

    final String libraryName = getText();

    final List<VirtualFile> libFiles = DartResolveUtil.findLibraryByName(this, libraryName);
    if (libFiles.isEmpty()) return ResolveResult.EMPTY_ARRAY;

    final List<ResolveResult> result = new SmartList<ResolveResult>();

    for (VirtualFile libFile : libFiles) {
      final PsiFile psiFile = getManager().findFile(libFile);
      for (PsiElement root : DartResolveUtil.findDartRoots(psiFile)) {
        final DartLibraryStatement libraryStatement = PsiTreeUtil.getChildOfType(root, DartLibraryStatement.class);
        final DartLibraryNameElement libraryNameElement = libraryStatement == null ? null : libraryStatement.getLibraryNameElement();
        if (libraryNameElement != null) {
          result.add(new PsiElementResolveResult(libraryNameElement));
        }
      }
    }

    return result.toArray(new ResolveResult[result.size()]);
  }

  @NotNull
  @Override
  public DartClassResolveResult resolveDartClass() {
    return DartClassResolveResult.EMPTY;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return PsiReference.EMPTY_ARRAY; // completion comes from DAS
    /*
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(getContainingFile());
    if (vFile == null) return PsiElement.EMPTY_ARRAY;

    final ProjectFileIndex index = ProjectRootManager.getInstance(getProject()).getFileIndex();
    VirtualFile scopeFolder = vFile.getParent();

    if (scopeFolder != null && scopeFolder.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
      VirtualFile parentFolder = scopeFolder.getParent();
      while (parentFolder != null && index.isInContent(parentFolder) && parentFolder.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
        scopeFolder = parentFolder;
        parentFolder = scopeFolder.getParent();
      }
    }

    if (scopeFolder == null) return PsiElement.EMPTY_ARRAY;

    // scopeFolder is either:
    // - pubspec.yaml file parent if current dart file is at the same level as pubspec.yaml
    // - direct subfolder of dart project root like 'bin' or 'web' if curennt dart file is inside at any level
    // - module content root if there's no pubspec.yaml file
    final GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(getProject(), scopeFolder, true);
    final CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<String>();
    FileBasedIndex.getInstance().processAllKeys(DartLibraryIndex.DART_LIBRARY_INDEX, processor, scope, null);

    return processor.toArray(new String[processor.getResults().size()]);
    */
  }
}
