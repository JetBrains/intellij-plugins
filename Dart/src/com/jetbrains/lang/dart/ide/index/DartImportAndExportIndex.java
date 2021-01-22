// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.IOUtil;
import com.jetbrains.lang.dart.DartFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;

public final class DartImportAndExportIndex extends SingleEntryFileBasedIndexExtension<List<DartImportOrExportInfo>> {
  private static final ID<Integer, List<DartImportOrExportInfo>> DART_IMPORT_EXPORT_INDEX = ID.create("DartImportIndex");

  @NotNull
  @Override
  public ID<Integer, List<DartImportOrExportInfo>> getName() {
    return DART_IMPORT_EXPORT_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION + 1;
  }

  @Override
  public @NotNull SingleEntryIndexer<List<DartImportOrExportInfo>> getIndexer() {
    return new SingleEntryIndexer<>(false) {
      @Nullable
      @Override
      protected List<DartImportOrExportInfo> computeValue(@NotNull FileContent inputData) {
        return DartIndexUtil.indexFile(inputData).getImportAndExportInfos();
      }
    };
  }

  @NotNull
  @Override
  public DataExternalizer<List<DartImportOrExportInfo>> getValueExternalizer() {
    return new DataExternalizer<>() {
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
        final List<DartImportOrExportInfo> result = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
          final DartImportOrExportInfo.Kind kind = DartImportOrExportInfo.Kind.valueOf(IOUtil.readUTF(in));
          final String uri = IOUtil.readUTF(in);
          final String prefix = IOUtil.readUTF(in);
          final int showSize = DataInputOutputUtil.readINT(in);
          final Set<String> showComponentNames = showSize == 0 ? Collections.emptySet() : new HashSet<>(showSize);
          for (int j = 0; j < showSize; j++) {
            showComponentNames.add(IOUtil.readUTF(in));
          }
          final int hideSize = DataInputOutputUtil.readINT(in);
          final Set<String> hideComponentNames = hideSize == 0 ? Collections.emptySet() : new HashSet<>(hideSize);
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
    return new DefaultFileTypeSpecificInputFilter(DartFileType.INSTANCE);
  }

  @NotNull
  public static List<DartImportOrExportInfo> getImportAndExportInfos(final @NotNull Project project,
                                                                     final @NotNull VirtualFile virtualFile) {
    return FileBasedIndex.getInstance().getSingleEntryIndexData(DART_IMPORT_EXPORT_INDEX, virtualFile, project);
  }
}
