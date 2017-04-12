package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PairProcessor;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class DartComponentIndex extends FileBasedIndexExtension<String, DartComponentInfo> {
  public static final ID<String, DartComponentInfo> DART_COMPONENT_INDEX = ID.create("DartComponentIndex");
  private final DataIndexer<String, DartComponentInfo, FileContent> myIndexer = new MyDataIndexer();
  private final DataExternalizer<DartComponentInfo> myExternalizer = new DartComponentInfoExternalizer();

  @NotNull
  @Override
  public ID<String, DartComponentInfo> getName() {
    return DART_COMPONENT_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, DartComponentInfo, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<DartComponentInfo> getValueExternalizer() {
    return myExternalizer;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  public static Collection<VirtualFile> getAllFiles(@NotNull final String componentName, @NotNull final GlobalSearchScope scope) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_COMPONENT_INDEX, componentName, scope);
  }

  public static void processAllComponents(@NotNull final GlobalSearchScope scope,
                                          @Nullable final Condition<String> nameFilter,
                                          @NotNull final PairProcessor<String, DartComponentInfo> processor) {
    final Processor<String> keysProcessor = componentName -> {
      if (nameFilter != null && !nameFilter.value(componentName)) return true;

      return processComponentsByName(componentName, scope, info -> processor.process(componentName, info));
    };

    FileBasedIndex.getInstance().processAllKeys(DART_COMPONENT_INDEX, keysProcessor, scope, null);
  }

  public static boolean processComponentsByName(@NotNull final String componentName,
                                                @NotNull final GlobalSearchScope scope,
                                                @NotNull final Processor<DartComponentInfo> processor) {
    final FileBasedIndex.ValueProcessor<DartComponentInfo> valueProcessor = (file, value) -> {
      if (!processor.process(value)) return false;
      return true;
    };

    return FileBasedIndex.getInstance().processValues(DART_COMPONENT_INDEX, componentName, null, valueProcessor, scope);
  }

  private static class MyDataIndexer implements DataIndexer<String, DartComponentInfo, FileContent> {
    @NotNull
    @Override
    public Map<String, DartComponentInfo> map(@NotNull FileContent inputData) {
      return DartIndexUtil.indexFile(inputData).getComponentInfoMap();
    }
  }
}
