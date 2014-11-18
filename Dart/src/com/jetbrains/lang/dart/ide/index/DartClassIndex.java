package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DartClassIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_CLASS_INDEX = ID.create("DartClassIndex");
  private static final int INDEX_VERSION = 2;
  private DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_CLASS_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
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

  public static Collection<String> getNames(Project project) {
    return FileBasedIndex.getInstance().getAllKeys(DART_CLASS_INDEX, project);
  }

  public static List<DartComponentName> getItemsByName(String name, Project project, GlobalSearchScope searchScope) {
    final Collection<VirtualFile> files =
      FileBasedIndex.getInstance().getContainingFiles(DART_CLASS_INDEX, name, searchScope);
    final Set<DartComponentName> result = new THashSet<DartComponentName>();
    for (VirtualFile vFile : files) {
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
      for (PsiElement root : DartResolveUtil.findDartRoots(psiFile)) {
        for (DartClass component : DartResolveUtil.getClassDeclarations(root)) {
          if (name.equals(component.getName())) {
            result.add(component.getComponentName());
          }
        }
      }
    }
    return new ArrayList<DartComponentName>(result);
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      DartFileIndexData indexData = DartIndexUtil.indexFile(inputData);
      final Map<String, Void> result = new THashMap<String, Void>();
      for (String componentName : indexData.getClassNames()) {
        result.put(componentName, null);
      }
      return result;
    }
  }
}
