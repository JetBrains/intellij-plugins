// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.DataInputOutputUtil;
import com.intellij.util.io.IOUtil;
import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartComponentInfoListExternalizer implements DataExternalizer<List<DartComponentInfo>> {

  @Override
  public void save(final @NotNull DataOutput out, final @NotNull List<DartComponentInfo> infos) throws IOException {
    DataInputOutputUtil.writeINT(out, infos.size());
    for (DartComponentInfo componentInfo : infos) {
      final DartComponentType dartComponentType = componentInfo.getComponentType();
      final int key = dartComponentType == null ? -1 : dartComponentType.getKey();
      DataInputOutputUtil.writeINT(out, key);
      final String libraryName = componentInfo.getLibraryName();
      out.writeBoolean(libraryName != null);
      if (libraryName != null) {
        IOUtil.writeUTF(out, libraryName);
      }
    }
  }

  @Override
  public List<DartComponentInfo> read(@NotNull DataInput in) throws IOException {
    int size = DataInputOutputUtil.readINT(in);
    if (size == 0) return Collections.emptyList();

    List<DartComponentInfo> result = new ArrayList<>(size);

    for (int i = 0; i < size; i++) {
      final int componentTypeKey = DataInputOutputUtil.readINT(in);
      final boolean hasLibraryName = in.readBoolean();
      final String libraryName = hasLibraryName ? IOUtil.readUTF(in) : null;
      result.add(new DartComponentInfo(DartComponentType.valueOf(componentTypeKey), libraryName));
    }

    return result;
  }
}
