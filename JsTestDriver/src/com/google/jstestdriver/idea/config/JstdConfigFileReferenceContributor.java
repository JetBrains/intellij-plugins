package com.google.jstestdriver.idea.config;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.PsiElementFragment;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdConfigFileReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    ElementPattern<PsiElement> place = JstdConfigFileUtils.CONFIG_FILE_ELEMENT_PATTERN.and(PlatformPatterns.psiElement(YAMLKeyValue.class));
    registrar.registerReferenceProvider(place, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        YAMLKeyValue keyValue = CastUtils.tryCast(element, YAMLKeyValue.class);
        if (keyValue != null && JstdConfigFileUtils.isKeyWithInnerFileSequence(keyValue)) {
          YAMLDocument yamlDocument = getDocumentByKeyValueElement(keyValue);
          if (yamlDocument != null) {
            VirtualFile basePath = JstdConfigFileUtils.extractBasePath(yamlDocument);
            if (basePath != null) {
              return findAllReferencesForKeyValue(basePath, keyValue);
            }
          }
        }
        return PsiReference.EMPTY_ARRAY;
      }
    });
  }

  @NotNull
  private static PsiReference[] findAllReferencesForKeyValue(@NotNull final VirtualFile basePath, @NotNull final YAMLKeyValue keyValue) {
    final List<PsiReference> references = Lists.newArrayList();
    keyValue.acceptChildren(new PsiElementVisitor() {
      @Override
      public void visitElement(PsiElement element) {
        YAMLCompoundValue compoundValue = CastUtils.tryCast(element, YAMLCompoundValue.class);
        if (compoundValue != null) {
          compoundValue.acceptChildren(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
              YAMLSequence yamlSequence = CastUtils.tryCast(element, YAMLSequence.class);
              if (yamlSequence != null) {
                PsiReference ref = getReferenceBySequence(basePath, yamlSequence, keyValue);
                if (ref != null) {
                  references.add(ref);
                }
              }
            }
          });
        }
      }
    });
    return references.toArray(new PsiReference[references.size()]);
  }

  @Nullable
  private static PsiReference getReferenceBySequence(@NotNull final VirtualFile basePath,
                                                     @NotNull YAMLSequence sequence,
                                                     @NotNull YAMLKeyValue keyValue) {
    PsiElementFragment<YAMLSequence> sequenceFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (sequenceFragment != null) {
      String text = sequenceFragment.getText();
      String relativePath = FileUtil.toSystemIndependentName(text);
      PsiElementFragment<YAMLKeyValue> keyValueFragment = sequenceFragment.getSameTextRangeForParent(keyValue);
      return new MyReference(keyValueFragment.getElement(), keyValueFragment.getTextRangeInElement(), basePath, relativePath);
    }
    return null;
  }

  private static YAMLDocument getDocumentByKeyValueElement(@NotNull YAMLKeyValue keyValue) {
    return JstdConfigFileUtils.getVerifiedHierarchyHead(
      keyValue,
      new Class[]{
        YAMLKeyValue.class
      },
      YAMLDocument.class
    );
  }

  private static class MyReference implements PsiReference {

    private final YAMLKeyValue myKeyValue;
    private final TextRange myTextRangeInSequence;

    private final VirtualFile myBasePath;
    private final String myRelativePath;

    private MyReference(@NotNull YAMLKeyValue keyValue, @NotNull TextRange textRangeInSequence, @NotNull VirtualFile basePath, @NotNull String relativePath) {
      myKeyValue = keyValue;
      myTextRangeInSequence = textRangeInSequence;
      myBasePath = basePath;
      myRelativePath = relativePath;
    }

    @Override
    public PsiElement getElement() {
      return myKeyValue;
    }

    @Override
    public TextRange getRangeInElement() {
      return myTextRangeInSequence;
    }

    @Override
    public PsiElement resolve() {
      VirtualFile targetVirtualFile = myBasePath.findFileByRelativePath(myRelativePath);
      if (targetVirtualFile != null) {
        final PsiFile targetPsiFile = PsiManager.getInstance(myKeyValue.getProject()).findFile(targetVirtualFile);
        if (targetPsiFile != null && targetPsiFile.isValid()) {
          return targetPsiFile;
        }
      }
      return null;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
      return myTextRangeInSequence.substring(myKeyValue.getText());
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      throw new IncorrectOperationException();
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
      return getElement().getManager().areElementsEquivalent(resolve(), element);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public boolean isSoft() {
      return false;
    }
  }
}
