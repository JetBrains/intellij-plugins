package org.angularjs.index;

import com.intellij.lang.javascript.index.JSEntryIndex;
import com.intellij.lang.javascript.index.JSIndexEntry;
import com.intellij.lang.javascript.index.JSNamedElementProxy;
import com.intellij.lang.javascript.index.JavaScriptIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.intellij.util.indexing.ID;

/**
 * @author Dennis.Ushakov
 */
public class AngularIndexUtil {
  public static final int BASE_VERSION = 2;

  public static JSNamedElementProxy resolve(final Project project, final ID<String, Void> index, final String lookupKey) {
    final Ref<JSNamedElementProxy> result = Ref.create();
    final GlobalSearchScope scope = GlobalSearchScope.allScope(project);
    for (VirtualFile file : FileBasedIndex.getInstance().getContainingFiles(index, lookupKey, scope)) {
      final int id = FileBasedIndex.getFileId(file);
      FileBasedIndex.getInstance().processValues(JSEntryIndex.INDEX_ID, id, null, new FileBasedIndex.ValueProcessor<JSIndexEntry>() {
        @Override
        public boolean process(VirtualFile file, JSIndexEntry value) {
          return value.processAdditionalData(JavaScriptIndex.getInstance(project), index.toString(), lookupKey, new Processor<JSNamedElementProxy>() {
            @Override
            public boolean process(JSNamedElementProxy proxy) {
              if (lookupKey.equals(proxy.getName())) {
                result.set(proxy);
              }
              return result.isNull();
            }
          });
        }
      }, scope);
      if (!result.isNull()) break;
    }
    return result.get();
  }
}
