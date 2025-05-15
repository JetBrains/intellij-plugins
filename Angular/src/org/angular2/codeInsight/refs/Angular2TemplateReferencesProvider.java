// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.codeInsight.refs;

import com.intellij.javascript.JSFileReference;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.ecmascript6.TypeScriptUtil;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.typescript.modules.resolver.TypeScriptFileReferenceContext;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceHelper;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

import static org.angular2.codeInsight.refs.Angular2TSReferencesContributorKt.isAngularUrlLiteral;


public class Angular2TemplateReferencesProvider extends PsiReferenceProvider {
  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new Angular2SoftFileReferenceSet(element).getAllReferences();
  }

  public static class Angular2SoftFileReferenceSet extends SoftFileReferenceSet {

    public static Map<String, Angular2TemplateReferenceData> encodeTemplateReferenceData(@Nullable PsiFile file) {
      Map<String, Angular2TemplateReferenceData> result = new HashMap<>();
      visitFile(file, fileReferenceSet ->
        ContainerUtil.putIfNotNull(fileReferenceSet.getPathString(),
                                   fileReferenceSet.encodeTemplateReferenceData(),
                                   result));
      if (file != null && !result.isEmpty()) {
        file.putCopyableUserData(TEMPLATE_REFERENCE_DATA_KEY, result);
      }
      return result;
    }

    public static void decodeTemplateReferenceData(@Nullable PsiFile file) {
      Map<String, Angular2TemplateReferenceData> map = file != null ? file.getCopyableUserData(TEMPLATE_REFERENCE_DATA_KEY) : null;
      if (map != null) {
        file.putCopyableUserData(TEMPLATE_REFERENCE_DATA_KEY, null);
        decodeTemplateReferenceData(file, map);
      }
    }

    public static void decodeTemplateReferenceData(@Nullable PsiFile file, @NotNull Map<String, Angular2TemplateReferenceData> dataMap) {
      if (file == null) return;
      visitFile(file, fileReferenceSet -> {
        Angular2TemplateReferenceData referenceData = dataMap.get(fileReferenceSet.getPathString());
        if (referenceData != null) {
          fileReferenceSet.decodeTemplateReferenceData(referenceData);
        }
      });
    }

    private static final Key<Map<String, Angular2TemplateReferenceData>> TEMPLATE_REFERENCE_DATA_KEY =
      new Key<>("angular2.template-reference-data");

    public Angular2SoftFileReferenceSet(@NotNull PsiElement element) {
      super(element);
    }

    @Override
    public @NotNull Collection<PsiFileSystemItem> computeDefaultContexts() {
      final PsiElement element = getElement();
      if (Angular2LangUtil.isAngular2Context(element)) {
        final PsiFile file = element.getContainingFile().getOriginalFile();
        String pathString = StringUtil.trimStart(getPathString(), "./");
        Collection<PsiFileSystemItem> contexts = new LinkedHashSet<>();
        PsiDirectory classicResolveRoot =
          TypeScriptFileReferenceContext.getClassicResolveRoot(file, pathString, TypeScriptUtil.TYPESCRIPT_EXTENSIONS);
        if (classicResolveRoot != null) {
          contexts.add(classicResolveRoot);
          return contexts;
        }
        return getDefaultFileRelativeContexts(file);
      }

      return super.computeDefaultContexts();
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return Angular2LangUtil.isAngular2Context(getElement())
             ? new Angular2FileReference(text, index, range, this)
             : new JSFileReference(text, index, range, this);
    }

    private static void visitFile(@Nullable PsiFile file, @NotNull Consumer<Angular2SoftFileReferenceSet> action) {
      if (file == null || file instanceof PsiCompiledElement || isBinary(file)) return;
      for (JSLiteralExpression expression : PsiTreeUtil.findChildrenOfType(file, JSLiteralExpression.class)) {
        if (isAngularUrlLiteral(expression)) {
          action.accept(new Angular2SoftFileReferenceSet(expression));
        }
      }
    }

    private @Nullable Angular2TemplateReferenceData encodeTemplateReferenceData() {
      final PsiFile file = getElement().getContainingFile().getOriginalFile();
      String pathString = StringUtil.trimStart(getPathString(), "./");

      PsiDirectory firstItem =
        TypeScriptFileReferenceContext.getClassicResolveRoot(file, pathString, TypeScriptUtil.TYPESCRIPT_EXTENSIONS);
      if (!pathString.startsWith("../")) {
        if (firstItem != null) {
          boolean isFileRelative = firstItem == file.getParent();
          PsiFileSystemItem resolved = resolve();
          if (resolved != null) {
            Collection<PsiFileSystemItem> contexts = new LinkedHashSet<>();
            contexts.add(firstItem);
            return new Angular2TemplateReferenceData(resolved, isFileRelative ? null : new ArrayList<>(contexts));
          }
        }
      }
      return null;
    }

    private void decodeTemplateReferenceData(@NotNull Angular2TemplateReferenceData data) {
      Angular2FileReference ref = ObjectUtils.tryCast(getLastReference(), Angular2FileReference.class);
      PsiManager manager = getElement().getManager();
      PsiFileSystemItem file = data.getTargetFile(manager);
      Collection<PsiFileSystemItem> contexts =
        ContainerUtil.filter(ObjectUtils.notNull(data.getContexts(manager), () ->
                               getDefaultFileRelativeContexts(getElement().getContainingFile().getOriginalFile())),
                             PsiFileSystemItem::isValid);
      if (file != null && file.isValid() && ref != null) {
        ref.bindToElementAfterMove(file, contexts);
      }
    }

    private static boolean isBinary(@NotNull PsiElement element) {
      final PsiFile containingFile = element.getContainingFile();
      if (containingFile == null || containingFile.getFileType().isBinary()) return true;
      return false;
    }

    private static Collection<PsiFileSystemItem> getDefaultFileRelativeContexts(PsiFile file) {
      return ContainerUtil.packNullables(file.getContainingDirectory());
    }
  }

  private static class Angular2FileReference extends JSFileReference {

    Angular2FileReference(String refText,
                          int offset,
                          TextRange textRange,
                          @NotNull FileReferenceSet fileReferenceSet) {
      super(refText, offset, textRange, fileReferenceSet);
    }

    public void bindToElementAfterMove(@NotNull PsiFileSystemItem targetFile, @NotNull Collection<PsiFileSystemItem> contexts) {
      if (!contexts.isEmpty()) {
        Collection<VirtualFile> filteredContexts = ContainerUtil.map(contexts, el -> el.getVirtualFile());
        VirtualFile dstVFile = targetFile.getVirtualFile();
        String path = JSFileReferencesUtil.getShortestPathInContexts(dstVFile, filteredContexts, true);
        if (path != null) {
          rename(path);
          return;
        }
      }
      bindToElement(targetFile, false, true);
    }
  }

  public static final class Angular2TemplateReferenceData {
    private final VirtualFile targetFile;
    private final @Nullable Collection<VirtualFile> contexts;

    private Angular2TemplateReferenceData(@NotNull PsiFileSystemItem targetFile,
                                          @Nullable Collection<PsiFileSystemItem> contexts) {
      this(targetFile.getVirtualFile(), contexts != null ? ContainerUtil.map(contexts, PsiFileSystemItem::getVirtualFile) : null);
    }

    private Angular2TemplateReferenceData(VirtualFile targetFile, @Nullable Collection<VirtualFile> contexts) {
      this.targetFile = targetFile;
      this.contexts = contexts;
    }

    private @Nullable PsiFileSystemItem getTargetFile(@NotNull PsiManager manager) {
      return FileReferenceHelper.getPsiFileSystemItem(manager, targetFile);
    }

    private @Nullable Collection<PsiFileSystemItem> getContexts(@NotNull PsiManager manager) {
      return contexts != null
             ? ContainerUtil.mapNotNull(contexts, item -> FileReferenceHelper.getPsiFileSystemItem(manager, item))
             : null;
    }
  }
}
