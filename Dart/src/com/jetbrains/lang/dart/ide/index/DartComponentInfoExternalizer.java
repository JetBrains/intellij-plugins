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

public class DartComponentInfoExternalizer implements DataExternalizer<DartComponentInfo> {

  @Override
  public void save(final @NotNull DataOutput out, final @NotNull DartComponentInfo componentInfo) throws IOException {
    final DartComponentType dartComponentType = componentInfo.getComponentType();
    final int key = dartComponentType == null ? -1 : dartComponentType.getKey();
    DataInputOutputUtil.writeINT(out, key);
    final String libraryName = componentInfo.getLibraryName();
    out.writeBoolean(libraryName != null);
    if (libraryName != null) {
      IOUtil.writeUTF(out, libraryName);
    }
  }

  @Override
  public DartComponentInfo read(@NotNull DataInput in) throws IOException {
    final int componentTypeKey = DataInputOutputUtil.readINT(in);
    final boolean hasLibraryName = in.readBoolean();
    final String libraryName = hasLibraryName ? IOUtil.readUTF(in) : null;
    return new DartComponentInfo(DartComponentType.valueOf(componentTypeKey), libraryName);
  }
}
