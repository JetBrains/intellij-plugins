package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class DartImportIndex extends FileBasedIndexExtension<String, List<DartImportInfo>> {
  public static final ID<String, List<DartImportInfo>> DART_IMPORT_INDEX = ID.create("DartImportIndex");
  private static final int INDEX_VERSION = 2;
  private DataIndexer<String, List<DartImportInfo>, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, List<DartImportInfo>> getName() {
    return DART_IMPORT_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<DartImportInfo>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @NotNull
  @Override
  public DataExternalizer<List<DartImportInfo>> getValueExternalizer() {
    return new DataExternalizer<List<DartImportInfo>>() {
      @Override
      public void save(@NotNull DataOutput out, List<DartImportInfo> value) throws IOException {
        out.writeInt(value.size());
        for (DartImportInfo importInfo : value) {
          out.writeUTF(importInfo.getImportText());
          out.writeUTF(StringUtil.notNullize(importInfo.getPrefix()));
          out.writeInt(importInfo.getShowComponents().size());
          for (String showComponentName : importInfo.getShowComponents()) {
            out.writeUTF(showComponentName);
          }
          out.writeInt(importInfo.getHideComponents().size());
          for (String hideComponentName : importInfo.getHideComponents()) {
            out.writeUTF(hideComponentName);
          }
        }
      }

      @Override
      public List<DartImportInfo> read(@NotNull DataInput in) throws IOException {
        final int size = in.readInt();
        final List<DartImportInfo> result = new ArrayList<DartImportInfo>(size);
        for (int i = 0; i < size; ++i) {
          final String importText = in.readUTF();
          final String prefix = in.readUTF();
          final int showSize = in.readInt();
          final Set<String> showComponentNames = showSize == 0 ? Collections.<String>emptySet() : new THashSet<String>(showSize);
          for (int j = 0; j < showSize; j++) {
            showComponentNames.add(in.readUTF());
          }
          final int hideSize = in.readInt();
          final Set<String> hideComponentNames = hideSize == 0 ? Collections.<String>emptySet() : new THashSet<String>(hideSize);
          for (int j = 0; j < hideSize; j++) {
            hideComponentNames.add(in.readUTF());
          }
          result.add(new DartImportInfo(importText, StringUtil.nullize(prefix), showComponentNames, hideComponentNames));
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

  public static List<DartImportInfo> getImportInfos(final @NotNull Project project, final @NotNull VirtualFile virtualFile) {
    final List<DartImportInfo> result = new ArrayList<DartImportInfo>();
    for (List<DartImportInfo> list : FileBasedIndex.getInstance()
      .getValues(DART_IMPORT_INDEX, virtualFile.getName(), GlobalSearchScope.fileScope(project, virtualFile))) {
      result.addAll(list);
    }
    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<DartImportInfo>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<DartImportInfo>> map(@NotNull final FileContent inputData) {
      return Collections.singletonMap(inputData.getFileName(), DartIndexUtil.indexFile(inputData).getImportInfos());
    }
  }
}
