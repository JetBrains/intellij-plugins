package com.jetbrains.lang.dart.ide.index;

import com.intellij.util.io.DataExternalizer;
import com.jetbrains.lang.dart.DartComponentType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DartComponentInfoListExternalizer implements DataExternalizer<List<DartComponentInfo>> {
  @Override
  public void save(DataOutput out, List<DartComponentInfo> infos) throws IOException {
    out.writeInt(infos.size());
    for (DartComponentInfo componentInfo : infos) {
      out.writeUTF(componentInfo.getValue());
      final DartComponentType dartComponentType = componentInfo.getType();
      final int key = dartComponentType == null ? -1 : dartComponentType.getKey();
      out.writeInt(key);
      final String libraryId = componentInfo.getLibraryId();
      out.writeBoolean(libraryId != null);
      if (libraryId != null) {
        out.writeUTF(libraryId);
      }
    }
  }

  @Override
  public List<DartComponentInfo> read(DataInput in) throws IOException {
    int size = in.readInt();
    if (size == 0) return Collections.emptyList();

    List<DartComponentInfo> result = new ArrayList<DartComponentInfo>(size);

    for (int i = 0; i < size; i++) {
      final String value = in.readUTF();
      final int key = in.readInt();
      final boolean haveLibraryId = in.readBoolean();
      final String libraryId = haveLibraryId ? in.readUTF() : null;
      result.add(new DartComponentInfo(value, DartComponentType.valueOf(key), libraryId));
    }
    return result;
  }
}
