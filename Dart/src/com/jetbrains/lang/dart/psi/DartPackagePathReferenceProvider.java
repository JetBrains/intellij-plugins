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
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.xml.util.HtmlUtil;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static com.jetbrains.lang.dart.util.DartUrlResolver.PACKAGES_FOLDER_NAME;

/**
 * Resolves path in <code>&lt;script src="packages/browser/dart.js"/&gt;</code> to base Dart <code>packages</code> folder because relative symlinked <code>packages</code> folder is excluded.<br/>
 * Another example: <code>&lt;link rel="import" href="packages/click_counter/click_counter.html"&gt;</code> is resolved to ./lib/click_counter.html if 'click_counter' is a Dart project name in pubspec.yaml
 */
public class DartPackagePathReferenceProvider extends PsiReferenceProvider {
  public static ElementFilter getFilter() {
    return new ElementFilter() {
      @Override
      public boolean isAcceptable(Object _element, PsiElement context) {
        PsiElement element = (PsiElement)_element;
        PsiFile file = element.getContainingFile().getOriginalFile();

        if (HtmlUtil.hasHtml(file) && DartFileReferenceHelper.hasDart(file.getProject(), file.getVirtualFile())) {
          final PsiElement parent = element.getParent();

          if (parent instanceof XmlAttribute) {
            XmlAttribute xmlAttribute = (XmlAttribute)parent;
            @NonNls final String attrName = xmlAttribute.getName();
            XmlTag tag = xmlAttribute.getParent();
            @NonNls final String tagName = tag.getName();

            return isFileAttribute(attrName, tagName);
          }
        }
        return false;
      }

      @Override
      public boolean isClassAcceptable(Class hintClass) {
        return true;
      }
    };
  }

  public static boolean isFileAttribute(String attrName, String tagName) {
    return ("href".equals(attrName) || "src".equals(attrName)) &&
           ("link".equals(tagName) || "script".equals(tagName) || "img".equals(tagName));
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

    // What are other cases of file references in HTML? May be we should always provide Dart references, not only in <script/> and <link/>?
    if (isFileAttribute(((XmlAttribute)parent).getName(), tag.getName())) {
      return getDartPackageReferences(psiElement, DartUrlResolver.getInstance(psiElement.getProject(), file));
    }

    return PsiReference.EMPTY_ARRAY;
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
      if (PACKAGES_FOLDER_NAME.equals(getText())) {
        final VirtualFile pubspecYamlFile = myDartResolver.getPubspecYamlFile();
        final VirtualFile packagesDir = pubspecYamlFile == null ? null : pubspecYamlFile.getParent().findChild(PACKAGES_FOLDER_NAME);
        final PsiDirectory psiDirectory = packagesDir == null ? null : containingFile.getManager().findDirectory(packagesDir);
        if (psiDirectory != null) {
          return new ResolveResult[]{new PsiElementResolveResult(psiDirectory)};
        }
      }

      final int index = getIndex();
      if (index > 0 && PACKAGES_FOLDER_NAME.equals(getFileReferenceSet().getReference(index - 1).getText())) {
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