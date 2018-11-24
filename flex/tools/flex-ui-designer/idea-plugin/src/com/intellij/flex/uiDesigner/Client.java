package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.ClassPoolGenerator;
import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.BlockDataOutputStream;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.libraries.*;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import gnu.trove.THashMap;
import gnu.trove.TObjectObjectProcedure;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.InfoMap;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

public class Client implements Disposable {
  protected final BlockDataOutputStream blockOut = new BlockDataOutputStream();
  protected final AmfOutputStream out = new AmfOutputStream(blockOut);

  private final InfoMap<Module, ModuleInfo> registeredModules = new InfoMap<>(true);
  private final InfoMap<Project, ProjectInfo> registeredProjects = new InfoMap<>();

  private final ReentrantLock outLock = new ReentrantLock();

  public static Client getInstance() {
    return DesignerApplicationManager.getService(Client.class);
  }

  public void setOut(@NotNull OutputStream socketOut) {
    blockOut.setOut(socketOut);
  }

  public boolean isModuleRegistered(Module module) {
    return registeredModules.contains(module);
  }

  public InfoMap<Project, ProjectInfo> getRegisteredProjects() {
    return registeredProjects;
  }

  public InfoMap<Module, ModuleInfo> getRegisteredModules() {
    return registeredModules;
  }

  @NotNull
  public Module getModule(int id) {
    return registeredModules.getElement(id);
  }

  @NotNull
  public Project getProject(int id) {
    return registeredProjects.getElement(id);
  }

  @Override
  public void dispose() {
    registeredModules.dispose();
  }

  public boolean flush() {
    try {
      out.flush();
      return true;
    }
    catch (IOException e) {
      LogMessageUtil.processInternalError(e);
    }

    return false;
  }

  private void beginMessage(ClientMethod method) {
    beginMessage(method, null, null, null);
  }

  private void beginMessage(ClientMethod method, ActionCallback callback) {
    beginMessage(method, callback, null, null);
  }

  private void beginMessage(ClientMethod method,
                            @Nullable ActionCallback callback,
                            @Nullable ActionCallback rejectedCallback,
                            @Nullable Runnable doneRunnable) {
    outLock.lock();
    if (callback != null) {
      if (rejectedCallback != null) {
        callback.notifyWhenRejected(rejectedCallback);
      }
      if (doneRunnable != null) {
        callback.doWhenDone(doneRunnable);
      }
    }

    blockOut.assertStart();
    out.write(ClientMethod.METHOD_CLASS);
    out.write(callback == null ? 0 : SocketInputHandler.getInstance().addCallback(callback));
    out.write(method);
  }

  public void openProject(Project project) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.openProject);
      writeId(project);
      out.writeAmfUtf(project.getName());
      ProjectWindowBounds.write(project, out);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError);
    }
  }

  public void closeProject(final Project project) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.closeProject);
      writeId(project);
      hasError = false;
    }
    finally {
      try {
        finalizeMessageAndFlush(hasError);
      }
      finally {
        unregisterProject(project);
      }
    }
  }

  public void unregisterProject(final Project project) {
    DocumentFactoryManager.getInstance().unregister(project);
    
    registeredProjects.remove(project);
    if (registeredProjects.isEmpty()) {
      registeredModules.clear();
    }
    else {
      registeredModules.remove(new TObjectObjectProcedure<Module, ModuleInfo>() {
        @Override
        public boolean execute(Module module, ModuleInfo info) {
          return module.getProject() != project;
        }
      });
    }
  }

  public ActionCallback unregisterModule(final Module module) {
    boolean hasError = true;
    final ActionCallback callback = new ActionCallback("renderDocumentAndDependents");
    try {
      hasError = false;
      beginMessage(ClientMethod.unregisterModule, callback, null, () -> {
        try {
          SocketInputHandler.getInstance().unregisterDocumentFactories();
        }
        catch (IOException e) {
          LogMessageUtil.LOG.error(e);
        }
      });
      writeId(module);
    }
    finally {
      try {
        registeredModules.remove(module);
      }
      finally {
        finalizeMessageAndFlush(hasError, callback);
      }
    }

    return callback;
  }

  public void updateStringRegistry(StringRegistry.StringWriter stringWriter) throws IOException {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.updateStringRegistry);
      stringWriter.writeTo(out);
      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public void registerLibrarySet(LibrarySet librarySet) {
    final List<Library> styleOwners = new ArrayList<>();
    for (Library library : librarySet.getLibraries()) {
      if (library.isStyleOwner()) {
        styleOwners.add(library);
      }
    }

    boolean hasError = true;
    try {
      beginMessage(ClientMethod.registerLibrarySet);
      out.writeUInt29(librarySet.getId());
      out.write(librarySet instanceof FlexLibrarySet);

      LibrarySet parent = librarySet.getParent();
      out.writeShort(parent == null ? -1 : parent.getId());

      out.write(styleOwners.size());
      final LibraryManager libraryManager = LibraryManager.getInstance();
      for (Library library : styleOwners) {
        final boolean registered = libraryManager.isRegistered(library);
        out.write(registered);

        if (registered) {
          out.writeUInt29(library.getId());
        }
        else {
          out.writeUInt29(libraryManager.add(library));
          writeVirtualFile(library.getFile(), out);

          if (library.inheritingStyles == null) {
            out.writeShort(0);
          }
          else {
            out.write(library.inheritingStyles);
          }

          if (library.defaultsStyle == null) {
            out.write(0);
          }
          else {
            out.write(1);
            out.write(library.defaultsStyle);
          }
        }
      }

      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public void registerModule(Project project, ModuleInfo moduleInfo, StringRegistry.StringWriter stringWriter) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.registerModule);
      stringWriter.writeToIfStarted(out);

      out.writeShort(registeredModules.add(moduleInfo));
      writeId(project);
      out.writeShort(moduleInfo.getLibrarySet().getId());
      out.write(moduleInfo.isApp());
      out.write(moduleInfo.getLocalStyleHolders(), "lsh", true, true);
      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
      if (hasError) {
        stringWriter.rollback();
      }
    }
  }

  public void renderDocument(Module module, XmlFile psiFile) {
    renderDocument(module, psiFile, new ProblemsHolder());
  }

  /**
   * final, full render document - responsible for handle problemsHolder and assetCounter - you must not do it
   */
  public AsyncResult<DocumentInfo> renderDocument(Module module, XmlFile psiFile, ProblemsHolder problemsHolder) {
    VirtualFile virtualFile = psiFile.getVirtualFile();
    final int factoryId = registerDocumentFactoryIfNeed(module, psiFile, virtualFile, false, problemsHolder);
    final AsyncResult<DocumentInfo> result = new AsyncResult<>();
    if (factoryId == -1) {
      result.setRejected();
      return result;
    }

    FlexLibrarySet flexLibrarySet = registeredModules.getInfo(module).getFlexLibrarySet();
    if (flexLibrarySet != null) {
      fillAssetClassPoolIfNeed(flexLibrarySet);
    }

    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }

    final ActionCallback callback = new ActionCallback("renderDocument");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.renderDocument, callback, result,
                   () -> result.setDone(DocumentFactoryManager.getInstance().getInfo(factoryId)));

      out.writeShort(factoryId);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError);
    }

    return result;
  }

  public void fillAssetClassPoolIfNeed(FlexLibrarySet librarySet) {
    final AssetCounterInfo assetCounterInfo = librarySet.assetCounterInfo;
    int diff = assetCounterInfo.demanded.imageCount - assetCounterInfo.allocated.imageCount;
    if (diff > 0) {
      // reduce number of call fill asset class pool
      diff *= 2;
      fillAssetClassPool(librarySet, diff, ClassPoolGenerator.Kind.IMAGE);
      assetCounterInfo.allocated.imageCount += diff;
    }

    diff = assetCounterInfo.demanded.swfCount - assetCounterInfo.allocated.swfCount;
    if (diff > 0) {
      // reduce number of call fill asset class pool
      diff *= 2;
      fillAssetClassPool(librarySet, diff, ClassPoolGenerator.Kind.SWF);
      assetCounterInfo.allocated.swfCount += diff;
    }

    diff = assetCounterInfo.demanded.viewCount - assetCounterInfo.allocated.viewCount;
    if (diff > 0) {
      // reduce number of call fill asset class pool
      diff *= 2;
      fillAssetClassPool(librarySet, diff, ClassPoolGenerator.Kind.SPARK_VIEW);
      assetCounterInfo.allocated.viewCount += diff;
    }
  }

  private void fillAssetClassPool(FlexLibrarySet flexLibrarySet, int classCount, ClassPoolGenerator.Kind kind) {
    boolean hasError = true;
    try {
      if (kind == ClassPoolGenerator.Kind.IMAGE) {
        beginMessage(ClientMethod.fillImageClassPool);
      }
      else {
        beginMessage(kind == ClassPoolGenerator.Kind.SWF ? ClientMethod.fillSwfClassPool : ClientMethod.fillViewClassPool);
      }

      writeId(flexLibrarySet.getId());
      out.writeShort(classCount);
      ClassPoolGenerator.generate(kind, classCount, flexLibrarySet.assetCounterInfo.allocated, blockOut);
      hasError = false;
    }
    catch (Throwable e) {
      LogMessageUtil.processInternalError(e, null);
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  private void finalizeMessage(boolean hasError) {
    try {
      if (hasError) {
        blockOut.rollback();
      }
      else {
        try {
          blockOut.end();
        }
        catch (IOException e) {
          LogMessageUtil.processInternalError(e);
        }
      }

      out.resetAfterMessage();
    }
    finally {
      outLock.unlock();
    }
  }

  private void finalizeMessageAndFlush(boolean hasError) {
    finalizeMessageAndFlush(hasError, null);
  }

  private void finalizeMessageAndFlush(boolean hasError, @Nullable ActionCallback callback) {
    if (hasError) {
      try {
        blockOut.rollback();
      }
      finally {
        outLock.unlock();
        if (callback != null) {
          callback.setRejected();
        }
      }
    }
    else {
      try {
        out.flush();
      }
      catch (IOException e) {
        if (callback != null) {
          callback.setRejected();
        }
        LogMessageUtil.processInternalError(e);
      }
      finally {
        outLock.unlock();
      }
    }
  }

  public void updateLocalStyleHolders(THashMap<ModuleInfo, List<LocalStyleHolder>> holders, StringRegistry.StringWriter stringWriter) {
    boolean hasError = false;
    try {
      beginMessage(ClientMethod.updateLocalStyleHolders);
      stringWriter.writeTo(out);
      out.writeUInt29(holders.size());
      holders.forEachKey(new TObjectProcedure<ModuleInfo>() {
        @Override
        public boolean execute(ModuleInfo moduleInfo) {
          out.writeUInt29(moduleInfo.getId());
          out.write(moduleInfo.getLocalStyleHolders(), "lsh", true, true);
          return true;
        }
      });
    }
    catch (Throwable e) {
      hasError = true;
      stringWriter.rollback();
      LogMessageUtil.processInternalError(e);
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public boolean updateDocumentFactory(int factoryId, Module module, XmlFile psiFile, boolean reportProblems) {
    try {
      beginMessage(ClientMethod.updateDocumentFactory);
      out.writeShort(factoryId);

      final ProblemsHolder problemsHolder = new ProblemsHolder();
      boolean result = writeDocumentFactory(DocumentFactoryManager.getInstance().getInfo(factoryId), module, psiFile, problemsHolder);
      if (!problemsHolder.isEmpty() && reportProblems) {
        DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
      }
      if (result) {
        return true;
      }
    }
    catch (Throwable e) {
      LogMessageUtil.processInternalError(e, psiFile.getVirtualFile());
    }
    finally {
      outLock.unlock();
    }

    blockOut.rollback();
    return false;
  }

  public AsyncResult<List<DocumentInfo>> renderDocumentAndDependents(@Nullable List<DocumentInfo> infos,
                                                                     THashMap<ModuleInfo, List<LocalStyleHolder>> outdatedLocalStyleHolders,
                                                                     final AsyncResult<List<DocumentInfo>> result) {
    if ((infos == null || infos.isEmpty()) && outdatedLocalStyleHolders.isEmpty()) {
      result.setDone(infos);
      return result;
    }

    final ActionCallback callback = new ActionCallback("renderDocumentAndDependents");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.renderDocumentsAndDependents, callback, result, () -> {
        final int[] ids;
        try {
          ids = SocketInputHandler.getInstance().getReader().readIntArray();
        }
        catch (IOException e) {
          LogMessageUtil.processInternalError(e);
          return;
        }

        DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance();
        List<DocumentInfo> rendered = new ArrayList<>(ids.length);
        for (int id : ids) {
          rendered.add(documentFactoryManager.getInfo(id));
        }
        result.setDone(rendered);
      });

      out.writeUInt29(outdatedLocalStyleHolders.size());
      outdatedLocalStyleHolders.forEachKey(new TObjectProcedure<ModuleInfo>() {
        @Override
        public boolean execute(ModuleInfo moduleInfo) {
          out.writeUInt29(moduleInfo.getId());
          return true;
        }
      });

      out.write(infos);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError, callback);
    }

    return result;
  }

  private int registerDocumentFactoryIfNeed(Module module, XmlFile psiFile, VirtualFile virtualFile, boolean force,
                                            ProblemsHolder problemsHolder) {
    final DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance();
    final boolean registered = !force && documentFactoryManager.isRegistered(virtualFile);
    final DocumentInfo documentInfo = documentFactoryManager.get(virtualFile, null, null);
    if (!registered) {
      boolean hasError = true;
      try {
        beginMessage(ClientMethod.registerDocumentFactory);
        writeId(module);
        out.writeShort(documentInfo.getId());
        writeVirtualFile(virtualFile, out);
        hasError = !writeDocumentFactory(documentInfo, module, psiFile, problemsHolder);
      }
      catch (Throwable e) {
        LogMessageUtil.processInternalError(e, virtualFile);
      }
      finally {
        try {
          if (hasError) {
            blockOut.rollback();
            //noinspection ReturnInsideFinallyBlock
            return -1;
          }
        }
        finally {
          outLock.unlock();
        }
      }
    }

    return documentInfo.getId();
  }

  /**
   * You must rollback block out if this method returns false
   */
  private boolean writeDocumentFactory(DocumentInfo documentInfo,
                                       Module module,
                                       XmlFile psiFile,
                                       ProblemsHolder problemsHolder) throws IOException {
    final AccessToken token = ReadAction.start();
    final int flags;
    try {
      final JSClass jsClass = XmlBackedJSClassFactory.getXmlBackedClass(psiFile);
      assert jsClass != null;
      out.writeAmfUtf(jsClass.getQualifiedName());

      if (ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.SPARK_APPLICATION) ||
          ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.MX_APPLICATION)) {
        flags = 1;
      }
      else if (ActionScriptClassResolver.isParentClass(jsClass, FlexCommonTypeNames.IUI_COMPONENT)) {
        flags = 0;
      }
      else {
        flags = 2;
      }
    }
    finally {
      token.finish();
    }

    out.write(flags);

    Pair<ProjectComponentReferenceCounter, List<RangeMarker>> result =
      new MxmlWriter(out, problemsHolder, registeredModules.getInfo(module).getFlexLibrarySet().assetCounterInfo.demanded).write(psiFile);
    if (result == null) {
      return false;
    }

    blockOut.end();

    documentInfo.setRangeMarkers(result.second);
    return result.first.unregistered.isEmpty() || registerDocumentReferences(result.first.unregistered, module, problemsHolder);
  }

  public boolean registerDocumentReferences(List<XmlFile> files, @Nullable Module module, ProblemsHolder problemsHolder) {
    for (XmlFile file : files) {
      VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
      Module documentModule = ModuleUtilCore.findModuleForFile(virtualFile, file.getProject());
      assert documentModule != null;
      if (module != documentModule && !isModuleRegistered(documentModule)) {
        try {
          LibraryManager.getInstance().registerModule(documentModule, problemsHolder);
        }
        catch (InitException e) {
          LogMessageUtil.LOG.error(e.getCause());
          // todo unclear error message (module will not be specified in this error message (but must be))
          problemsHolder.add(e.getMessage());
        }
      }

      // force register, it is registered (id allocated) only on server side
      if (registerDocumentFactoryIfNeed(documentModule, file, virtualFile, true, problemsHolder) == -1) {
        return false;
      }
    }

    return true;
  }

  public void selectComponent(int documentId, int componentId) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.selectComponent);
      out.writeUInt29(documentId);
      out.writeUInt29(componentId);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError);
    }
  }

  public ActionCallback updatePropertyOrStyle(int documentId, int componentId, Consumer<AmfOutputStream> streamConsumer) {
    final ActionCallback callback = new ActionCallback("updatePropertyOrStyle");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.updatePropertyOrStyle, callback);
      out.writeUInt29(documentId);
      out.writeUInt29(componentId);
      streamConsumer.consume(out);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError, callback);
    }

    return callback;
  }

  //public AsyncResult<BufferedImage> getDocumentImage(DocumentFactoryManager.DocumentInfo documentInfo) {
  //  final AsyncResult<BufferedImage> result = new AsyncResult<BufferedImage>();
  //  getDocumentImage(documentInfo, result);
  //  return result;
  //}

  public void getDocumentImage(DocumentInfo documentInfo, final AsyncResult<BufferedImage> result) {
    final ActionCallback callback = new ActionCallback("getDocumentImage");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.getDocumentImage, callback, result, () -> {
        Reader reader = SocketInputHandler.getInstance().getReader();
        try {
          result.setDone(reader.readImage());
        }
        catch (IOException e) {
          LogMessageUtil.LOG.error(e);
          result.setRejected();
        }
      });

      out.writeShort(documentInfo.getId());
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError, callback);
    }
  }

  public static void writeVirtualFile(VirtualFile file, PrimitiveAmfOutputStream out) {
    out.writeAmfUtf(file.getUrl());
    out.writeAmfUtf(file.getPresentableUrl());
  }

  public void initStringRegistry() throws IOException {
    StringRegistry stringRegistry = StringRegistry.getInstance();
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.initStringRegistry);
      out.write(stringRegistry.toArray());
      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public void writeId(Module module, PrimitiveAmfOutputStream out) {
    out.writeShort(registeredModules.getId(module));
  }

  private void writeId(Module module) {
    writeId(module, out);
  }

  private void writeId(Project project) {
    writeId(registeredProjects.getId(project));
  }

  private void writeId(int id) {
    out.writeShort(id);
  }

  private enum ClientMethod {
    openProject, closeProject, registerLibrarySet, registerModule, unregisterModule, registerDocumentFactory, updateDocumentFactory, renderDocument, renderDocumentsAndDependents,
    initStringRegistry, updateStringRegistry, fillImageClassPool, fillSwfClassPool, fillViewClassPool,
    selectComponent, getDocumentImage, updatePropertyOrStyle, updateLocalStyleHolders;
    
    public static final int METHOD_CLASS = 0;
  }
}