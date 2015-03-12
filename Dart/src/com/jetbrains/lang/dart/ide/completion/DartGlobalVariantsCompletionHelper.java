package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PairProcessor;
import com.jetbrains.lang.dart.ide.index.DartComponentIndex;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class DartGlobalVariantsCompletionHelper {
  private DartGlobalVariantsCompletionHelper() {
  }

  public static void addAdditionalGlobalVariants(@NotNull final CompletionResultSet result,
                                                 @NotNull final PsiElement context,
                                                 @NotNull final Set<String> namesToSkip,
                                                 @Nullable final Condition<DartComponentInfo> infoFilter) {
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
          return namesToSkip.contains(componentName);
        }
      }
    );
  }

  @NotNull
  private static LookupElement buildElement(String componentName, DartComponentInfo info) {
    LookupElementBuilder builder = LookupElementBuilder.create(info, componentName);
    if (info.getLibraryId() != null) {
      // todo may be show DartImportUtil.getUrlToImport() or file name where component declared instead of info.getLibraryId() ?
      builder = builder.withTailText(" (" + info.getLibraryId() + ")", true);
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
      final String libraryName = info.getLibraryId();
      if (libraryName == null) {
        return;
      }

      final PsiElement contextElement = context.getFile().findElementAt(context.getStartOffset());
      final DartReference dartReference = PsiTreeUtil.getParentOfType(contextElement, DartReference.class);
      if (dartReference != null && dartReference.resolve() == null) {
        final String urlToImport = DartImportUtil.getUrlToImport(contextElement, libraryName);
        if (urlToImport != null) {
          PsiDocumentManager.getInstance(context.getProject()).commitDocument(context.getDocument());
          DartImportUtil.insertImport(context.getFile(), item.getLookupString(), urlToImport);
        }
      }
    }
  };
}
