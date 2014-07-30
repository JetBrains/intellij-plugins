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
import com.intellij.util.ArrayUtil;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

/**
 * Resolves path in <code>&lt;script src="packages/browser/dart.js"/&gt;</code> to base Dart <code>packages</code> folder because relative symlinked <code>packages</code> folder is excluded.<br/>
 * Another example: <code>&lt;link rel="import" href="packages/click_counter/click_counter.html"&gt;</code> is resolved to ./lib/click_counter.html if 'click_counter' is a Dart project name in pubspec.yaml
 */
public class DartPackagePathReferenceProvider implements PathReferenceProvider {

  @Override
  @Nullable
  public PathReference getPathReference(@NotNull final String path, @NotNull final PsiElement element) {
    return null;
  }

  @Override
  public boolean createReferences(@NotNull final PsiElement psiElement, final @NotNull List<PsiReference> references, final boolean soft) {
    if (!(psiElement instanceof XmlAttributeValue) || !HtmlUtil.isHtmlFile(psiElement.getContainingFile())) return false;

    final PsiElement parent = psiElement.getParent();
    if (!(parent instanceof XmlAttribute)) return false;

    final XmlTag tag = ((XmlAttribute)parent).getParent();
    if (tag == null) return false;

    final VirtualFile file = DartResolveUtil.getRealVirtualFile(psiElement.getContainingFile());
    if (file == null) return false;

    // What are other cases of file references in HTML? May be we should always provide Dart references, not only in <script/> and <link/>?
    if (HtmlUtil.isScriptTag(tag) && "src".equalsIgnoreCase(((XmlAttribute)parent).getName()) ||
        tag.getLocalName().equalsIgnoreCase("link") && "href".equalsIgnoreCase(((XmlAttribute)parent).getName())) {
      final DartUrlResolver dartResolver = DartUrlResolver.getInstance(psiElement.getProject(), file);
      if (dartResolver.getPubspecYamlFile() == null && dartResolver.getPackageRoots().length == 0) {
        return false; // no Dart at all
      }

      return Collections.addAll(references, getDartPackageReferences(psiElement, dartResolver));
    }

    return false;
  }

  private static FileReference[] getDartPackageReferences(@NotNull final PsiElement psiElement,
                                                          @NotNull final DartUrlResolver dartResolver) {
    final TextRange textRange = ElementManipulators.getValueTextRange(psiElement);
    final String referenceText = psiElement.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());

    final FileReferenceSet referenceSet = new FileReferenceSet(referenceText, psiElement, textRange.getStartOffset(), null, true) {
      public FileReference createFileReference(final TextRange range, final int index, final String text) {
        return new DartPackageAwareFileReference(this, range, index, text, dartResolver);
      }
    };

    return referenceSet.getAllReferences();
  }

  private static class DartPackageAwareFileReference extends FileReference {
    @NotNull private final DartUrlResolver myDartResolver;

    public DartPackageAwareFileReference(@NotNull final FileReferenceSet fileReferenceSet,
                                         final TextRange range,
                                         final int index,
                                         final String text,
                                         @NotNull final DartUrlResolver dartResolver) {
      super(fileReferenceSet, range, index, text);
      myDartResolver = dartResolver;
    }

    @NotNull
    protected ResolveResult[] innerResolve(final boolean caseSensitive, @NotNull final PsiFile containingFile) {
      if (getIndex() == 0 && PACKAGES_FOLDER_NAME.equals(getText())) {
        final VirtualFile pubspecYamlFile = myDartResolver.getPubspecYamlFile();
        final VirtualFile packagesDir = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild(PACKAGES_FOLDER_NAME);
        final PsiDirectory psiDirectory = packagesDir == null ? null : containingFile.getManager().findDirectory(packagesDir);
        if (psiDirectory != null) {
          return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
        }
      }

      if (getIndex() == 1 && PACKAGES_FOLDER_NAME.equals(getFileReferenceSet().getReference(0).getText())) {
        final VirtualFile packageDir = myDartResolver.getPackageDirIfLivePackageOrFromPubListPackageDirs(getText());
        final PsiDirectory psiDirectory = packageDir == null ? null : containingFile.getManager().findDirectory(packageDir);
        if (psiDirectory != null) {
          return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
        }
      }

      return super.innerResolve(caseSensitive, containingFile);
    }

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
        final Collection<Object> result = new ArrayList<Object>(myDartResolver.getLivePackageNames());
        if (!result.isEmpty()) {
          Collections.addAll(result, superVariants);
          return ArrayUtil.toObjectArray(result);
        }
      }

      return superVariants;
    }
  }
}