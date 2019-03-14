package org.angularjs.codeInsight.refs;

import com.intellij.javascript.JSFileReference;
import com.intellij.lang.ecmascript6.resolve.JSFileReferencesUtil;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static com.intellij.lang.typescript.modules.TypeScriptModuleFileReferenceSet.addParentPathContexts;

public class AngularJSTemplateReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return ArrayUtil.mergeArrays(new Angular2SoftFileReferenceSet(element).getAllReferences(),
                                 new PsiReference[]{new AngularJSTemplateCacheReference((JSLiteralExpression)element)});
  }

  public static class Angular2SoftFileReferenceSet extends SoftFileReferenceSet {

    public static void encodeAmbiguousRelativePathData(PsiFile file) {
      visitFile(file, Angular2SoftFileReferenceSet::encodeAmbiguousRelativePathData);
    }

    public static void decodeAmbiguousRelativePathData(PsiFile file) {
      visitFile(file, Angular2SoftFileReferenceSet::decodeAmbiguousRelativePathData);
    }

    private static final Key<AmbiguousRelativePathData> AMBIGUOUS_RELATIVE_PATH_DATA =
      new Key<>("angular2.ambiguous-relative-path-data");

    public Angular2SoftFileReferenceSet(PsiElement element) {
      super(element);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
      final PsiElement element = getElement();
      if (Angular2LangUtil.isAngular2Context(element)) {
        final PsiFile file = element.getContainingFile().getOriginalFile();
        String pathString = StringUtil.trimStart(getPathString(), "./");
        Collection<PsiFileSystemItem> contexts = ContainerUtil.newLinkedHashSet();
        if (!pathString.startsWith("../") && addParentPathContexts(file, pathString, contexts)) {
          return contexts;
        }
        return getDefaultFileRelativeContexts(file);
      }

      return super.computeDefaultContexts();
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return Angular2LangUtil.isAngular2Context(getElement())
             ? new Angular2FileReference(text, index, range, this, ArrayUtil.EMPTY_STRING_ARRAY)
             : new JSFileReference(text, index, range, this, ArrayUtil.EMPTY_STRING_ARRAY);
    }

    private static void visitFile(@Nullable PsiFile file, @NotNull Consumer<Angular2SoftFileReferenceSet> action) {
      if (file == null || file instanceof PsiCompiledElement || isBinary(file)) return;
      for (JSLiteralExpression expression : PsiTreeUtil.findChildrenOfType(file, JSLiteralExpression.class)) {
        Angular2FileReference angular2FileReference = ContainerUtil.findInstance(expression.getReferences(),
                                                                                 Angular2FileReference.class);
        if (angular2FileReference != null) {
          action.accept((Angular2SoftFileReferenceSet)angular2FileReference.getFileReferenceSet());
        }
      }
    }

    @NotNull
    private void encodeAmbiguousRelativePathData() {
      final PsiFile file = getElement().getContainingFile().getOriginalFile();
      String pathString = StringUtil.trimStart(getPathString(), "./");
      Collection<PsiFileSystemItem> contexts = ContainerUtil.newLinkedHashSet();
      if (!pathString.startsWith("../") && addParentPathContexts(file, pathString, contexts)) {
        PsiFileSystemItem firstItem = ContainerUtil.getFirstItem(contexts);
        if (firstItem instanceof PsiFile) {
          firstItem = firstItem.getParent();
        }
        boolean isFileRelative = firstItem == file.getParent();
        PsiFileSystemItem resolved = resolve();
        if (resolved != null) {
          getElement().putCopyableUserData(
            AMBIGUOUS_RELATIVE_PATH_DATA, new AmbiguousRelativePathData(
              resolved, isFileRelative ? null : new ArrayList<>(contexts)));
        }
      }
    }

    @NotNull
    private void decodeAmbiguousRelativePathData() {
      AmbiguousRelativePathData data = getElement().getCopyableUserData(AMBIGUOUS_RELATIVE_PATH_DATA);
      if (data != null) {
        getElement().putCopyableUserData(AMBIGUOUS_RELATIVE_PATH_DATA, null);
        Angular2FileReference ref = ObjectUtils.tryCast(getLastReference(), Angular2FileReference.class);
        if (data.targetFile != null && ref != null) {
          ref.bindToElementAfterMove(data);
        }
      }
    }

    private static boolean isBinary(@NotNull PsiElement element) {
      final PsiFile containingFile = element.getContainingFile();
      if (containingFile == null || containingFile.getFileType().isBinary()) return true;
      return false;
    }

    private static Collection<PsiFileSystemItem> getDefaultFileRelativeContexts(PsiFile file) {
      final Project project = file.getProject();
      final TypeScriptConfig config = TypeScriptCompilerConfigUtil.getConfigForFile(project, file.getVirtualFile());
      final PsiDirectory directory = config != null ?
                                     PsiManager.getInstance(project).findDirectory(config.getConfigDirectory()) :
                                     null;
      return ContainerUtil.packNullables(file.getContainingDirectory(), directory);
    }
  }

  private static class Angular2FileReference extends JSFileReference {

    private AmbiguousRelativePathData myBindingAfterMoveData;

    public Angular2FileReference(String refText,
                                 int offset,
                                 TextRange textRange,
                                 @NotNull FileReferenceSet fileReferenceSet,
                                 String[] implicitExtensions) {
      super(refText, offset, textRange, fileReferenceSet, implicitExtensions);
    }

    @Override
    public boolean isRelativeCommonPath() {
      return myBindingAfterMoveData != null || super.isRelativeCommonPath();
    }

    public void bindToElementAfterMove(AmbiguousRelativePathData data) {
      Collection<PsiFileSystemItem> contexts =
        data.contexts != null ? ContainerUtil.filter(data.contexts, PsiFileSystemItem::isValid)
                              : Angular2SoftFileReferenceSet.getDefaultFileRelativeContexts(
                                getFileReferenceSet().getElement().getContainingFile().getOriginalFile());
      if (contexts.size() > 0 && data.targetFile.isValid()) {
        Collection<VirtualFile> filteredContexts = ContainerUtil.map(contexts, el -> el.getVirtualFile());
        VirtualFile dstVFile = data.targetFile.getVirtualFile();
        String path = JSFileReferencesUtil.getShortestPathInContexts(dstVFile, filteredContexts, true);
        if (path != null) {
          rename(path);
          return;
        }
      }
      myBindingAfterMoveData = data;
      try {
        super.bindToElement(data.targetFile, false);
      }
      finally {
        myBindingAfterMoveData = null;
      }
    }
  }

  private static class AmbiguousRelativePathData {
    @NotNull public final PsiFileSystemItem targetFile;
    @Nullable public final Collection<PsiFileSystemItem> contexts;


    private AmbiguousRelativePathData(@NotNull PsiFileSystemItem targetFile,
                                      @Nullable Collection<PsiFileSystemItem> contexts) {
      this.targetFile = targetFile;
      this.contexts = contexts;
    }
  }
}
