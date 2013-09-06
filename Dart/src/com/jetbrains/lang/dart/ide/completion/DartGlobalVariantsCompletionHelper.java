package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.PairProcessor;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.index.DartComponentIndex;
import com.jetbrains.lang.dart.ide.index.DartComponentInfo;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.util.DartImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Created by fedorkorotkov.
 */
public class DartGlobalVariantsCompletionHelper {
  private DartGlobalVariantsCompletionHelper() {
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
        DartImportUtil.insertImport(at.getContainingFile(), item.getLookupString(), libraryId);
      }
    }
  };
}
