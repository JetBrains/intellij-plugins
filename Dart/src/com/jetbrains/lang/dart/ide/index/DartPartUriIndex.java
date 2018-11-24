package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.*;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DartPartUriIndex extends FileBasedIndexExtension<String, List<String>> {
  public static final ID<String, List<String>> DART_PATH_INDEX = ID.create("DartPathIndex");
  private DataIndexer<String, List<String>, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, List<String>> getName() {
    return DART_PATH_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<String>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<List<String>> getValueExternalizer() {
    return new DataExternalizer<List<String>>() {
      @Override
      public void save(@NotNull DataOutput out, List<String> value) throws IOException {
        DataInputOutputUtil.writeINT(out, value.size());
        for (String path : value) {
          IOUtil.writeUTF(out, path);
        }
      }

      @Override
      public List<String> read(@NotNull DataInput in) throws IOException {
        final int size = DataInputOutputUtil.readINT(in);
        final List<String> result = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
          result.add(IOUtil.readUTF(in));
        }
        return result;
      }
    };
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static List<String> getPartUris(@NotNull final Project project, @NotNull final VirtualFile virtualFile) {
    final List<String> result = new ArrayList<>();
    for (List<String> list : FileBasedIndex.getInstance().getValues(DART_PATH_INDEX, virtualFile.getName(),
                                                                    GlobalSearchScope.fileScope(project, virtualFile))) {
      result.addAll(list);
    }
    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<String>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<String>> map(@NotNull final FileContent inputData) {
      return Collections.singletonMap(inputData.getFileName(), DartIndexUtil.indexFile(inputData).getPartUris());
    }
  }
}
