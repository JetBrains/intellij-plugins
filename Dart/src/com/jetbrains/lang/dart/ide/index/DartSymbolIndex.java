package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DartSymbolIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_SYMBOL_INDEX = ID.create("DartSymbolIndex");
  private DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_SYMBOL_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
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
    return FileBasedIndex.getInstance().getAllKeys(DART_SYMBOL_INDEX, project);
  }

  public static List<DartComponentName> getItemsByName(final String name, Project project, GlobalSearchScope searchScope) {
    final Collection<VirtualFile> files =
      FileBasedIndex.getInstance().getContainingFiles(DART_SYMBOL_INDEX, name, searchScope);
    final Set<DartComponentName> result = new THashSet<DartComponentName>();
    for (VirtualFile vFile : files) {
      final PsiFile psiFile = PsiManager.getInstance(project).findFile(vFile);
      for (PsiElement root : DartResolveUtil.findDartRoots(psiFile)) {
        processComponents(root, new PsiElementProcessor<DartComponent>() {
          @Override
          public boolean execute(@NotNull DartComponent component) {
            if (name.equals(component.getName())) {
              result.add(component.getComponentName());
            }
            return true;
          }
        });
      }
    }
    return new ArrayList<DartComponentName>(result);
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      List<String> symbols = DartIndexUtil.indexFile(inputData).getSymbols();
      final Map<String, Void> result = new THashMap<String, Void>();
      for (String symbol : symbols) {
        result.put(symbol, null);
      }
      return result;
    }
  }

  private static void processComponents(PsiElement context, PsiElementProcessor<DartComponent> processor) {
    // top-level components
    final DartComponent[] components = PsiTreeUtil.getChildrenOfType(context, DartComponent.class);
    if (components != null) {
      for (DartComponent component : components) {
        if (!processComponent(processor, component)) {
          return;
        }
      }
    }
    // top-level variables
    final DartVarDeclarationList[] varLists = PsiTreeUtil.getChildrenOfType(context, DartVarDeclarationList.class);
    if (varLists != null) {
      for (DartVarDeclarationList varList : varLists) {
        if (!processComponent(processor, varList.getVarAccessDeclaration())) {
          return;
        }
        for (DartVarDeclarationListPart part : varList.getVarDeclarationListPartList()) {
          if (!processComponent(processor, part)) {
            return;
          }
        }
      }
    }
  }

  private static boolean processComponent(PsiElementProcessor<DartComponent> processor, DartComponent component) {
    final String componentName = component.getName();
    if (componentName == null) {
      return true;
    }
    if (!processor.execute(component)) {
      return false;
    }
    if (component instanceof DartClass) {
      for (DartComponent subComponent : DartResolveUtil.getNamedSubComponents((DartClass)component)) {
        if (subComponent.isConstructor() && componentName.equals(subComponent.getName())) {
          continue;
        }
        if (!processor.execute(subComponent)) {
          return false;
        }
      }
    }
    return true;
  }
}
