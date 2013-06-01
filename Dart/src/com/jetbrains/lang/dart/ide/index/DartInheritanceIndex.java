package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DefinitionsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.DartComponentType;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartInheritanceIndex extends FileBasedIndexExtension<String, List<DartComponentInfo>> {
  public static final ID<String, List<DartComponentInfo>> DART_INHERITANCE_INDEX = ID.create("DartInheritanceIndex");
  private static final int INDEX_VERSION = 2;
  private final DataIndexer<String, List<DartComponentInfo>, FileContent> myIndexer = new MyDataIndexer();
  private final DataExternalizer<List<DartComponentInfo>> myExternalizer = new DartComponentInfoListExternalizer();

  @NotNull
  @Override
  public ID<String, List<DartComponentInfo>> getName() {
    return DART_INHERITANCE_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public DataExternalizer<List<DartComponentInfo>> getValueExternalizer() {
    return myExternalizer;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @NotNull
  @Override
  public DataIndexer<String, List<DartComponentInfo>, FileContent> getIndexer() {
    return myIndexer;
  }

  private static class MyDataIndexer implements DataIndexer<String, List<DartComponentInfo>, FileContent> {
    @Override
    @NotNull
    public Map<String, List<DartComponentInfo>> map(final FileContent inputData) {
      return DartIndexUtil.indexFile(inputData).getInheritorsMap();
    }
  }

  public static List<DartClass> getItemsByName(final DartClass dartClass) {
    final List<DartClass> result = new ArrayList<DartClass>();
    DefinitionsSearch.search(dartClass).forEach(new Processor<PsiElement>() {
      @Override
      public boolean process(PsiElement element) {
        if (element instanceof DartClass) {
          result.add((DartClass)element);
        }
        return true;
      }
    });
    return result;
  }

  public static class DefinitionsSearchExecutor implements QueryExecutor<PsiElement, PsiElement> {
    @Override
    public boolean execute(@NotNull final PsiElement queryParameters, @NotNull final Processor<PsiElement> consumer) {
      return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {
        public Boolean compute() {
          final PsiElement queryParametersParent = queryParameters.getParent();
          DartComponent dartComponent;
          if (queryParameters instanceof DartClass) {
            dartComponent = (DartClass)queryParameters;
          }
          else if (queryParametersParent instanceof DartComponent && queryParameters instanceof DartComponentName) {
            dartComponent = (DartComponent)queryParametersParent;
          }
          else {
            return true;
          }
          if (dartComponent instanceof DartClass) {
            processInheritors((DartClass)dartComponent, queryParameters, consumer);
          }
          else if (DartComponentType.typeOf(dartComponent) == DartComponentType.METHOD) {
            final String nameToFind = dartComponent.getName();
            if (nameToFind == null) return true;

            DartClass dartClass = PsiTreeUtil.getParentOfType(dartComponent, DartClass.class);
            assert dartClass != null;

            processInheritors(dartClass, queryParameters, new Processor<PsiElement>() {
              @Override
              public boolean process(PsiElement element) {
                for (DartComponent subDartNamedComponent : DartResolveUtil.getNamedSubComponents((DartClass)element)) {
                  if (nameToFind.equals(subDartNamedComponent.getName())) {
                    consumer.process(subDartNamedComponent);
                  }
                }
                return true;
              }
            });
          }
          return true;
        }
      });
    }

    private static boolean processInheritors(final DartClass dartClass, final PsiElement context, final Processor<PsiElement> consumer) {
      final Set<DartClass> classSet = new THashSet<DartClass>();
      final LinkedList<DartClass> namesQueue = new LinkedList<DartClass>();
      namesQueue.add(dartClass);
      while (!namesQueue.isEmpty()) {
        final DartClass currentClass = namesQueue.pollFirst();
        final String currentClassName = currentClass.getName();
        if (currentClassName == null || !classSet.add(currentClass)) {
          continue;
        }
        final Collection<VirtualFile> files =
          DartIndexUtil.getContainingFiles(DART_INHERITANCE_INDEX, currentClassName, GlobalSearchScope.allScope(context.getProject()));
        for (VirtualFile virtualFile : files) {
          PsiFile psiFile = dartClass.getManager().findFile(virtualFile);
          for (PsiElement root : DartResolveUtil.findDartRoots(psiFile)) {
            for (DartClass subClass : DartResolveUtil.findClassesByParent(currentClass, root)) {
              if (!consumer.process(subClass)) {
                return true;
              }
              namesQueue.add(subClass);
            }
          }
        }
      }
      return true;
    }
  }
}
