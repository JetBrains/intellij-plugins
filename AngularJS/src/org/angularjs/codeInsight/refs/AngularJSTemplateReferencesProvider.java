package org.angularjs.codeInsight.refs;

import com.intellij.javascript.JSFileReference;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerConfigUtil;
import com.intellij.lang.typescript.tsconfig.TypeScriptConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.SoftFileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

import static com.intellij.lang.typescript.modules.TypeScriptModuleFileReferenceSet.addParentPathContexts;
import static com.intellij.openapi.util.Pair.pair;

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

    private static final Key<Pair<PsiFileSystemItem, Collection<PsiFileSystemItem>>> AMBIGUOUS_RELATIVE_PATH_DATA =
      new Key<>("angular2.ambiguous-relative-path-data");
    private Pair<PsiFileSystemItem, Collection<PsiFileSystemItem>> myAmbiguousRelativePathData;

    public Angular2SoftFileReferenceSet(PsiElement element) {
      super(element);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
      final PsiElement element = getElement();
      final Project project = element.getProject();
      if (Angular2LangUtil.isAngular2Context(element)) {
        if (myAmbiguousRelativePathData != null
            && myAmbiguousRelativePathData.second != null) {
          return myAmbiguousRelativePathData.second;
        }
        final PsiFile file = element.getContainingFile().getOriginalFile();
        final TypeScriptConfig config = TypeScriptCompilerConfigUtil.getConfigForFile(project, file.getVirtualFile());
        final PsiDirectory directory = config != null ?
                                       PsiManager.getInstance(project).findDirectory(config.getConfigDirectory()) :
                                       null;

        String pathString = getPathString();

        Collection<PsiFileSystemItem> contexts = ContainerUtil.newLinkedHashSet();
        if (!pathString.startsWith(".") && addParentPathContexts(file, pathString, contexts)) {
          return contexts;
        }
        ContainerUtil.addAllNotNull(contexts, file.getContainingDirectory(), directory);
        return contexts;
      }

      return super.computeDefaultContexts();
    }

    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
      return Angular2LangUtil.isAngular2Context(getElement())
             ? new Angular2FileReference(text, index, range, this, ArrayUtil.EMPTY_STRING_ARRAY)
             : new JSFileReference(text, index, range, this, ArrayUtil.EMPTY_STRING_ARRAY);
    }

    public boolean hasAmbiguousFileRelativePath() {
      return myAmbiguousRelativePathData != null
             && myAmbiguousRelativePathData.second == null;
    }

    private static void visitFile(@Nullable PsiFile file, @NotNull Function<Angular2SoftFileReferenceSet, PsiElement> action) {
      if (file == null || file instanceof PsiCompiledElement || isBinary(file)) return;
      for (JSLiteralExpression expression : PsiTreeUtil.findChildrenOfType(file, JSLiteralExpression.class)) {
        Angular2FileReference angular2FileReference = ContainerUtil.findInstance(expression.getReferences(),
                                                                                 Angular2FileReference.class);
        if (angular2FileReference != null) {
          action.apply((Angular2SoftFileReferenceSet)angular2FileReference.getFileReferenceSet());
        }
      }
    }

    private PsiElement encodeAmbiguousRelativePathData() {
      final PsiFile file = getElement().getContainingFile().getOriginalFile();
      String pathString = getPathString();
      Collection<PsiFileSystemItem> contexts = ContainerUtil.newLinkedHashSet();
      if (!pathString.startsWith(".") && addParentPathContexts(file, pathString, contexts)) {
        PsiFileSystemItem firstItem = ContainerUtil.getFirstItem(contexts);
        if (firstItem instanceof PsiFile) {
          firstItem = firstItem.getParent();
        }
        boolean isFileRelative = firstItem == file.getParent();
        getElement().putCopyableUserData(AMBIGUOUS_RELATIVE_PATH_DATA,
                                         pair(resolve(), isFileRelative ? null
                                                                        : new ArrayList<>(contexts)));
      }
      return getElement();
    }

    private PsiElement decodeAmbiguousRelativePathData() {
      PsiElement result = getElement();
      myAmbiguousRelativePathData = getElement().getCopyableUserData(AMBIGUOUS_RELATIVE_PATH_DATA);
      if (myAmbiguousRelativePathData != null) {
        getElement().putCopyableUserData(AMBIGUOUS_RELATIVE_PATH_DATA, null);
        FileReference ref = getLastReference();
        if (myAmbiguousRelativePathData.first != null && ref != null) {
          result = ref.bindToElement(myAmbiguousRelativePathData.first);
        }
        myAmbiguousRelativePathData = null;
      }
      return result;
    }

    private static boolean isBinary(PsiElement element) {
      final PsiFile containingFile = element.getContainingFile();
      if (containingFile == null || containingFile.getFileType().isBinary()) return true;
      return false;
    }
  }

  private static class Angular2FileReference extends JSFileReference {

    public Angular2FileReference(String refText,
                                 int offset,
                                 TextRange textRange,
                                 @NotNull FileReferenceSet fileReferenceSet,
                                 String[] implicitExtensions) {
      super(refText, offset, textRange, fileReferenceSet, implicitExtensions);
    }

    @Override
    public boolean isRelativeCommonPath() {
      return super.isRelativeCommonPath()
             || ((Angular2SoftFileReferenceSet)getFileReferenceSet()).hasAmbiguousFileRelativePath();
    }
  }
}
