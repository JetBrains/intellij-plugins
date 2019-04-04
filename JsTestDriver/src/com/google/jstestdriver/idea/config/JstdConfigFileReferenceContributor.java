package com.google.jstestdriver.idea.config;

import com.google.common.collect.Lists;
import com.google.jstestdriver.idea.util.PsiElementFragment;
import com.intellij.openapi.editor.DocumentFragment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLCompoundValue;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequenceItem;

import java.util.List;

/**
 * @author Sergey Simonchik
 */
public class JstdConfigFileReferenceContributor extends PsiReferenceContributor {

  @Override
  public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(JstdConfigFileUtils.CONFIG_FILE_ELEMENT_PATTERN, new PsiReferenceProvider() {
      @NotNull
      @Override
      public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        final YAMLKeyValue keyValue = ObjectUtils.tryCast(element, YAMLKeyValue.class);
        if (keyValue == null) {
          return PsiReference.EMPTY_ARRAY;
        }
        final YAMLDocument yamlDocument = ObjectUtils.tryCast(keyValue.getParent(), YAMLDocument.class);
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
            return references.toArray(PsiReference.EMPTY_ARRAY);
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
        YAMLCompoundValue compoundValue = ObjectUtils.tryCast(element, YAMLCompoundValue.class);
        if (compoundValue != null) {
          compoundValue.acceptChildren(new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
              YAMLSequenceItem yamlSequenceItem = ObjectUtils.tryCast(element, YAMLSequenceItem.class);
              if (yamlSequenceItem != null) {
                PsiReference ref = getReferenceBySequence(basePathInfo, keyValue, yamlSequenceItem);
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
                                                     @NotNull YAMLSequenceItem sequence) {
    PsiElementFragment<YAMLSequenceItem> sequenceFragment = JstdConfigFileUtils.buildSequenceTextFragment(sequence);
    if (sequenceFragment != null) {
      String text = sequenceFragment.getText().trim();
      String relativePath = FileUtil.toSystemIndependentName(text);
      PsiElementFragment<YAMLKeyValue> keyValueFragment = sequenceFragment.getSameTextRangeForParent(keyValue);
      return new MyPsiReference(keyValueFragment, basePathInfo, relativePath);
    }
    return null;
  }

  private static class MyPsiReference implements PsiReference {

    private final PsiElementFragment<YAMLKeyValue> myYamlKeyValueFragment;
    private final BasePathInfo myBasePathInfo;
    private final String myRelativePath;

    private MyPsiReference(@NotNull PsiElementFragment<YAMLKeyValue> yamlKeyValueFragment,
                           @NotNull BasePathInfo basePathInfo,
                           @NotNull String relativePath) {
      myYamlKeyValueFragment = yamlKeyValueFragment;
      myBasePathInfo = basePathInfo;
      myRelativePath = relativePath;
    }

    @NotNull
    @Override
    public PsiElement getElement() {
      return myYamlKeyValueFragment.getElement();
    }

    @NotNull
    @Override
    public TextRange getRangeInElement() {
      return myYamlKeyValueFragment.getTextRangeInElement();
    }

    @Override
    public PsiElement resolve() {
      VirtualFile targetVirtualFile = myBasePathInfo.findFile(myRelativePath);
      if (targetVirtualFile != null && targetVirtualFile.isValid()) {
        Project project = myYamlKeyValueFragment.getElement().getProject();
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
      return myYamlKeyValueFragment.getText();
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
      TextRange fileNameTextRange = findFileNameTextRagne();
      YAMLKeyValue oldKeyValue = myYamlKeyValueFragment.getElement();
      String newKeyValueText = fileNameTextRange.replace(oldKeyValue.getText(), newElementName);
      YAMLKeyValue newKeyValue = createTempKeyValue(newKeyValueText);
      if (newKeyValue != null) {
        return oldKeyValue.replace(newKeyValue);
      }
      return null;
    }

    @NotNull
    private TextRange findFileNameTextRagne() {
      TextRange lastComponentTextRange = calcLastComponentTextRange(myYamlKeyValueFragment.getText());
      TextRange oldPathRange = myYamlKeyValueFragment.getTextRangeInElement();
      return oldPathRange.cutOut(lastComponentTextRange);
    }

    @NotNull
    private static TextRange calcLastComponentTextRange(@NotNull String path) {
      final int i1 = path.lastIndexOf(JstdConfigFileUtils.UNIX_PATH_SEPARATOR);
      final int i2 = path.lastIndexOf(JstdConfigFileUtils.WINDOWS_PATH_SEPARATOR);
      int resInd = i1;
      if (resInd == -1) {
        resInd = i2;
      } else if (i2 != -1) {
        resInd = Math.max(resInd, i2);
      }
      if (resInd != -1) {
        return new TextRange(resInd + 1, path.length());
      }
      return TextRange.allOf(path);
    }

    @Nullable
    private YAMLKeyValue createTempKeyValue(@NotNull String keyValueText) {
      Project project = myYamlKeyValueFragment.getElement().getProject();
      PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("temp", JstdConfigFileType.INSTANCE, keyValueText);
      return PsiTreeUtil.findChildOfType(psiFile, YAMLKeyValue.class);
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
      return myYamlKeyValueFragment.getElement();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
      return getElement().getManager().areElementsEquivalent(resolve(), element);
    }

    @Override
    public boolean isSoft() {
      return false;
    }
  }
}
