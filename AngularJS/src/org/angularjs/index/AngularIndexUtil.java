package org.angularjs.index;

import com.intellij.ProjectTopics;
import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.ParameterizedCachedValue;
import com.intellij.psi.util.ParameterizedCachedValueProvider;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dennis.Ushakov
 */
public class AngularIndexUtil {
  public static final int BASE_VERSION = 29;
  private static final Key<NotNullLazyValue<ModificationTracker>> TRACKER = Key.create("angular.js.tracker");
  private static final ConcurrentMap<String, Key<ParameterizedCachedValue<Collection<String>, Pair<Project, ID<String, ?>>>>> ourCacheKeys =
    ContainerUtil.newConcurrentMap();
  private static final AngularKeysProvider PROVIDER = new AngularKeysProvider();

  public static JSImplicitElement resolve(final Project project, final StubIndexKey<String, JSImplicitElementProvider> index, final String lookupKey) {
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final Ref<JSImplicitElement> result = new Ref<JSImplicitElement>(null);
    StubIndex.getInstance().processElements(
      index, lookupKey, project, scope, JSImplicitElementProvider.class, new Processor<JSImplicitElementProvider>() {
        @Override
        public boolean process(JSImplicitElementProvider provider) {
          final JSElementIndexingData indexingData = provider.getIndexingData();
          if (indexingData != null) {
            final Collection<JSImplicitElement> elements = indexingData.getImplicitElements();
            if (elements != null) {
              for (JSImplicitElement element : elements) {
                if (element.getName().equals(lookupKey) && (index == AngularInjectionDelimiterIndex.KEY ||
                                                            AngularJSIndexingHandler.isAngularRestrictions(element.getTypeString()))) {
                  result.set(element);
                  if (element.canNavigate()) {
                    return false;
                  }
                }
              }
            }
          }
          return true;
        }
      }
    );

    return result.get();
  }

  public static Collection<String> getAllKeys(final ID<String, ?> index, final Project project) {
    final String indexId = index.toString();
    final Key<ParameterizedCachedValue<Collection<String>, Pair<Project, ID<String, ?>>>> key =
      ConcurrencyUtil.cacheOrGet(ourCacheKeys, indexId, Key.<ParameterizedCachedValue<Collection<String>, Pair<Project, ID<String, ?>>>>create("angularjs.index." + indexId));
    final Pair<Project, ID<String, ?>> pair = Pair.<Project, ID<String, ?>>create(project, index);
    return CachedValuesManager.getManager(project).getParameterizedCachedValue(project, key, PROVIDER, false, pair);
  }

  public static boolean hasAngularJS(final Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode() && "disabled".equals(System.getProperty("angular.js"))) return false;
    return getAngularJSVersion(project) > 0;
  }

  public static boolean hasAngularJS2(final Project project) {
    if (ApplicationManager.getApplication().isUnitTestMode() && "disabled".equals(System.getProperty("angular.js"))) return false;
    return getAngularJSVersion(project) >= 20;
  }

  private static int getAngularJSVersion(final Project project) {
    if (DumbService.isDumb(project)) return -1;
    NotNullLazyValue<ModificationTracker> tracker = project.getUserData(TRACKER);
    if (tracker == null) {
      tracker = new AtomicNotNullLazyValue<ModificationTracker>() {
        @NotNull
        @Override
        protected ModificationTracker compute() {
          return new AngularModificationTracker(project);
        }
      };
      tracker = ((UserDataHolderEx)project).putUserDataIfAbsent(TRACKER, tracker);
    }

    final NotNullLazyValue<ModificationTracker> finalTracker = tracker;
    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<Integer>() {
      @Nullable
      @Override
      public Result<Integer> compute() {
        int version = -1;
        PsiElement resolve;
        if ((resolve = resolve(project, AngularDirectivesIndex.KEY, "[ngFor]")) != null) {
          version = 20;
        } else if ((resolve = resolve(project, AngularDirectivesIndex.KEY, "ng-messages")) != null) {
          version = 13;
        } else if ((resolve = resolve(project, AngularDirectivesIndex.KEY, "ng-model")) != null) {
          version = 12;
        }
        return Result.create(version, resolve != null ? resolve.getContainingFile() : finalTracker.getValue());
      }
    });
  }

  public static String convertRestrictions(final Project project, String restrictions) {
    if (AngularJSIndexingHandler.DEFAULT_RESTRICTIONS.equals(restrictions)) {
      return getAngularJSVersion(project) >= 13 ? "AE" : "A";
    }
    return restrictions;
  }

  private static class AngularKeysProvider implements ParameterizedCachedValueProvider<Collection<String>, Pair<Project, ID<String, ?>>> {
    @Nullable
    @Override
    public CachedValueProvider.Result<Collection<String>> compute(final Pair<Project, ID<String, ?>> projectAndIndex) {
      final Project project = projectAndIndex.first;
      final ID<String, ?> id = projectAndIndex.second;
      final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
      final FileBasedIndex fileIndex = FileBasedIndex.getInstance();
      final StubIndex stubIndex = StubIndex.getInstance();
      final Collection<String> allKeys =
        id instanceof StubIndexKey ? stubIndex.getAllKeys((StubIndexKey<String, ?>)id, project) :
        fileIndex.getAllKeys(id, project);

      return CachedValueProvider.Result.<Collection<String>>create(ContainerUtil.filter(allKeys, new Condition<String>() {
        @Override
        public boolean value(String key) {
          return id instanceof StubIndexKey ?
                 !stubIndex.processElements((StubIndexKey<String, PsiElement>)id, key, project, scope, PsiElement.class,
                                            new Processor<PsiElement>() {
                                              @Override
                                              public boolean process(PsiElement element) {
                                                return false;
                                              }
                                            }) :
                 !fileIndex.processValues(id, key, null, new FileBasedIndex.ValueProcessor() {
                   @Override
                   public boolean process(VirtualFile file, Object value) {
                     return false;
                   }
                 }, scope);
        }
      }), PsiManager.getInstance(project).getModificationTracker());
    }
  }

  private static class AngularModificationTracker extends SimpleModificationTracker {
    public AngularModificationTracker(final Project project) {
      VirtualFileManager.getInstance().addVirtualFileListener(new VirtualFileAdapter() {
        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
          incModificationCount();
        }
      }, project);
      project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter() {
        @Override
        public void rootsChanged(ModuleRootEvent event) {
          incModificationCount();
        }
      });
    }
  }
}
