package org.angularjs.index;

import com.intellij.lang.javascript.psi.impl.JSOffsetBasedImplicitElement;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.*;
import com.intellij.util.ConcurrencyUtil;
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
  public static final int BASE_VERSION = 17;
  private static final ConcurrentMap<String, Key<ParameterizedCachedValue<Collection<String>, Pair<Project, ID<String, ?>>>>> ourCacheKeys =
    ContainerUtil.newConcurrentMap();
  private static final AngularKeysProvider PROVIDER = new AngularKeysProvider();

  public static JSOffsetBasedImplicitElement resolve(final Project project, final ID<String, byte[]> index, final String lookupKey) {
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final Ref<JSOffsetBasedImplicitElement> result = new Ref<JSOffsetBasedImplicitElement>(null);
    FileBasedIndex.getInstance().processValues(index, lookupKey, null, new FileBasedIndex.ValueProcessor<byte[]>() {
      @Override
      public boolean process(VirtualFile file, byte[] value) {
        final Trinity<Boolean, Integer, String> deserialized = AngularJSIndexingHandler.deserializeDataValue(value);
        final JSImplicitElement.Type type = deserialized.first ? JSImplicitElement.Type.Class : JSImplicitElement.Type.Tag;
        final JSImplicitElementImpl.Builder builder = new JSImplicitElementImpl.Builder(lookupKey, null)
          .setType(type)
          .setTypeString(deserialized.third);
        final PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        if (psiFile != null) {
          final JSOffsetBasedImplicitElement element = new JSOffsetBasedImplicitElement(builder, deserialized.second, psiFile);
          result.set(element);
          if (element.canNavigate()) return false;
        }
        return true;
      }
    }, scope);

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
        PsiElement resolve;
        if ((resolve = resolve(project, AngularDirectivesIndex.INDEX_ID, "ng-messages")) != null) {
          version = 13;
        } else if ((resolve = resolve(project, AngularDirectivesIndex.INDEX_ID, "ng-model")) != null) {
          version = 12;
        }
        return Result.create(version, resolve != null ? resolve.getContainingFile() : PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
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
      return CachedValueProvider.Result.create(FileBasedIndex.getInstance().getAllKeys(id, project),
                                               PsiManager.getInstance(project).getModificationTracker());
    }
  }
}
