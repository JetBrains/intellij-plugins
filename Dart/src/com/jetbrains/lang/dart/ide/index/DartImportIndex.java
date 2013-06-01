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

/**
 * @author: Fedor.Korotkov
 */
public class DartImportIndex extends FileBasedIndexExtension<String, List<DartPathInfo>> {
  public static final ID<String, List<DartPathInfo>> DART_IMPORT_INDEX = ID.create("DartImportIndex");
  private static final int INDEX_VERSION = 2;
  private DataIndexer<String, List<DartPathInfo>, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, List<DartPathInfo>> getName() {
    return DART_IMPORT_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<DartPathInfo>, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public DataExternalizer<List<DartPathInfo>> getValueExternalizer() {
    return new DataExternalizer<List<DartPathInfo>>() {
      @Override
      public void save(DataOutput out, List<DartPathInfo> value) throws IOException {
        out.writeInt(value.size());
        for (DartPathInfo pathInfo : value) {
          out.writeUTF(pathInfo.getPath());
          out.writeUTF(StringUtil.notNullize(pathInfo.getPrefix()));
          out.writeInt(pathInfo.getShowComponents().size());
          for (String showComponentName : pathInfo.getShowComponents()) {
            out.writeUTF(showComponentName);
          }
          out.writeInt(pathInfo.getHideComponents().size());
          for (String hideComponentName : pathInfo.getHideComponents()) {
            out.writeUTF(hideComponentName);
          }
        }
      }

      @Override
      public List<DartPathInfo> read(DataInput in) throws IOException {
        final int size = in.readInt();
        final List<DartPathInfo> result = new ArrayList<DartPathInfo>(size);
        for (int i = 0; i < size; ++i) {
          final String path = in.readUTF();
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
          result.add(new DartPathInfo(path, StringUtil.nullize(prefix), showComponentNames, hideComponentNames));
        }
        return result;
      }
    };
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static List<DartPathInfo> getLibraryNames(Project project, VirtualFile virtualFile) {
    final List<DartPathInfo> result = new ArrayList<DartPathInfo>();
    for (List<DartPathInfo> list : FileBasedIndex.getInstance()
      .getValues(DART_IMPORT_INDEX, virtualFile.getName(), GlobalSearchScope.fileScope(project, virtualFile))) {
      result.addAll(list);
    }
    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<DartPathInfo>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<DartPathInfo>> map(final FileContent inputData) {
      return Collections.singletonMap(inputData.getFileName(), DartIndexUtil.indexFile(inputData).getImportPaths());
    }
  }
}
