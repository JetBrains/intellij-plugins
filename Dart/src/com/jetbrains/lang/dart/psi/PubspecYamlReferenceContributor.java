// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.OSAgnosticPathUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.ProcessingContext;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Collection;

public final class PubspecYamlReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(@NotNull final PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(PlatformPatterns.psiElement(YAMLKeyValue.class), new PubspecYamlReferenceProvider());
  }

  public static boolean isPathPackageDefinition(@NotNull final YAMLKeyValue element) {
    if (!PubspecYamlUtil.PUBSPEC_YAML.equals(element.getContainingFile().getName())) return false;
    if (!PubspecYamlUtil.PATH.equals(element.getKeyText())) return false;

    final PsiElement parent1 = element.getParent();
    final PsiElement parent2 = parent1 instanceof YAMLMapping ? parent1.getParent() : null;
    final String packageName = parent2 instanceof YAMLKeyValue ? ((YAMLKeyValue)parent2).getKeyText() : null;
    if (packageName == null) return false;

    final PsiElement parent3 = parent2.getParent();
    final PsiElement parent4 = parent3 instanceof YAMLMapping ? parent3.getParent() : null;
    return parent4 instanceof YAMLKeyValue &&
           parent4.getParent() instanceof YAMLMapping &&
           parent4.getParent().getParent() instanceof YAMLDocument &&
           (PubspecYamlUtil.DEPENDENCIES.equals(((YAMLKeyValue)parent4).getKeyText()) ||
            PubspecYamlUtil.DEV_DEPENDENCIES.equals(((YAMLKeyValue)parent4).getKeyText()) ||
            PubspecYamlUtil.DEPENDENCY_OVERRIDES.equals(((YAMLKeyValue)parent4).getKeyText()));
  }

  private static class PubspecYamlReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement element, @NotNull final ProcessingContext context) {
      if (!(element instanceof YAMLKeyValue) || !isPathPackageDefinition((YAMLKeyValue)element)) {
        return PsiReference.EMPTY_ARRAY;
      }

      final PsiElement value = ((YAMLKeyValue)element).getValue();
      if (value == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      final String text = StringUtil.trimTrailing(FileUtil.toSystemIndependentName(value.getText()));
      final boolean quoted = StringUtil.isQuotedString(text);
      final int startInElement = value.getStartOffsetInParent() + (quoted ? 1 : 0);

      final FileReferenceSet fileReferenceSet = new FileReferenceSet(StringUtil.unquoteString(text), element, startInElement,
                                                                     this, element.getContainingFile().getViewProvider().getVirtualFile().isCaseSensitive(), false) {
        @NotNull
        @Override
        public Collection<PsiFileSystemItem> computeDefaultContexts() {
          if (isAbsolutePathReference()) {
            final VirtualFile[] roots = ManagingFS.getInstance().getLocalRoots();
            final Collection<PsiFileSystemItem> result = new SmartList<>();
            for (VirtualFile root : roots) {
              ContainerUtil.addIfNotNull(result, element.getManager().findDirectory(root));
            }
            return result;
          }

          return super.computeDefaultContexts();
        }

        @Override
        public boolean isAbsolutePathReference() {
          final String path = getPathString();
          return SystemInfo.isWindows
                 ? OSAgnosticPathUtil.isAbsoluteDosPath(path)
                 : path.startsWith("/");
        }

        @Override
        protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
          return DIRECTORY_FILTER;
        }
      };

      return fileReferenceSet.getAllReferences();
    }
  }
}
