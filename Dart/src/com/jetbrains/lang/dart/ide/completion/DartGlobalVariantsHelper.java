package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.DartTokenTypesSets;
import com.jetbrains.lang.dart.ide.index.DartComponentIndex;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Created by fedorkorotkov.
 */
public class DartGlobalVariantsHelper {
  private DartGlobalVariantsHelper() {
  }

  public static void addAdditionalGlobalVariants(final CompletionResultSet result,
                                                 @NotNull PsiElement context,
                                                 Set<DartComponentName> variants,
                                                 @Nullable final Condition<DartComponentInfo> infoFilter) {
    final List<String> addedNames = ContainerUtil.skipNulls(ContainerUtil.mapNotNull(variants, new Function<DartComponentName, String>() {
      @Override
      public String fun(DartComponentName name) {
        return name.getName();
      }
    }));
    DartComponentIndex.processAllComponents(
      context,
      new PairProcessor<String, DartComponentInfo>() {
        @Override
        public boolean process(String componentName, DartComponentInfo info) {
          if (infoFilter == null || !infoFilter.value(info)) {
            result.addElement(buildElement(componentName, info));
          }
          return true;
        }
      }, new Condition<String>() {
        @Override
        public boolean value(String componentName) {
          return addedNames.contains(componentName);
        }
      }
    );
  }

  @NotNull
  private static LookupElement buildElement(String componentName, DartComponentInfo info) {
    LookupElementBuilder builder = LookupElementBuilder.create(info, componentName);
    if (info.getLibraryId() != null) {
      builder = builder.withTailText(info.getLibraryId(), true);
    }
    if (info.getType() != null) {
      builder = builder.withIcon(info.getType().getIcon());
    }
    return builder.withInsertHandler(MY_INSERT_HANDLER);
  }

  private static final InsertHandler<LookupElement> MY_INSERT_HANDLER = new InsertHandler<LookupElement>() {
    @Override
    public void handleInsert(InsertionContext context, LookupElement item) {
      DartComponentInfo info = (DartComponentInfo)item.getObject();
      final String libraryId = info.getLibraryId();
      if (libraryId == null) {
        return;
      }
      final PsiElement at = context.getFile().findElementAt(context.getStartOffset());
      final DartReference dartReference = PsiTreeUtil.getParentOfType(at, DartReference.class);
      if (dartReference != null && dartReference.resolve() == null) {
        insertImport(at.getContainingFile(), item.getLookupString(), libraryId);
      }
    }
  };

  private static void insertImport(@NotNull PsiFile context, @Nls String componentName, @NotNull String libraryId) {
    final PsiManager psiManager = context.getManager();
    libraryRootLoop:
    for (VirtualFile libraryRoot : DartResolveUtil.findLibrary(context)) {
      final PsiFile file = psiManager.findFile(libraryRoot);
      if (file == null) {
        continue;
      }
      final DartImportStatement[] importStatements = PsiTreeUtil.getChildrenOfType(file, DartImportStatement.class);
      if (importStatements != null) {
        for (DartImportStatement importStatement : importStatements) {
          final PsiElement importTarget = importStatement.getPathOrLibraryReference().resolve();
          if (importTarget != null && DartResolver.resolveSimpleReference(importTarget, componentName) != null) {
            addShowOrRemoveHide(importStatement, componentName);
            continue libraryRootLoop;
          }
        }
      }

      final PsiElement toAdd = DartElementGenerator.createTopLevelStatementFromText(file.getProject(), "import '" + libraryId + "';");
      if (toAdd != null) {
        final PsiElement anchor = findAnchorForImportStatement(file, importStatements);
        if (anchor == null) {
          file.addBefore(toAdd, file.getFirstChild());
        }
        else {
          file.addAfter(toAdd, anchor);
        }
      }
    }
  }

  private static void addShowOrRemoveHide(@NotNull DartImportStatement importStatement, String componentName) {
    // try to remove hide
    for (DartHideCombinator hideCombinator : importStatement.getHideCombinatorList()) {
      final List<DartLibraryComponentReferenceExpression> libraryComponents =
        hideCombinator.getLibraryReferenceList().getLibraryComponentReferenceExpressionList();
      for (DartLibraryComponentReferenceExpression libraryComponentReferenceExpression : libraryComponents) {
        if (componentName.equals(libraryComponentReferenceExpression.getText())) {
          final PsiElement toRemove = libraryComponents.size() > 1 ?
                                      libraryComponentReferenceExpression :
                                      hideCombinator;
          toRemove.delete();
          return;
        }
      }
    }

    // add show
    final List<DartShowCombinator> showCombinators = importStatement.getShowCombinatorList();
    if (showCombinators.isEmpty()) {
      // something wrong
      return;
    }
    final DartShowCombinator combinatoroToAdd = showCombinators.iterator().next();
    final DartLibraryComponentReferenceExpression libraryComponentReference =
      DartElementGenerator.createLibraryComponentReference(importStatement.getProject(), componentName);
    if (libraryComponentReference != null) {
      combinatoroToAdd.getLibraryReferenceList().getNode().addLeaf(DartTokenTypes.COMMA, ",", null);
      combinatoroToAdd.getLibraryReferenceList().getNode().addLeaf(DartTokenTypesSets.WHITE_SPACE, " ", null);
      combinatoroToAdd.getLibraryReferenceList().add(libraryComponentReference);
    }
  }

  private static PsiElement findAnchorForImportStatement(@NotNull PsiFile psiFile,
                                                         @Nullable DartImportStatement[] importStatements) {
    if (importStatements != null && importStatements.length > 0) {
      return importStatements[importStatements.length - 1];
    }
    return PsiTreeUtil.getChildOfType(psiFile, DartLibraryStatement.class);
  }
}
