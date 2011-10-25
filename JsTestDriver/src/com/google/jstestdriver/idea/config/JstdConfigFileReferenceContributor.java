package com.google.jstestdriver.idea.config;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.CastUtils;
import com.google.jstestdriver.idea.util.PsiElementFragment;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
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
    registrar.registerReferenceProvider(JstdConfigFileUtils.CONFIG_FILE_ELEMENT_PATTERN, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        final YAMLKeyValue keyValue = CastUtils.tryCast(element, YAMLKeyValue.class);
        if (keyValue == null) {
          return PsiReference.EMPTY_ARRAY;
        }
        final YAMLDocument yamlDocument = CastUtils.tryCast(keyValue.getParent(), YAMLDocument.class);
        if (yamlDocument == null) {
          return PsiReference.EMPTY_ARRAY;
        }
        final BasePathInfo basePathInfo = new BasePathInfo(yamlDocument);
        if (BasePathInfo.isBasePathKey(keyValue)) {
          PsiReference basePathRef = createBasePathRef(basePathInfo);
          if (basePathRef != null) {
            return new PsiReference[] {basePathRef};
          }
        } else if (JstdConfigFileUtils.isTopLevelKeyWithInnerFileSequence(keyValue)) {
          VirtualFile basePath = basePathInfo.getBasePath();
          if (basePath != null) {
            List<PsiReference> references = Lists.newArrayList();
            addReferencesForKeyValueWithInnerFileSequence(basePathInfo, keyValue, references);
            return references.toArray(new PsiReference[references.size()]);
          }
        }
        return PsiReference.EMPTY_ARRAY;
    }
    });
  }

  @Nullable
  private static PsiReference createBasePathRef(@NotNull BasePathInfo basePathInfo) {
    DocumentFragment documentFragment = basePathInfo.getValueAsDocumentFragment();
    YAMLKeyValue keyValue = basePathInfo.getKeyValue();
    if (documentFragment != null && keyValue != null) {
      PsiElementFragment<YAMLKeyValue> keyValueFragment = PsiElementFragment.create(keyValue, documentFragment);
      if (keyValueFragment != null) {
        return new MyPsiReference(keyValueFragment, basePathInfo, ".");
      }
    }
    return null;
  }

  private static void addReferencesForKeyValueWithInnerFileSequence(@NotNull final BasePathInfo basePathInfo,
                                                                    @NotNull final YAMLKeyValue keyValue,
                                                                    @NotNull final List<PsiReference> references) {
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
                PsiReference ref = getReferenceBySequence(basePathInfo, keyValue, yamlSequence);
                if (ref != null) {
                  references.add(ref);
                }
              }
            }
          });
        }
      }
    });
  }

  @Nullable
  private static PsiReference getReferenceBySequence(@NotNull BasePathInfo basePathInfo,
                                                     @NotNull YAMLKeyValue keyValue,
                                                     @NotNull YAMLSequence sequence) {
    PsiElementFragment<YAMLSequence> sequenceFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (sequenceFragment != null) {
      String text = sequenceFragment.getText().trim();
      String relativePath = FileUtil.toSystemIndependentName(text);
      PsiElementFragment<YAMLKeyValue> keyValueFragment = sequenceFragment.getSameTextRangeForParent(keyValue);
      return new MyPsiReference(keyValueFragment, basePathInfo, relativePath);
    }
    return null;
  }

  private static class MyPsiReference implements PsiReference {

    private final PsiElementFragment<YAMLKeyValue> myYamlDocumentFragment;
    private final BasePathInfo myBasePathInfo;
    private final String myRelativePath;

    private MyPsiReference(@NotNull PsiElementFragment<YAMLKeyValue> yamlDocumentFragment,
                           @NotNull BasePathInfo basePathInfo,
                           @NotNull String relativePath) {
      myYamlDocumentFragment = yamlDocumentFragment;
      myBasePathInfo = basePathInfo;
      myRelativePath = relativePath;
    }

    @Override
    public PsiElement getElement() {
      return myYamlDocumentFragment.getElement();
    }

    @Override
    public TextRange getRangeInElement() {
      return myYamlDocumentFragment.getTextRangeInElement();
    }

    @Override
    public PsiElement resolve() {
      VirtualFile targetVirtualFile = myBasePathInfo.findFile(myRelativePath);
      if (targetVirtualFile != null && targetVirtualFile.isValid()) {
        Project project = myYamlDocumentFragment.getElement().getProject();
        PsiManager psiManager = PsiManager.getInstance(project);
        if (targetVirtualFile.isDirectory()) {
          PsiDirectory targetPsiDirectory = psiManager.findDirectory(targetVirtualFile);
          if (targetPsiDirectory != null && targetPsiDirectory.isValid()) {
            return targetPsiDirectory;
          }
        } else {
          final PsiFile targetPsiFile = psiManager.findFile(targetVirtualFile);
          if (targetPsiFile != null && targetPsiFile.isValid()) {
            return targetPsiFile;
          }
        }
      }
      return null;
    }

    @NotNull
    @Override
    public String getCanonicalText() {
      return myYamlDocumentFragment.getText();
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
