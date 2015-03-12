package com.jetbrains.lang.dart.ide.index;

import com.intellij.util.io.DataExternalizer;
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
  public void save(@NotNull final DataOutput out, @NotNull final List<DartComponentInfo> infos) throws IOException {
    out.writeInt(infos.size());
    for (DartComponentInfo componentInfo : infos) {
      final DartComponentType dartComponentType = componentInfo.getComponentType();
      final int key = dartComponentType == null ? -1 : dartComponentType.getKey();
      out.writeInt(key);
      final String libraryName = componentInfo.getLibraryName();
      out.writeBoolean(libraryName != null);
      if (libraryName != null) {
        out.writeUTF(libraryName);
      }
    }
  }

  @Override
  public List<DartComponentInfo> read(@NotNull DataInput in) throws IOException {
    int size = in.readInt();
    if (size == 0) return Collections.emptyList();

    List<DartComponentInfo> result = new ArrayList<DartComponentInfo>(size);

    for (int i = 0; i < size; i++) {
      final int componentTypeKey = in.readInt();
      final boolean hasLibraryName = in.readBoolean();
      final String libraryName = hasLibraryName ? in.readUTF() : null;
      result.add(new DartComponentInfo(DartComponentType.valueOf(componentTypeKey), libraryName));
    }

    return result;
  }
}
