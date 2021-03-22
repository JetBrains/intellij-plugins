package com.intellij.javascript.flex;

import com.intellij.javascript.flex.mxml.FlexXmlBackedMembersIndex;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.cache.impl.id.PlatformIdTableBuilding;
import com.intellij.psi.impl.cache.impl.idCache.XmlTodoIndexer;
import com.intellij.psi.impl.cache.impl.todo.TodoIndexEntry;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.FileContentImpl;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class MxmlTodoIndexer extends XmlTodoIndexer {
  @NotNull
  @Override
  public Map<TodoIndexEntry, Integer> map(@NotNull final FileContent inputData) {
    final Map<TodoIndexEntry, Integer> map = new THashMap<>(super.map(inputData));
    FlexXmlBackedMembersIndex.visitScriptTagInjectedFilesForIndexing((XmlFile)inputData.getPsiFile(),
                                                                     new JSResolveUtil.JSInjectedFilesVisitor() {
                                                                       @Override
                                                                       protected void process(JSFile file) {
                                                                         VirtualFile injectedFile = file.getViewProvider().getVirtualFile();
                                                                         final DataIndexer<TodoIndexEntry, Integer, FileContent> indexer =
                                                                           PlatformIdTableBuilding.getTodoIndexer(file.getFileType());
                                                                         if (indexer != null) {
                                                                           Map<TodoIndexEntry, Integer> injectedMap = indexer.map(
                                                                             FileContentImpl.createByText(injectedFile, file.getText(), inputData.getProject())
                                                                           );
                                                                           for (Map.Entry<TodoIndexEntry, Integer> e : injectedMap
                                                                             .entrySet()) {
                                                                             Integer integer = map.get(e.getKey());
                                                                             map.put(e.getKey(), integer == null
                                                                                                 ? e.getValue()
                                                                                                 : e.getValue() + integer);
                                                                           }
                                                                         }
                                                                       }
                                                                     }, false);
    return map;
  }
}
