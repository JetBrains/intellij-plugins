package org.angularjs.index;

import com.intellij.lang.javascript.index.AngularJSIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.TObjectIntHashMap;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Dennis.Ushakov
 */
public class AngularIndexUtil {
  public static class Entry {
    public final VirtualFile file;
    public final String name;
    public final int offset;

    public Entry(final VirtualFile _file, final String _name, final int _offset) {
      file = _file;
      name = _name;
      offset = _offset;
    }
  }

  public static List<Entry> collect(final Project project, final String indexKey) {
    final List<Entry> result = new LinkedList<Entry>();
    FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, indexKey, null,
                                               new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                 @Override
                                                 public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                   for (Object o : descriptorNames.keys()) {
                                                     result.add(new Entry(file, (String)o, descriptorNames.get((String)o)));
                                                   }
                                                   return true;
                                                 }
                                               }, GlobalSearchScope.allScope(project)
    );
    return result;
  }

  public static Entry resolve(final Project project, final String indexKey, final String lookupKey) {
    final Ref<Entry> result = Ref.create();
    FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, indexKey, null,
                                               new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                 @Override
                                                 public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                   for (Object o : descriptorNames.keys()) {
                                                     if (lookupKey.equals(o)) {
                                                       result.set(new Entry(file, lookupKey, descriptorNames.get(lookupKey)));
                                                     }
                                                   }
                                                   return result.isNull();
                                                 }
                                               }, GlobalSearchScope.allScope(project)
    );
    return result.get();
  }
}
