package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PairProcessor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartComponentIndex extends FileBasedIndexExtension<String, DartComponentInfo> {
  public static final ID<String, DartComponentInfo> DART_COMPONENT_INDEX = ID.create("DartComponentIndex");
  private static final int INDEX_VERSION = 3;
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

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public DataExternalizer<DartComponentInfo> getValueExternalizer() {
    return myExternalizer;
  }

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
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  public static List<VirtualFile> getAllFiles(@NotNull Project project, @Nullable String componentName) {
    if (componentName == null) {
      return Collections.emptyList();
    }
    return new ArrayList<VirtualFile>(
      FileBasedIndex.getInstance().getContainingFiles(DART_COMPONENT_INDEX, componentName, GlobalSearchScope.allScope(project)));
  }

  public static void processAllComponents(@NotNull PsiElement contex,
                                          PairProcessor<String, DartComponentInfo> processor,
                                          Condition<String> nameFilter) {
    final Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(DART_COMPONENT_INDEX, contex.getProject());
    for (String componentName : allKeys) {
      if (nameFilter.value(componentName)) {
        continue;
      }
      final List<DartComponentInfo> allComponents = FileBasedIndex.getInstance().getValues(
        DART_COMPONENT_INDEX, componentName, contex.getResolveScope()
      );
      for (DartComponentInfo componentInfo : allComponents) {
        if (!processor.process(componentName, componentInfo)) return;
      }
    }
  }

  private static class MyDataIndexer implements DataIndexer<String, DartComponentInfo, FileContent> {
    @NotNull
    @Override
    public Map<String, DartComponentInfo> map(FileContent inputData) {
      return DartIndexUtil.indexFile(inputData).getComponentInfoMap();
    }
  }
}
