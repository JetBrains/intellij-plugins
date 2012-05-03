package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.FlexLibrarySet;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.css.CssFileType;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.messages.MessageBus;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.intellij.flex.uiDesigner.LogMessageUtil.LOG;

class ComplexRenderAction extends RenderActionQueue.RenderAction<AsyncResult<List<DocumentFactoryManager.DocumentInfo>>> {
  private Document[] documents;
  final boolean onlyStyle;

  ComplexRenderAction(Document[] documents, boolean onlyStyle) {
    super(null, null, new AsyncResult<List<DocumentFactoryManager.DocumentInfo>>());
    this.documents = documents;
    this.onlyStyle = onlyStyle;
  }

  @Override
  protected boolean isNeedEdt() {
    return false;
  }

  void merge(Document[] otherDocuments) {
    THashSet<Document> merged = new THashSet<Document>(documents.length + otherDocuments.length);
    Collections.addAll(merged, documents);
    Collections.addAll(merged, otherDocuments);
    documents = merged.toArray(new Document[merged.size()]);
  }

  @Override
  protected void doRun() {
    renderDocumentsAndCheckLocalStyleModification(result);
    result.doWhenDone(new AsyncResult.Handler<List<DocumentFactoryManager.DocumentInfo>>() {
      @Override
      public void run(List<DocumentFactoryManager.DocumentInfo> infos) {
        Application application = ApplicationManager.getApplication();
        if (application.isDisposed()) {
          return;
        }

        MessageBus messageBus = application.getMessageBus();
        for (DocumentFactoryManager.DocumentInfo info : infos) {
          messageBus.syncPublisher(DesignerApplicationManager.MESSAGE_TOPIC).documentRendered(info);
        }
      }
    });
  }

  private void renderDocumentsAndCheckLocalStyleModification(AsyncResult<List<DocumentFactoryManager.DocumentInfo>> result) {
    final Client client = Client.getInstance();
    final List<DocumentFactoryManager.DocumentInfo> documentInfos = new ArrayList<DocumentFactoryManager.DocumentInfo>(documents.length);
    final THashMap<ModuleInfo, List<LocalStyleHolder>> localStyleSources = new THashMap<ModuleInfo, List<LocalStyleHolder>>();
    collectChanges(documentInfos, localStyleSources);
    if (!localStyleSources.isEmpty()) {
      updateLocalStyleSources(client, localStyleSources);
    }

    client.renderDocumentAndDependents(documentInfos, localStyleSources, result);
  }

  private static void updateLocalStyleSources(final Client client, final THashMap<ModuleInfo, List<LocalStyleHolder>> localStyleSources) {
    final ProblemsHolder problemsHolder = new ProblemsHolder();
    final ProjectComponentReferenceCounter projectComponentReferenceCounter = new ProjectComponentReferenceCounter();
    localStyleSources.forEachEntry(new TObjectObjectProcedure<ModuleInfo, List<LocalStyleHolder>>() {
      @Override
      public boolean execute(ModuleInfo moduleInfo, List<LocalStyleHolder> b) {
        try {
          List<LocalStyleHolder> oldList = moduleInfo.getLocalStyleHolders();
          if (oldList == null) {
            oldList = Collections.emptyList();
          }

          FlexLibrarySet flexLibrarySet = moduleInfo.getFlexLibrarySet();
          final StringRegistry.StringWriter stringWriter = new StringRegistry.StringWriter();
          stringWriter.startChange();
          try {
            List<LocalStyleHolder> list = ModuleInfoUtil.collectLocalStyle(moduleInfo, flexLibrarySet.getVersion(), stringWriter,
                                                                           problemsHolder, projectComponentReferenceCounter,
                                                                           flexLibrarySet.assetCounterInfo.demanded);
            // todo we should't create list, we should check while collecting
            boolean hasChanges = true;
            if (list.size() == oldList.size()) {
              int diff = list.size();
              for (LocalStyleHolder holder : list) {
                if (oldList.contains(holder)) {
                  diff--;
                }
              }

              hasChanges = diff != 0;
            }

            if (hasChanges) {
              moduleInfo.setLocalStyleHolders(list);

              client.fillAssetClassPoolIfNeed(flexLibrarySet);
              client.updateLocalStyleHolders(localStyleSources, stringWriter);
              if (projectComponentReferenceCounter.hasUnregistered()) {
                client.registerDocumentReferences(projectComponentReferenceCounter.unregistered, null, problemsHolder);
              }
            }
            else {
              stringWriter.rollback();
              localStyleSources.remove(moduleInfo);
            }
          }
          catch (Throwable e) {
            stringWriter.rollback();
            LOG.error(e);
          }
        }
        catch (Throwable e) {
          LOG.error(e);
        }
        return true;
      }
    });

    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(null, problemsHolder);
    }
  }

  private void collectChanges(List<DocumentFactoryManager.DocumentInfo> documentInfos, THashMap<ModuleInfo, List<LocalStyleHolder>> localStyleSources) {
    final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    final DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance();
    final Client client = Client.getInstance();
    for (Document document : documents) {
      final VirtualFile file = fileDocumentManager.getFile(document);
      if (file == null) {
        continue;
      }

      boolean isMxml = JavaScriptSupportLoader.isFlexMxmFile(file);
      if (isMxml || file.getFileType() == CssFileType.INSTANCE) {
        if (!collectChangedLocalStyleSources(localStyleSources, file) && onlyStyle) {
          // if onlyStyle and we didn't find changed local style sources, so, it is new style source — we must collect style sources for appropriate module
          Project p = ProjectUtil.guessProjectForFile(file);
          if (p != null) {
            ModuleInfo info = client.getRegisteredModules().getNullableInfo(ModuleUtil.findModuleForFile(file, p));
            if (info != null) {
              localStyleSources.put(info, Collections.<LocalStyleHolder>emptyList());
            }
          }
        }
      }

      final DocumentFactoryManager.DocumentInfo info = isMxml ? documentFactoryManager.getNullableInfo(file) : null;
      if (info == null) {
        continue;
      }
      else if (onlyStyle) {
        info.documentModificationStamp = document.getModificationStamp();
        continue;
      }

      if (info.documentModificationStamp == document.getModificationStamp()) {
        continue;
      }

      final Project project = ProjectUtil.guessProjectForFile(file);
      if (project == null) {
        continue;
      }

      final Module module = ModuleUtil.findModuleForFile(file, project);
      if (module == null) {
        continue;
      }

      final XmlFile psiFile;
      final AccessToken token = ReadAction.start();
      try {
        psiFile = (XmlFile)PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) {
          continue;
        }
      }
      finally {
        token.finish();
      }

      if (client.updateDocumentFactory(info.getId(), module, psiFile)) {
        info.documentModificationStamp = document.getModificationStamp();
        documentInfos.add(info);
      }
    }
  }

  private static boolean collectChangedLocalStyleSources(final THashMap<ModuleInfo, List<LocalStyleHolder>> holders,
                                                      final VirtualFile file) {
    final Ref<Boolean> result = new Ref<Boolean>(false);
    Client.getInstance().getRegisteredModules().forEach(new TObjectProcedure<ModuleInfo>() {
      @Override
      public boolean execute(ModuleInfo moduleInfo) {
        if (holders.containsKey(moduleInfo)) {
          result.set(true);
          return false;
        }

        List<LocalStyleHolder> styleHolders = moduleInfo.getLocalStyleHolders();
        if (styleHolders != null) {
          List<LocalStyleHolder> list = null;
          for (LocalStyleHolder styleHolder : styleHolders) {
            if (styleHolder.file.equals(file)) {
              if (list == null) {
                list = new ArrayList<LocalStyleHolder>();
                holders.put(moduleInfo, list);
                result.set(true);
              }

              list.add(styleHolder);
            }
          }

          if (list != null) {
            // well, local style applicable only for one module, so,
            // if we found for this module, there is no reason to continue search
            return false;
          }
        }

        return true;
      }
    });

    return result.get();
  }
}
