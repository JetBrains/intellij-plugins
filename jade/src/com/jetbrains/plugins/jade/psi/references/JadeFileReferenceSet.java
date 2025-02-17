package com.jetbrains.plugins.jade.psi.references;

import com.intellij.analysis.AnalysisBundle;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.IndexingBundle;
import com.jetbrains.plugins.jade.JadeToPugTransitionHelper;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import com.jetbrains.plugins.jade.psi.JadeTokenTypes;
import com.jetbrains.plugins.jade.psi.impl.JadeFilePathImpl;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class JadeFileReferenceSet extends FileReferenceSet {
  private final boolean myAllFiles;
  private final @NotNull String myExtension;

  public JadeFileReferenceSet(final @NotNull JadeFilePathImpl element) {
    super(element);
    myAllFiles = element.getParent().getFirstChild().getNode().getElementType() == JadeTokenTypes.INCLUDE_KEYWORD;
    if (JadeToPugTransitionHelper.isPugElement(element)) {
      myExtension = "pug";
    }
    else {
      myExtension = "jade";
    }
  }

  @Override
  protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
    final FileTypeManager manager = FileTypeManager.getInstance();
    return psiFileSystemItem -> {
      if (psiFileSystemItem.isDirectory()) {
        return true;
      }
      if (getElement().getContainingFile().getOriginalFile().equals(psiFileSystemItem)) {
        return false;
      }
      return myAllFiles || manager.getFileTypeByFileName(psiFileSystemItem.getName()) instanceof JadeFileType;
    };
  }

  @Override
  public FileReference createFileReference(final TextRange range, final int index, final String text) {
    return new FileReference(this, range, index, text) {
      @Override
      protected Object createLookupItem(final PsiElement candidate) {
        if (!(candidate instanceof PsiFile file) || !(candidate.isPhysical())) {
          return candidate;
        }
        String name = file.getName();
        String lookupString = FileUtilRt.extensionEquals(name, myExtension)
                              ? FileUtilRt.getNameWithoutExtension(name)
                              : name;
        return LookupElementBuilder.create(file, lookupString)
          .withIcon(file.getIcon(0))
          .withPresentableText(name);
      }

      private @NlsSafe String patchText(String text) {
        if (!isLast()) {
          return text;
        }
        String extension = FileUtilRt.getExtension(text);
        if (myAllFiles && !extension.isEmpty()) {
          return text;
        }
        if (ContainerUtil.find(JadeToPugTransitionHelper.ALL_EXTENSIONS,
                               name -> FileUtil.namesEqual(name, extension)) == null) {
          return text + "." + myExtension;
        }
        return text;
      }

      @Override
      protected void innerResolveInContext(final @NotNull String text,
                                           final @NotNull PsiFileSystemItem context,
                                           final @NotNull Collection<? super ResolveResult> result,
                                           final boolean caseSensitive) {
        super.innerResolveInContext(patchText(text), context, result, caseSensitive);
      }

      @Override
      public @NotNull String getFileNameToCreate() {
        return patchText(getCanonicalText());
      }

      @Override
      public @NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String getUnresolvedMessagePattern() {
        String text = StringUtil.escapePattern(decode(getCanonicalText()));
        return AnalysisBundle.message("error.cannot.resolve.file.or.dir",
                                      IndexingBundle.message(isLast() ? "terms.file" : "terms.directory"),
                                      patchText(text));
      }
    };
  }
}
