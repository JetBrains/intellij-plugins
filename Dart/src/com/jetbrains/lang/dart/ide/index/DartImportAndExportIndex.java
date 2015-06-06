package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public class DartImportAndExportIndex extends FileBasedIndexExtension<String, List<DartImportOrExportInfo>> {
  public static final ID<String, List<DartImportOrExportInfo>> DART_IMPORT_EXPORT_INDEX = ID.create("DartImportIndex");
  private DataIndexer<String, List<DartImportOrExportInfo>, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, List<DartImportOrExportInfo>> getName() {
    return DART_IMPORT_EXPORT_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<DartImportOrExportInfo>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @NotNull
  @Override
  public DataExternalizer<List<DartImportOrExportInfo>> getValueExternalizer() {
    return new DataExternalizer<List<DartImportOrExportInfo>>() {
      @Override
      public void save(final @NotNull DataOutput out, final @NotNull List<DartImportOrExportInfo> value) throws IOException {
        DataInputOutputUtil.writeINT(out, value.size());
        for (DartImportOrExportInfo importOrExportInfo : value) {
          IOUtil.writeUTF(out, importOrExportInfo.getKind().name());
          IOUtil.writeUTF(out, importOrExportInfo.getUri());
          IOUtil.writeUTF(out, StringUtil.notNullize(importOrExportInfo.getImportPrefix()));
          DataInputOutputUtil.writeINT(out, importOrExportInfo.getShowComponents().size());
          for (String showComponentName : importOrExportInfo.getShowComponents()) {
            IOUtil.writeUTF(out, showComponentName);
          }
          DataInputOutputUtil.writeINT(out, importOrExportInfo.getHideComponents().size());
          for (String hideComponentName : importOrExportInfo.getHideComponents()) {
            IOUtil.writeUTF(out, hideComponentName);
          }
        }
      }

      @Override
      @NotNull
      public List<DartImportOrExportInfo> read(final @NotNull DataInput in) throws IOException {
        final int size = DataInputOutputUtil.readINT(in);
        final List<DartImportOrExportInfo> result = new ArrayList<DartImportOrExportInfo>(size);
        for (int i = 0; i < size; ++i) {
          final DartImportOrExportInfo.Kind kind = DartImportOrExportInfo.Kind.valueOf(in.readUTF());
          final String uri = IOUtil.readUTF(in);
          final String prefix = IOUtil.readUTF(in);
          final int showSize = DataInputOutputUtil.readINT(in);
          final Set<String> showComponentNames = showSize == 0 ? Collections.<String>emptySet() : new THashSet<String>(showSize);
          for (int j = 0; j < showSize; j++) {
            showComponentNames.add(IOUtil.readUTF(in));
          }
          final int hideSize = DataInputOutputUtil.readINT(in);
          final Set<String> hideComponentNames = hideSize == 0 ? Collections.<String>emptySet() : new THashSet<String>(hideSize);
          for (int j = 0; j < hideSize; j++) {
            hideComponentNames.add(IOUtil.readUTF(in));
          }
          result.add(new DartImportOrExportInfo(kind, uri, StringUtil.nullize(prefix), showComponentNames, hideComponentNames));
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

  @NotNull
  public static List<DartImportOrExportInfo> getImportAndExportInfos(final @NotNull Project project,
                                                                     final @NotNull VirtualFile virtualFile) {
    final List<DartImportOrExportInfo> result = new ArrayList<DartImportOrExportInfo>();
    for (List<DartImportOrExportInfo> list : FileBasedIndex.getInstance()
      .getValues(DART_IMPORT_EXPORT_INDEX, virtualFile.getName(), GlobalSearchScope.fileScope(project, virtualFile))) {
      result.addAll(list);
    }
    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<DartImportOrExportInfo>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<DartImportOrExportInfo>> map(@NotNull final FileContent inputData) {
      return Collections.singletonMap(inputData.getFileName(), DartIndexUtil.indexFile(inputData).getImportAndExportInfos());
    }
  }
}
