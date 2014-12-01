package org.angularjs.index;

import com.intellij.lang.javascript.index.JSEntryIndex;
import com.intellij.lang.javascript.index.JSIndexContent;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public abstract class AngularIndexBase extends ScalarIndexExtension<String> {
  private final DataIndexer<String, Void, FileContent> myIndexer =
    new DataIndexer<String, Void, FileContent>() {
      @Override
      @NotNull
      public Map<String, Void> map(@NotNull FileContent inputData) {
        return keysToMap(JSIndexContent.indexFile(inputData).myAdditionalData.get(getName().toString()));
      }

      @NotNull
      private Map<String, Void> keysToMap(@Nullable Map<String, ?> map) {
        if (map == null) return Collections.emptyMap();
        final Map<String, Void> result = new THashMap<String, Void>();
        for (String key : map.keySet()) {
          result.put(key, null);
        }
        return result;
      }
    };
  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public abstract ID<String, Void> getName();

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return JSEntryIndex.ourIndexedFilesFilter;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION + JSEntryIndex.getVersionStatic();
  }
}
