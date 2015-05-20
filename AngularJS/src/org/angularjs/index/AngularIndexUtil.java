package org.angularjs.index;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.*;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Dennis.Ushakov
 */
public class AngularIndexUtil {
  public static final int BASE_VERSION = 20;
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

  private static int getAngularJSVersion(final Project project) {
    if (DumbService.isDumb(project)) return -1;
    return CachedValuesManager.getManager(project).getCachedValue(project, new CachedValueProvider<Integer>() {
      @Nullable
      @Override
      public Result<Integer> compute() {
        int version = -1;
        if (resolve(project, AngularDirectivesIndex.KEY, "non-bindable") != null) {
          version = 20;
        } else if (resolve(project, AngularDirectivesIndex.KEY, "ng-messages") != null) {
          version = 13;
        } else if (resolve(project, AngularDirectivesIndex.KEY, "ng-model") != null) {
          version = 12;
        }
        return Result.create(version, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
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
      final Collection<String> allKeys =
        id instanceof StubIndexKey ? StubIndex.getInstance().getAllKeys((StubIndexKey<String, ?>)id, project) :
        FileBasedIndex.getInstance().getAllKeys(id, project);
      return CachedValueProvider.Result.create(allKeys, PsiManager.getInstance(project).getModificationTracker());
    }
  }
}
