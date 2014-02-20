package org.angularjs.index;

import com.intellij.lang.javascript.index.JSEntryIndex;
import com.intellij.lang.javascript.index.JSIndexEntry;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.CommonProcessors;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;
import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class AngularIndexUtil {
  public static final int BASE_VERSION = 8;

  public static JSNamedElementProxy resolve(final Project project, final ID<String, Void> index, final String lookupKey) {
    final Ref<JSNamedElementProxy> result = Ref.create();
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    for (VirtualFile file : FileBasedIndex.getInstance().getContainingFiles(index, lookupKey, scope)) {
      final int id = FileBasedIndex.getFileId(file);
      if (FileBasedIndex.getInstance().processValues(JSEntryIndex.INDEX_ID, id, null, new FileBasedIndex.ValueProcessor<JSIndexEntry>() {
        @Override
        public boolean process(VirtualFile file, JSIndexEntry value) {
          result.set(value.resolveAdditionalData(JavaScriptIndex.getInstance(project), index.toString(), lookupKey));
          return result.isNull();
        }
      }, scope)) {
        break;
      }
    }
    return result.get();
  }

  public static Collection<String> getAllKeys(final ID<String, Void> index, final Project project) {
    return getAllKeys(index, project, true);
  }

  public static Collection<String> getAllKeys(final ID<String, Void> index, final Project project, final boolean checkExisting) {
    Set<String> allKeys = new THashSet<String>();
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    final CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<String>(allKeys) {
      @Override
      protected boolean accept(String key) {
        return true;
      }
    };
    FileBasedIndex.getInstance().processAllKeys(index, processor, scope, null);
    return checkExisting ? ContainerUtil.filter(allKeys, new Condition<String>() {
      @Override
      public boolean value(String key) {
        return !FileBasedIndex.getInstance().processValues(index, key, null, new FileBasedIndex.ValueProcessor<Void>() {
          @Override
          public boolean process(VirtualFile file, Void value) {
            return false;
          }
        }, scope);
      }
    }) : allKeys;
  }

  public static boolean hasAngularJS(Project project) {
    return !DumbService.isDumb(project) && resolve(project, AngularDirectivesIndex.INDEX_ID, "ng-model") != null;
  }
}
