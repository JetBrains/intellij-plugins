package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.LogMessageUtil;
import com.intellij.flex.uiDesigner.abc.AbcTranscoder;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.PersistentHashMap;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;

import static com.intellij.flex.uiDesigner.libraries.LibrarySorter.SortResult;

class LibrariesData {
  final PersistentHashMap<String, SortResult> librarySets;

  private static final String ABC_FILTER_VERSION = "40";
  private static final String ABC_FILTER_VERSION_VALUE_NAME = "fud_abcFilterVersion";

  static final char NAME_PREFIX = '@';

  LibrariesData(File cacheDir) throws IOException {
    PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
    if (!ABC_FILTER_VERSION.equals(propertiesComponent.getValue(ABC_FILTER_VERSION_VALUE_NAME))) {
      clearCache(cacheDir);
      propertiesComponent.setValue(ABC_FILTER_VERSION_VALUE_NAME, ABC_FILTER_VERSION);
    }

    librarySets = createLibrarySetsCache(cacheDir);
  }

  public void close() {
    try {
      librarySets.close();
    }
    catch (IOException e) {
      LogMessageUtil.LOG.info(e);
    }
  }

  private static void clearCache(File cacheDir) {
    for (String path : cacheDir.list()) {
      if (path.charAt(0) == NAME_PREFIX) {
        //noinspection ResultOfMethodCallIgnored
        new File(cacheDir, path).delete();
      }
    }
  }

  private static PersistentHashMap<String, SortResult> createLibrarySetsCache(File cacheDir) throws IOException {
    final File file = new File(cacheDir, NAME_PREFIX + "librarySets");
    try {
      return new PersistentHashMap<>(file, EnumeratorStringDescriptor.INSTANCE, new LibrarySetDataExternalizer());
    }
    catch (IOException e) {
      LogMessageUtil.LOG.info(e);
      clearCache(cacheDir);
      return new PersistentHashMap<>(file, EnumeratorStringDescriptor.INSTANCE, new LibrarySetDataExternalizer());
    }
  }

  private static class LibrarySetDataExternalizer implements DataExternalizer<SortResult> {
    @Override
    public void save(@NotNull final DataOutput out, SortResult value) throws IOException {
      out.writeShort(value.libraries.size());
      for (Library library : value.libraries) {
        out.writeUTF(library.getFile().getPath());
      }

      if (value.definitionMap == null) {
        out.writeInt(0);
        return;
      }

      out.writeInt(value.definitionMap.size());
      value.definitionMap.forEachKey(charSequence -> {
        try {
          out.writeUTF(charSequence.toString());
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }

        return true;
      });
    }

    @Override
    public SortResult read(@NotNull DataInput in) throws IOException {
      int librariesSize = in.readShort();
      String[] libraryPaths = new String[librariesSize];
      while (librariesSize-- > 0) {
        libraryPaths[librariesSize] = in.readUTF();
      }

      int size = in.readInt();
      final THashMap<CharSequence, Definition> map;
      if (size != 0) {
        map = new THashMap<>(size, AbcTranscoder.HASHING_STRATEGY);
        while (size-- > 0) {
          map.put(in.readUTF(), null);
        }
      }
      else {
        map = null;
      }

      return new SortResult(map, libraryPaths);
    }
  }
}