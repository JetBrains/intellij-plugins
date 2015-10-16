package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.DartFile;
import com.jetbrains.lang.dart.psi.DartImportStatement;
import com.jetbrains.lang.dart.psi.DartUriBasedDirective;
import com.jetbrains.lang.dart.psi.DartUriElement;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class DartUriElementBase extends DartPsiCompositeElementImpl implements DartUriElement {

  private static final Condition<PsiFileSystemItem> DART_FILE_OR_DIR_FILTER = new Condition<PsiFileSystemItem>() {
    @Override
    public boolean value(final PsiFileSystemItem item) {
      return item.isDirectory() || item instanceof DartFile;
    }
  };

  public DartUriElementBase(@NotNull final ASTNode node) {
    super(node);
  }

  @Nullable
  @Override
  public PsiReference getReference() {
    final PsiReference[] references = getReferences();
    return references.length == 0 ? null : references[0];
  }

  @NotNull
  @Override
  public PsiReference[] getReferences() {
    final PsiElement parent = getParent();
    assert parent instanceof DartUriBasedDirective : parent;

    final String uri = ((DartUriBasedDirective)parent).getUriString();
    final int uriOffset = ((DartUriBasedDirective)parent).getUriStringOffset();

    if (DartResolver.isServerDrivenResolution()) {
      return new PsiReference[]{new DartFileReference(this, uri)};
    }

    if (uri.startsWith(DartUrlResolver.DART_PREFIX)) {
      return new PsiReference[]{new DartSdkLibReference(this, uri)};
    }

    if (uri.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
      final VirtualFile file = DartResolveUtil.getRealVirtualFile(getContainingFile());
      if (file == null) return PsiReference.EMPTY_ARRAY;

      final DartUrlResolver dartUrlResolver = DartUrlResolver.getInstance(getProject(), file);

      final int slashIndex = uri.indexOf('/');
      if (slashIndex > 0) {
        final String packageName = uri.substring(DartUrlResolver.PACKAGE_PREFIX.length(), slashIndex);
        final String pathAfterPackageName = uri.substring(slashIndex + 1);
        final VirtualFile packageDir = dartUrlResolver.getPackageDirIfNotInOldStylePackagesFolder(packageName, pathAfterPackageName);
        if (packageDir != null) {
          return getPackageReferences(file, packageDir, pathAfterPackageName, uriOffset + slashIndex + 1);
        }
      }

      final String relPath = uri.substring(DartUrlResolver.PACKAGE_PREFIX.length());
      final VirtualFile packageRoot = dartUrlResolver.getPackageRoot();
      if (packageRoot != null) {
        return getPackageReferences(file, packageRoot, relPath, uriOffset + DartUrlResolver.PACKAGE_PREFIX.length());
      }

      return PsiReference.EMPTY_ARRAY;
    }

    final PsiFile containingFile = getContainingFile().getOriginalFile();

    final FileReferenceSet referenceSet =
      new FileReferenceSet(uri, this, uriOffset, null, false, true, new FileType[]{DartFileType.INSTANCE}) {
        @Override
        protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
          return new Condition<PsiFileSystemItem>() {
            @Override
            public boolean value(final PsiFileSystemItem item) {
              return item.isDirectory() && !DartUrlResolver.PACKAGES_FOLDER_NAME.equals(item.getName()) ||
                     item instanceof DartFile && item != containingFile;
            }
          };
        }

        @Override
        public FileReference createFileReference(final TextRange range, final int index, final String text) {
          return new FileReference(this, range, index, text) {
            @NotNull
            @Override
            public Object[] getVariants() {
              return EMPTY_ARRAY; // completion comes fromDAS

              /*
              final Object[] superVariants = super.getVariants();

              if (getIndex() == 0) {
                final Object[] sdkVariants = DartSdkLibReference.getSdkLibUrisAsCompletionVariants(DartUriElementBase.this);
                final Object[] result = new Object[1 + superVariants.length + sdkVariants.length];
                result[0] = DartUrlResolver.PACKAGE_PREFIX;
                System.arraycopy(sdkVariants, 0, result, 1, sdkVariants.length);
                System.arraycopy(superVariants, 0, result, 1 + sdkVariants.length, superVariants.length);
                return result;
              }

              return superVariants;
              */
            }
          };
        }
      };

    return referenceSet.getAllReferences();
  }

  @NotNull
  private PsiReference[] getPackageReferences(final @NotNull VirtualFile contextFile,
                                              final @Nullable VirtualFile packagesFolder,
                                              final @NotNull String relPathFromPackagesFolderToReferencedFile,
                                              final int startOffset) {
    final VirtualFile parentFile = contextFile.getParent();
    if (packagesFolder == null || parentFile == null) return PsiReference.EMPTY_ARRAY;

    String relPathFromContextFileToPackagesFolder = FileUtil.getRelativePath(parentFile.getPath(), packagesFolder.getPath(), '/');
    if (relPathFromContextFileToPackagesFolder == null) return PsiReference.EMPTY_ARRAY;

    relPathFromContextFileToPackagesFolder += "/";
    final String str = relPathFromContextFileToPackagesFolder + relPathFromPackagesFolderToReferencedFile;

    final int nestedLevel = StringUtil.countChars(relPathFromContextFileToPackagesFolder, '/');
    final int shift = startOffset - relPathFromContextFileToPackagesFolder.length();

    final FileReferenceSet referenceSet = new FileReferenceSet(str, this, 0, null, false, true, new FileType[]{DartFileType.INSTANCE}) {
      @Override
      protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return DART_FILE_OR_DIR_FILTER;
      }

      @Override
      public FileReference createFileReference(TextRange range, int index, String text) {
        if (index < nestedLevel) {
          return super.createFileReference(range, index, text); // will be ignored anyway few lines below
        }

        return new FileReference(this, range.shiftRight(shift), index, text) {

          @NotNull
          @Override
          public Object[] getVariants() {
            return EMPTY_ARRAY; // completion comes fromDAS
          }

          @Override
          public PsiElement bindToElement(@NotNull final PsiElement element, final boolean absolute) {
            final VirtualFile contextFile = DartResolveUtil.getRealVirtualFile(getElement().getContainingFile());
            final VirtualFile targetFile = DartResolveUtil.getRealVirtualFile(element.getContainingFile());
            if (contextFile != null && targetFile != null) {
              final String newUri = DartUrlResolver.getInstance(element.getProject(), contextFile).getDartUrlForFile(targetFile);
              if (newUri.startsWith(DartUrlResolver.PACKAGE_PREFIX)) {
                return rename(newUri);
              }
            }
            return getElement();
          }
        };
      }
    };

    final FileReference[] references = referenceSet.getAllReferences();
    return references.length < nestedLevel ? PsiReference.EMPTY_ARRAY : Arrays.copyOfRange(references, nestedLevel, references.length);
  }

  public static class DartUriElementManipulator extends AbstractElementManipulator<DartUriElement> {
    @Override
    public DartUriElement handleContentChange(@NotNull final DartUriElement oldUriElement,
                                              @NotNull final TextRange range,
                                              @NotNull final String newContent) {
      final String newUriElementText = StringUtil.replaceSubstring(oldUriElement.getText(), getRangeInElement(oldUriElement), newContent);
      final PsiFile fileFromText = PsiFileFactory.getInstance(oldUriElement.getProject())
        .createFileFromText(DartLanguage.INSTANCE, "import " + newUriElementText + ";");

      final DartImportStatement importStatement = PsiTreeUtil.findChildOfType(fileFromText, DartImportStatement.class);
      assert importStatement != null : fileFromText.getText();

      return (DartUriElement)oldUriElement.replace(importStatement.getUriElement());
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull final DartUriElement element) {
      final PsiElement parent = element.getParent();
      assert parent instanceof DartUriBasedDirective : parent;

      final String uri = ((DartUriBasedDirective)parent).getUriString();
      final int uriOffset = element.getText().indexOf(uri);
      return TextRange.create(uriOffset, uriOffset + uri.length());
    }
  }
}
