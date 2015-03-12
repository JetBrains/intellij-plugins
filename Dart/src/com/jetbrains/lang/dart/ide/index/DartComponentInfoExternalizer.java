package com.jetbrains.lang.dart.ide.index;

import com.intellij.util.io.DataExternalizer;
import com.jetbrains.lang.dart.DartComponentType;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DartComponentInfoExternalizer implements DataExternalizer<DartComponentInfo> {

  @Override
  public void save(@NotNull final DataOutput out, @NotNull final DartComponentInfo componentInfo) throws IOException {
    final DartComponentType dartComponentType = componentInfo.getComponentType();
    final int key = dartComponentType == null ? -1 : dartComponentType.getKey();
    out.writeInt(key);
    final String libraryName = componentInfo.getLibraryName();
    out.writeBoolean(libraryName != null);
    if (libraryName != null) {
      out.writeUTF(libraryName);
    }
  }

  @Override
  public DartComponentInfo read(@NotNull DataInput in) throws IOException {
    final int componentTypeKey = in.readInt();
    final boolean hasLibraryName = in.readBoolean();
    final String libraryName = hasLibraryName ? in.readUTF() : null;
    return new DartComponentInfo(DartComponentType.valueOf(componentTypeKey), libraryName);
  }
}
