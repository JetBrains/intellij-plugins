// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.DotPackagesFileUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;
import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGE_PREFIX;

class DartPackageAwareFileReference extends FileReference {
  @NotNull private final DartUrlResolver myDartResolver;

  DartPackageAwareFileReference(@NotNull final FileReferenceSet fileReferenceSet,
                                       final TextRange range,
                                       final int index,
                                       final String text,
                                       @NotNull final DartUrlResolver dartResolver) {
    super(fileReferenceSet, range, index, text);
    myDartResolver = dartResolver;
  }

  @Override
  @NotNull
  protected ResolveResult[] innerResolve(final boolean caseSensitive, @NotNull final PsiFile containingFile) {
    if (PACKAGES_FOLDER_NAME.equals(getText())) {
      final VirtualFile pubspecYamlFile = myDartResolver.getPubspecYamlFile();
      final VirtualFile packagesDir = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild(PACKAGES_FOLDER_NAME);
      final PsiDirectory psiDirectory = packagesDir == null ? null : containingFile.getManager().findDirectory(packagesDir);
      if (psiDirectory != null) {
        return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
      }
      VirtualFile dotPackages = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild(DotPackagesFileUtil.DOT_PACKAGES);
      final PsiFile psiFile = dotPackages == null ? null : containingFile.getManager().findFile(dotPackages);
      if (psiFile != null) {
        return new ResolveResult[]{new PsiElementResolveResult(psiFile)};
      }
    }

    final int index = getIndex();
    final FileReference[] allReferences = getFileReferenceSet().getAllReferences();
    if (index > 0 && PACKAGES_FOLDER_NAME.equals(allReferences[index - 1].getText())) {
      final StringBuilder b = new StringBuilder();
      for (int i = index + 1; i < allReferences.length; i++) {
        if (b.length() > 0) b.append('/');
        b.append(allReferences[i].getText());
      }

      final VirtualFile packageDir = myDartResolver.getPackageDirIfNotInOldStylePackagesFolder(getText(), b.toString());
      final PsiDirectory psiDirectory = packageDir == null ? null : containingFile.getManager().findDirectory(packageDir);
      if (psiDirectory != null) {
        return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
      }
    }

    return super.innerResolve(caseSensitive, containingFile);
  }

  @Override
  @NotNull
  public Object[] getVariants() {
    final Object[] superVariants = super.getVariants();

    if (getIndex() == 0) {
      final VirtualFile pubspecYamlFile = myDartResolver.getPubspecYamlFile();
      final VirtualFile packagesDir = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild(PACKAGES_FOLDER_NAME);
      final PsiDirectory psiDirectory = packagesDir == null ? null : getElement().getManager().findDirectory(packagesDir);
      if (psiDirectory != null) {
        return ArrayUtil.append(superVariants, psiDirectory);
      }
    }

    if (getIndex() == 1 && PACKAGES_FOLDER_NAME.equals(getFileReferenceSet().getReference(0).getText())) {
      final Collection<Object> result = new ArrayList<>(myDartResolver.getLivePackageNames());
      if (!result.isEmpty()) {
        Collections.addAll(result, superVariants);
        return ArrayUtil.toObjectArray(result);
      }
    }

    return superVariants;
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element, final boolean absolute) throws IncorrectOperationException {
    final String path = getFileReferenceSet().getPathString();

    if (path.startsWith(PACKAGES_FOLDER_NAME + "/") || path.contains("/" + PACKAGES_FOLDER_NAME + "/")) {
      final VirtualFile contextFile = DartResolveUtil.getRealVirtualFile(getElement().getContainingFile());
      final VirtualFile targetFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());

      if (contextFile != null && targetFile != null) {
        final DartUrlResolver urlResolver = DartUrlResolver.getInstance(element.getProject(), contextFile);
        final String newUrl = urlResolver.getDartUrlForFile(targetFile);

        if (newUrl.startsWith(PACKAGE_PREFIX)) {
          final int index = path.startsWith(PACKAGES_FOLDER_NAME + "/") ? 0 : (path.indexOf("/" + PACKAGES_FOLDER_NAME + "/") + 1);
          final String newName = path.substring(0, index) + PACKAGES_FOLDER_NAME + "/" + newUrl.substring(PACKAGE_PREFIX.length());
          return rename(newName);
        }
      }
    }

    return super.bindToElement(element, absolute);
  }
}
