// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

/**
 * Resolves path in {@code <script src="packages/browser/dart.js"/>} to base Dart {@code packages} folder because relative symlinked {@code packages} folder is excluded.<br/>
 * Another example: {@code <link rel="import" href="packages/click_counter/click_counter.html">} is resolved to ./lib/click_counter.html if 'click_counter' is a Dart project name in pubspec.yaml
 */
public class DartPackagePathReferenceProvider extends PsiReferenceProvider {

  public static ElementFilter getFilter() {
    return new ElementFilter() {
      @Override
      public boolean isAcceptable(Object _element, PsiElement context) {
        if (!(_element instanceof PsiElement)) return false;
        final PsiElement element = (PsiElement)_element;
        final PsiElement parentElement = element.getParent();
        final PsiFile file = element.getContainingFile().getOriginalFile();
        final VirtualFile vFile = file.getVirtualFile();

        return vFile != null &&
               HtmlUtil.hasHtml(file) &&
               parentElement instanceof XmlAttribute &&
               canContainDartPackageReference(((XmlAttribute)parentElement).getParent().getLocalName(),
                                              ((XmlAttribute)parentElement).getName()) &&
               PubspecYamlUtil.findPubspecYamlFile(element.getProject(), vFile) != null;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    };
  }

  private static boolean canContainDartPackageReference(@Nullable final String tagName, @Nullable final String attrName) {
    return ("link".equalsIgnoreCase(tagName) && "href".equalsIgnoreCase(attrName)) ||
           ("script".equalsIgnoreCase(tagName) && "src".equalsIgnoreCase(attrName)) ||
           ("img".equalsIgnoreCase(tagName) && "src".equalsIgnoreCase(attrName));
  }

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext context) {
    if (!(psiElement instanceof XmlAttributeValue) || !HtmlUtil.isHtmlFile(psiElement.getContainingFile())) return PsiReference.EMPTY_ARRAY;

    final PsiElement parent = psiElement.getParent();
    if (!(parent instanceof XmlAttribute)) return PsiReference.EMPTY_ARRAY;

    final XmlTag tag = ((XmlAttribute)parent).getParent();
    if (tag == null) return PsiReference.EMPTY_ARRAY;

    final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiElement.getContainingFile());
    if (file == null) return PsiReference.EMPTY_ARRAY;

    if (!canContainDartPackageReference(tag.getName(), ((XmlAttribute)parent).getName())) return PsiReference.EMPTY_ARRAY;

    if (PubspecYamlUtil.findPubspecYamlFile(psiElement.getProject(), file) == null) return PsiReference.EMPTY_ARRAY;

    return getDartPackageReferences(psiElement, DartUrlResolver.getInstance(psiElement.getProject(), file));
  }

  private static FileReference[] getDartPackageReferences(@NotNull final PsiElement psiElement,
                                                          @NotNull final DartUrlResolver dartResolver) {
    final TextRange textRange = ElementManipulators.getValueTextRange(psiElement);
    final String referenceText = psiElement.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());

    if (!referenceText.trim().startsWith(PACKAGES_FOLDER_NAME + "/") && !referenceText.contains("/" + PACKAGES_FOLDER_NAME + "/")) {
      return FileReference.EMPTY;
    }

    final FileReferenceSet referenceSet = new FileReferenceSet(referenceText, psiElement, textRange.getStartOffset(), null, true) {
      public FileReference createFileReference(final TextRange range, final int index, final String text) {
        return new DartPackageAwareFileReference(this, range, index, text, dartResolver);
      }
    };

    return referenceSet.getAllReferences();
  }
}