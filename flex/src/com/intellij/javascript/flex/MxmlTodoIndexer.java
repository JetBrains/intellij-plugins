package com.intellij.javascript.flex;

import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.cache.impl.id.IdTableBuilding;
import com.intellij.psi.impl.cache.impl.idCache.XmlTodoIndexer;
import com.intellij.psi.impl.cache.impl.todo.TodoIndexEntry;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import com.intellij.util.indexing.FileContentImpl;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 2/17/12
 * Time: 9:32 PM
 */
public class MxmlTodoIndexer extends XmlTodoIndexer {
  @NotNull
  @Override
  public Map<TodoIndexEntry, Integer> map(final FileContent inputData) {
    final Map<TodoIndexEntry, Integer> map = new THashMap<TodoIndexEntry, Integer>(super.map(inputData));
    XmlBackedJSClassImpl.visitScriptTagInjectedFilesForIndexing((XmlFile)inputData.getPsiFile(),
                                                                new JSResolveUtil.JSInjectedFilesVisitor() {
                                                                  @Override
                                                                  protected void process(JSFile file) {
                                                                    VirtualFile injectedFile = file.getViewProvider().getVirtualFile();
                                                                    final DataIndexer<TodoIndexEntry, Integer, FileContent> indexer =
                                                                      IdTableBuilding.getTodoIndexer(file.getFileType(), injectedFile);
                                                                    if (indexer != null) {
                                                                      map.putAll(indexer.map(
                                                                        new FileContentImpl(injectedFile, file.getText(), null)));
                                                                    }
                                                                  }
                                                                }, false);
    return map;
  }
}
