package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.paths.PathReferenceProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Resolves path in <code>&lt;script src="packages/browser/dart.js"/&gt;</code> to base Dart <code>packages</code> folder because relative symlinked <code>packages</code> folder is excluded.
 */
public class HtmlScriptSrcToDartPackagesPathReferenceProvider implements PathReferenceProvider {

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement, final @NotNull List<PsiReference> references, final boolean soft) {
    if (!(psiElement instanceof XmlAttributeValue)) return false;

    final PsiElement parent = psiElement.getParent();
    if (!(parent instanceof XmlAttribute) || !"src".equalsIgnoreCase(((XmlAttribute)parent).getName())) return false;

    final XmlTag tag = (XmlTag)parent.getParent();
    if (tag == null || !HtmlUtil.isScriptTag(tag)) return false;

    final TextRange range = ElementManipulators.getValueTextRange(psiElement);
    int offset = range.getStartOffset();
    int endOffset = range.getEndOffset();
    final String elementText = psiElement.getText();
    final String text = elementText.substring(offset, endOffset);
    if (!text.trim().startsWith("packages/")) return false;

    FileReferenceSet set = new FileReferenceSet(text, psiElement, offset, null, true, false, null);

    set.addCustomization(FileReferenceSet.DEFAULT_PATH_EVALUATOR_OPTION, new Function<PsiFile, Collection<PsiFileSystemItem>>() {
      @Override
      public Collection<PsiFileSystemItem> fun(final PsiFile psiFile) {
        final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiFile);
        final VirtualFile packagesFolder = file == null ? null : PubspecYamlUtil.getDartPackagesFolder(psiFile.getProject(), file);
        final VirtualFile parentFolder = packagesFolder == null ? null : packagesFolder.getParent();

        final PsiFileSystemItem psiDirectory = parentFolder == null
                                               ? null
                                               : PsiManager.getInstance(psiFile.getProject()).findDirectory(parentFolder);
        return psiDirectory != null ? Collections.singletonList(psiDirectory) : null;
      }
    });

    final FileReference[] allReferences = set.getAllReferences();
    Collections.addAll(references, allReferences);
    return allReferences.length > 0;
  }

  @Override
  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return null;
  }
}
