package org.angularjs.index;

import com.intellij.lang.javascript.index.JSIndexContent;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public abstract class AngularIndexBase extends FileBasedIndexExtension<String, byte[]> {
  private final DataIndexer<String, byte[], FileContent> myIndexer =
    new DataIndexer<String, byte[], FileContent>() {
      @Override
      @NotNull
      public Map<String, byte[]> map(@NotNull FileContent inputData) {
        final Map<String, byte[]> map = JSIndexContent.indexFile(inputData).myAdditionalData.get(getName().toString());
        return map != null ? map : Collections.<String, byte[]>emptyMap();
      }
    };
  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

  @NotNull
  @Override
  public DataIndexer<String, byte[], FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public abstract ID<String, byte[]> getName();

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return JavaScriptIndex.ourIndexedFilesFilter;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION + JavaScriptIndex.getVersionStatic();
  }

  @NotNull
  @Override
  public DataExternalizer<byte[]> getValueExternalizer() {
    return new DataExternalizer<byte[]>() {
      @Override
      public void save(@NotNull DataOutput out, byte[] value) throws IOException {
        out.writeInt(value.length);
        out.write(value);
      }

      @Override
      public byte[] read(@NotNull DataInput in) throws IOException {
        final int length = in.readInt();
        final byte[] result = new byte[length];
        in.readFully(result);
        return result;
      }
    };
  }
}
