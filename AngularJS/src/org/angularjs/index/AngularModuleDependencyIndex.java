package org.angularjs.index;

import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

/**
 * @author Irina.Chernushina on 3/17/2016.
 */
public class AngularModuleDependencyIndex extends FileBasedIndexExtension<String, List<String>> {
  public static final ID<String, List<String>> ANGULAR_MODULE_DEPENDENCY_INDEX = ID.create("angularjs.module.dependency.index");
  private DataExternalizer<List<String>> myExternalizer = new MyListDataExternalizer();

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new FileBasedIndex.InputFilter() {
      @Override
      public boolean acceptInput(@NotNull VirtualFile file) {
        return JavaScriptIndex.ourIndexedFilesFilter.acceptInput(file);
      }
    };
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @NotNull
  @Override
  public ID<String, List<String>> getName() {
    return ANGULAR_MODULE_DEPENDENCY_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<String>, FileContent> getIndexer() {
    return new DataIndexer<String, List<String>, FileContent>() {
      @NotNull
      @Override
      public Map<String, List<String>> map(@NotNull FileContent inputData) {
        final Map<String, List<String>> map = new HashMap<>();
        final PsiFile psiFile = inputData.getPsiFile();
        if (psiFile instanceof JSFile) {
          psiFile.accept(new JSRecursiveElementVisitor() {
            @Override
            public void visitJSCallExpression(JSCallExpression call) {
              final JSExpression methodExpression = call.getMethodExpression();
              if (methodExpression instanceof JSReferenceExpression &&
                  ((JSReferenceExpression)methodExpression).getQualifier() != null &&
                  AngularJSIndexingHandler.MODULE.equals(((JSReferenceExpression)methodExpression).getReferenceName())) {
                final JSExpression[] arguments = call.getArguments();
                if (arguments.length > 1 && arguments[0] instanceof JSLiteralExpression
                    && ((JSLiteralExpression) arguments[0]).isQuotedLiteral()
                    && arguments[1] instanceof JSArrayLiteralExpression) {
                  final JSArrayLiteralExpression array = (JSArrayLiteralExpression)arguments[1];
                  final JSExpression[] children = array.getExpressions();
                  final Set<String> dependencies = new HashSet<String>();
                  for (JSExpression child : children) {
                    if (child instanceof JSLiteralExpression && ((JSLiteralExpression)child).isQuotedLiteral()) {
                      dependencies.add(StringUtil.unquoteString(child.getText()));
                    }
                  }
                  if (!dependencies.isEmpty()) {
                    map.put(StringUtil.unquoteString(arguments[0].getText()), new ArrayList<>(dependencies));
                  }
                }
              }
              super.visitJSCallExpression(call);
            }
          });
        }
        return map;
      }
    };
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<List<String>> getValueExternalizer() {
    return myExternalizer;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }

  private static class MyListDataExternalizer implements DataExternalizer<List<String>> {
    @Override
    public void save(@NotNull DataOutput out, List<String> value) throws IOException {
      out.writeInt(value.size());
      for (String s : value) {
        out.writeUTF(s);
      }
    }

    @Override
    public List<String> read(@NotNull DataInput in) throws IOException {
      final int size = in.readInt();
      final List<String> result = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        result.add(in.readUTF());
      }
      return result;
    }
  }
}
