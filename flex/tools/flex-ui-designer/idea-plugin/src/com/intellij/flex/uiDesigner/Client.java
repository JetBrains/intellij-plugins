package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.ClassPoolGenerator;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.libraries.*;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.flex.uiDesigner.mxml.ProjectComponentReferenceCounter;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Consumer;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.flex.uiDesigner.DocumentFactoryManager.DocumentInfo;

public class Client implements Disposable {
  protected final BlockDataOutputStream blockOut = new BlockDataOutputStream();
  protected final AmfOutputStream out = new AmfOutputStream(blockOut);

  private final InfoMap<Module, ModuleInfo> registeredModules = new InfoMap<Module, ModuleInfo>(true);
  private final InfoMap<Project, ProjectInfo> registeredProjects = new InfoMap<Project, ProjectInfo>();

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
      beginMessage(ClientMethod.unregisterModule, callback, null, new Runnable() {
        @Override
        public void run() {
          try {
            SocketInputHandler.getInstance().unregisterDocumentFactories();
          }
          catch (IOException e) {
            LogMessageUtil.LOG.error(e);
          }
        }
      });
      writeId(module);
    }
    finally {
      registeredModules.remove(module);
      finalizeMessageAndFlush(hasError, callback);
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
    final List<Library> styleOwners = new ArrayList<Library>();
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
      out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);
      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public void renderDocument(Module module, XmlFile psiFile) {
    renderDocument(module, psiFile, new ProblemsHolder());
  }

  /**
   * final, full render document — responsible for handle problemsHolder and assetCounter — you must don't do it
   */
  public AsyncResult<DocumentInfo> renderDocument(Module module, XmlFile psiFile, ProblemsHolder problemsHolder) {
    final AsyncResult<DocumentInfo> result = new AsyncResult<DocumentInfo>();

    final VirtualFile virtualFile = psiFile.getVirtualFile();
    final int factoryId = registerDocumentFactoryIfNeed(module, psiFile, virtualFile, false, problemsHolder);
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
    beginMessage(ClientMethod.renderDocument, callback, result, new Runnable() {
      @Override
      public void run() {
        result.setDone(DocumentFactoryManager.getInstance().getInfo(factoryId));
      }
    });

    out.writeShort(factoryId);

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

  private void finalizeMessageAndFlush(boolean hasError) {
    finalizeMessageAndFlush(hasError, null);
  }

  private void finalizeMessageAndFlush(boolean hasError, @Nullable ActionCallback callback) {
    if (hasError) {
      try {
        blockOut.rollback();
      }
      finally {
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
        LogMessageUtil.processInternalError(e);
      }
    }
  }

  public boolean updateDocumentFactory(int factoryId, Module module, XmlFile psiFile) {
    try {
      beginMessage(ClientMethod.updateDocumentFactory);
      out.writeShort(factoryId);

      final ProblemsHolder problemsHolder = new ProblemsHolder();
      writeDocumentFactory(DocumentFactoryManager.getInstance().getInfo(factoryId), module, psiFile, problemsHolder);
      if (!problemsHolder.isEmpty()) {
        DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
      }
      return true;
    }
    catch (Throwable e) {
      LogMessageUtil.processInternalError(e, psiFile.getVirtualFile());
    }

    blockOut.rollback();
    return false;
  }

  public AsyncResult<List<DocumentInfo>> renderDocumentAndDependents(final List<DocumentInfo> infos) {
    final AsyncResult<List<DocumentInfo>> result = new AsyncResult<List<DocumentInfo>>();
    final ActionCallback callback = new ActionCallback("renderDocumentAndDependents");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.renderDocumentsAndDependents, callback, result, new Runnable() {
        @Override
        public void run() {
          result.setDone(infos);
        }
      });

      out.writeUInt29(infos.size());
      for (DocumentInfo info : infos) {
        out.writeUInt29(info.getId());
      }

      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError);
      if (hasError) {
        callback.setRejected();
      }
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
        if (hasError) {
          blockOut.rollback();
          //noinspection ReturnInsideFinallyBlock
          return -1;
        }
      }
    }

    return documentInfo.getId();
  }

  private boolean writeDocumentFactory(DocumentInfo documentInfo,
                                       Module module,
                                       XmlFile psiFile,
                                       ProblemsHolder problemsHolder) throws IOException {
    final AccessToken token = ReadAction.start();
    final int flags;
    try {
      final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass(psiFile);
      assert jsClass != null;
      out.writeAmfUtf(jsClass.getQualifiedName());

      if (JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.SPARK_APPLICATION) ||
          JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.MX_APPLICATION)) {
        flags = 1;
      }
      else if (JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.IUI_COMPONENT)) {
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

  public boolean registerDocumentReferences(List<XmlFile> files, Module module, ProblemsHolder problemsHolder) {
    for (XmlFile file : files) {
      VirtualFile virtualFile = file.getViewProvider().getVirtualFile();
      Module documentModule = ModuleUtil.findModuleForFile(virtualFile, file.getProject());
      if (module != documentModule && !isModuleRegistered(module)) {
        try {
          LibraryManager.getInstance().registerModule(module, problemsHolder);
        }
        catch (InitException e) {
          LogMessageUtil.LOG.error(e.getCause());
          // todo unclear error message (module will not be specified in this error message (but must be))
          problemsHolder.add(e.getMessage());
        }
      }

      // force register, it is registered (id allocated) only on server side
      if (registerDocumentFactoryIfNeed(module, file, virtualFile, true, problemsHolder) == -1) {
        return false;
      }
    }

    return true;
  }

  public void selectComponent(int documentId, int componentId) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.selectComponent);
      out.writeShort(documentId);
      out.writeShort(componentId);
      hasError = false;
    }
    finally {
      finalizeMessageAndFlush(hasError);
    }
  }

  public ActionCallback updateAttribute(int documentId, int componentId, Consumer<AmfOutputStream> streamConsumer) {
    final ActionCallback callback = new ActionCallback("updateAttribute");
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.updateAttribute, callback);
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
      beginMessage(ClientMethod.getDocumentImage, callback, result, new Runnable() {
        @Override
        public void run() {
          SocketInputHandlerImpl.Reader reader = SocketInputHandler.getInstance().getReader();
          try {
            result.setDone(reader.readImage());
          }
          catch (IOException e) {
            LogMessageUtil.LOG.error(e);
            result.setRejected();
          }
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
    beginMessage(ClientMethod.initStringRegistry);
    out.write(stringRegistry.toArray());

    blockOut.end();
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

  private static enum ClientMethod {
    openProject, closeProject, registerLibrarySet, registerModule, unregisterModule, registerDocumentFactory, updateDocumentFactory, renderDocument, renderDocumentsAndDependents,
    initStringRegistry, updateStringRegistry, fillImageClassPool, fillSwfClassPool, fillViewClassPool,
    selectComponent, getDocumentImage, updateAttribute;
    
    public static final int METHOD_CLASS = 0;
  }
}