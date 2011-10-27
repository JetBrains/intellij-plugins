package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.AssetClassPoolGenerator;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.libraries.*;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ArrayUtil;
import gnu.trove.TObjectObjectProcedure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class Client implements Closable {
  protected final BlockDataOutputStream blockOut = new BlockDataOutputStream();
  protected final AmfOutputStream out = new AmfOutputStream(blockOut);

  private final MxmlWriter mxmlWriter = new MxmlWriter(out);

  private final InfoList<Module, ModuleInfo> registeredModules = new InfoList<Module, ModuleInfo>(true);
  private final InfoList<Project, ProjectInfo> registeredProjects = new InfoList<Project, ProjectInfo>();

  public static Client getInstance() {
    return ServiceManager.getService(Client.class);
  }

  public AmfOutputStream getOut() {
    return out;
  }

  public void setOut(OutputStream out) {
    blockOut.setOut(out);
  }

  public boolean isModuleRegistered(Module module) {
    return registeredModules.contains(module);
  }

  public InfoList<Project, ProjectInfo> getRegisteredProjects() {
    return registeredProjects;
  }

  @NotNull
  public Module getModule(int id) {
    return registeredModules.getElement(id);
  }

  @NotNull
  public Project getProject(int id) {
    return registeredProjects.getElement(id);
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

  @Override
  // synchronized due to out, otherwise may be NPE at out.closeWithoutFlush() (meaningful primary for tests)
  public synchronized void close() throws IOException {
    out.reset();

    registeredModules.clear();
    registeredProjects.clear();

    mxmlWriter.reset();

    LibraryManager.getInstance().reset();

    out.closeWithoutFlush();
  }

  private void beginMessage(ClientMethod method) {
    blockOut.assertStart();
    out.write(ClientMethod.METHOD_CLASS);
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
    DocumentFactoryManager.getInstance(project).reset();
    
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

  public void unregisterModule(final Module module) {
    registeredModules.remove(module);
    // todo close related documents
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
    final List<LibrarySetItem> items = librarySet.getItems();
    // contains only  resource bundles
    // todo write test for it
    if (items.isEmpty()) {
      return;
    }

    boolean hasError = true;
    try {
      beginMessage(ClientMethod.registerLibrarySet);
      out.writeAmfUtf(librarySet.getId());

      LibrarySet parent = librarySet.getParent();
      if (parent == null) {
        out.write(0);
      }
      else {
        out.writeAmfUtf(parent.getId());
      }

      out.write(librarySet.getApplicationDomainCreationPolicy());
      out.write(items.size());
      final LibraryManager libraryManager = LibraryManager.getInstance();
      for (LibrarySetItem item : items) {
        final Library library = item.library;
        final boolean registered = libraryManager.isRegistered(library);
        int flags = item.filtered ? 1 : 0;
        if (registered) {
          flags |= 2;
        }
        out.write(flags);

        if (registered) {
          out.writeShort(library.getId());
        }
        else {
          out.writeShort(libraryManager.add(library));

          out.writeAmfUtf(library.getPath());
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

        writeParents(items, item);
      }

      out.write(librarySet.getEmbedItems().size());
      for (LibrarySetEmbedItem item : librarySet.getEmbedItems()) {
        out.write(items.indexOf(item.parent));
        out.writeAmfUtf(item.path);
      }

      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  private void writeParents(List<LibrarySetItem> items, LibrarySetItem item) {
    out.write(item.parents.size());
    if (!item.parents.isEmpty()) {
      for (LibrarySetItem parent : item.parents) {
        out.write(items.indexOf(parent));
      }
    }
  }

  public void registerModule(Project project, ModuleInfo moduleInfo, String[] librarySetIds, StringRegistry.StringWriter stringWriter) {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.registerModule);
      stringWriter.writeToIfStarted(out);

      out.writeShort(registeredModules.add(moduleInfo));
      writeId(project);
      out.write(moduleInfo.isApp());
      out.write(librarySetIds);
      out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);
      hasError = false;
    }
    finally {
      finalizeMessage(hasError);
    }
  }

  public void openDocument(Module module, XmlFile psiFile) {
    openDocument(module, psiFile, new ProblemsHolder());
  }

  public void openDocument(Module module, XmlFile psiFile, ProblemsHolder problemsHolder) {
    openDocument(module, psiFile, false, problemsHolder);
  }

  /**
   * final, full open document — responsible for handle problemsHolder and assetCounter — you must not do it
   */
  public boolean openDocument(Module module, XmlFile psiFile, boolean notifyOpened, ProblemsHolder problemsHolder) {
    final DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance(module.getProject());
    final VirtualFile virtualFile = psiFile.getVirtualFile();
    final FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    assert virtualFile != null;
    if (documentFactoryManager.isRegistered(virtualFile) && ArrayUtil.indexOf(fileDocumentManager.getUnsavedDocuments(),
      fileDocumentManager.getDocument(virtualFile)) != -1) {
      return updateDocumentFactory(documentFactoryManager.getId(virtualFile), module, psiFile);
    }

    final int factoryId = registerDocumentFactoryIfNeed(module, psiFile, virtualFile, false, problemsHolder);
    if (factoryId == -1) {
      return false;
    }

    fillAssetClassPoolIfNeed(module);

    if (!problemsHolder.isEmpty()) {
      DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
    }

    beginMessage(ClientMethod.openDocument);
    writeId(module);
    out.writeShort(factoryId);
    out.write(notifyOpened);

    return true;
  }

  public void fillAssetClassPoolIfNeed(Module module) {
    final AssetCounterInfo assetCounter = registeredModules.getInfo(module).assetCounterInfo;
    int diff = assetCounter.demanded.imageCount - assetCounter.allocated.imageCount;
    if (diff > 0) {
      // reduce number of call fill asset class pool
      diff *= 2;
      fillAssetClassPool(module, diff, ClientMethod.fillImageClassPool);
      assetCounter.allocated.imageCount += diff;
    }

    diff = assetCounter.demanded.swfCount - assetCounter.allocated.swfCount;
    if (diff > 0) {
      // reduce number of call fill asset class pool
      diff *= 2;
      fillAssetClassPool(module, diff, ClientMethod.fillSwfClassPool);
      assetCounter.allocated.swfCount += diff;
    }
  }

  private void fillAssetClassPool(Module module, int classCount, ClientMethod method) {
    boolean hasError = true;
    try {
      beginMessage(method);
      writeId(module);
      out.writeShort(classCount);
      final AssetCounterInfo assetCounterInfo = registeredModules.getInfo(module).assetCounterInfo;
      AssetClassPoolGenerator.generate(method, classCount, assetCounterInfo.allocated, blockOut);
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
    if (hasError) {
      blockOut.rollback();
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
      writeId(module);
      out.writeShort(factoryId);

      ProblemsHolder problemsHolder = new ProblemsHolder();
      writeDocumentFactory(module, psiFile, problemsHolder, XmlBackedJSClassImpl.getXmlBackedClass(psiFile));
      if (!problemsHolder.isEmpty()) {
        DocumentProblemManager.getInstance().report(module.getProject(), problemsHolder);
      }

      beginMessage(ClientMethod.updateDocuments);
      writeId(module);
      out.writeShort(factoryId);
      return true;
    }
    catch (Throwable e) {
      LogMessageUtil.processInternalError(e, psiFile.getVirtualFile());
    }

    blockOut.rollback();
    return false;
  }

  private int registerDocumentFactoryIfNeed(Module module, XmlFile psiFile, VirtualFile virtualFile, boolean force,
                                            ProblemsHolder problemsHolder) {
    final DocumentFactoryManager documentFactoryManager = DocumentFactoryManager.getInstance(module.getProject());
    final boolean registered = !force && documentFactoryManager.isRegistered(virtualFile);
    final int id = documentFactoryManager.getId(virtualFile);
    if (!registered) {
      boolean hasError = true;
      try {
        beginMessage(ClientMethod.registerDocumentFactory);
        writeId(module);
        out.writeShort(id);
        writeVirtualFile(virtualFile, out);

        final JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass(psiFile);
        assert jsClass != null;
        out.writeAmfUtf(jsClass.getQualifiedName());

        hasError = !writeDocumentFactory(module, psiFile, problemsHolder, jsClass);
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

    return id;
  }

  private boolean writeDocumentFactory(Module module, XmlFile psiFile, ProblemsHolder problemsHolder, JSClass jsClass)
    throws IOException {

    final AccessToken token = ReadAction.start();
    final int flags;
    try {
      if (JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.SPARK_APPLICATION) ||
          JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.MX_APPLICATION)) {
        flags = 1;
      }
      else if (JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.FLASH_DISPLAY_OBJECT_CONTAINER) &&
               !JSInheritanceUtil.isParentClass(jsClass, FlexCommonTypeNames.IUI_COMPONENT)) {
        flags = 2;
      }
      else {
        flags = 0;
      }
    }
    finally {
      token.finish();
    }

    out.write(flags);

    XmlFile[] unregisteredDocumentReferences = mxmlWriter.write(psiFile, problemsHolder, registeredModules.getInfo(module).assetCounterInfo.demanded);
    return unregisteredDocumentReferences == null || registerDocumentReferences(unregisteredDocumentReferences, module, problemsHolder);
  }

  public boolean registerDocumentReferences(XmlFile[] files, Module module, ProblemsHolder problemsHolder) {
    for (XmlFile file : files) {
      VirtualFile virtualFile = file.getVirtualFile();
      assert virtualFile != null;
      Module documentModule = ModuleUtil.findModuleForFile(virtualFile, file.getProject());
      if (module != documentModule && !isModuleRegistered(module)) {
        try {
          LibraryManager.getInstance().initLibrarySets(module, problemsHolder);
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

  public void qualifyExternalInlineStyleSource() {
    beginMessage(ClientMethod.qualifyExternalInlineStyleSource);
  }

  public static void writeVirtualFile(VirtualFile file, PrimitiveAmfOutputStream out) {
    out.writeAmfUtf(file.getUrl());
    out.writeAmfUtf(file.getPresentableUrl());
  }

  public void initStringRegistry() throws IOException {
    StringRegistry stringRegistry = ServiceManager.getService(StringRegistry.class);
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

  public static enum ClientMethod {
    openProject, closeProject, registerLibrarySet, registerModule, registerDocumentFactory, updateDocumentFactory, openDocument, updateDocuments,
    qualifyExternalInlineStyleSource, initStringRegistry, updateStringRegistry, fillImageClassPool, fillSwfClassPool;
    
    public static final int METHOD_CLASS = 0;
  }
}