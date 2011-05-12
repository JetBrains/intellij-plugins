package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.abc.ImageWrapper;
import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.libraries.*;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ArrayUtil;
import gnu.trove.TObjectProcedure;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class Client implements Closable {
  protected final BlockDataOutputStream blockOut = new BlockDataOutputStream();
  protected final AmfOutputStream out = new AmfOutputStream(blockOut);

  private final MxmlWriter mxmlWriter = new MxmlWriter(out);

  private final InfoList<Module, ModuleInfo> registeredModules = new InfoList<Module, ModuleInfo>();
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

  public void flush() throws IOException {
    out.flush();
  }

  @Override
  // synchronized due to out, otherwise may be NPE at out.closeWithoutFlush() (meaningful primary for tests)
  public synchronized void close() throws IOException {
    out.reset();

    registeredModules.clear();
    registeredProjects.clear();

    mxmlWriter.reset();

    BinaryFileManager.getInstance().reset();
    LibraryManager.getInstance().reset();

    out.closeWithoutFlush();
  }

  private void beginMessage(ClientMethod method) {
    blockOut.assertStart();
    out.write(ClientMethod.METHOD_CLASS);
    out.write(method);
  }

  public void openProject(Project project) throws IOException {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.openProject);
      writeId(project);
      out.writeAmfUtf(project.getName());
      ProjectWindowBounds.write(project, out);
      hasError = false;
    }
    finally {
      if (hasError) {
        blockOut.rollback();
      }
      else {
        out.flush();
      }
    }
  }

  public void closeProject(final Project project) throws IOException {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.closeProject);
      writeId(project);
      hasError = false;
    }
    finally {
      registeredProjects.remove(project);
      if (registeredProjects.isEmpty()) {
        registeredModules.clear();
      }
      else {
        registeredModules.remove(new TObjectProcedure<Module>() {
          @Override
          public boolean execute(Module module) {
            return module.getProject() == project;
          }
        });
      }

      if (hasError) {
        blockOut.rollback();
      }
      else {
        out.flush();
      }
    }
  }

  public void updateStringRegistry(StringRegistry.StringWriter stringWriter) throws IOException {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.updateStringRegistry);
      stringWriter.writeTo(out);
      hasError = false;
    }
    finally {
      if (hasError) {
        blockOut.rollback();
      }
      else {
        blockOut.end();
      }
    }
  }

  public void registerLibrarySet(LibrarySet librarySet) throws IOException {
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
    final List<LibrarySetItem> items = librarySet.getItems();
    out.write(items.size());
    final LibraryManager libraryManager = LibraryManager.getInstance();
    for (LibrarySetItem item : items) {
      final Library library = item.library;
      final boolean unregisteredLibrary = !libraryManager.isRegistered(library);
      int flags = item.filtered ? 1 : 0;
      if (unregisteredLibrary) {
        flags |= 2;
      }
      out.write(flags);

      if (unregisteredLibrary) {
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
      else {
        out.writeShort(library.getId());
      }

      writeParents(items, item);
    }

    out.write(librarySet.getEmbedItems().size());
    for (LibrarySetEmbedItem item : librarySet.getEmbedItems()) {
      out.write(items.indexOf(item.parent));
      out.writeAmfUtf(item.path);
    }

    blockOut.end();
  }

  private void writeParents(List<LibrarySetItem> items, LibrarySetItem item) {
    out.write(item.parents.size());
    if (!item.parents.isEmpty()) {
      for (LibrarySetItem parent : item.parents) {
        out.write(items.indexOf(parent));
      }
    }
  }

  public void registerModule(Project project, ModuleInfo moduleInfo, String[] librarySetIds, StringRegistry.StringWriter stringWriter)
    throws IOException {
    boolean hasError = true;
    try {
      beginMessage(ClientMethod.registerModule);

      stringWriter.writeToIfStarted(out);

      out.writeShort(registeredModules.add(moduleInfo));
      writeId(project);
      out.write(librarySetIds);
      out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);
      hasError = false;
    }
    finally {
      if (hasError) {
        blockOut.rollback();
      }
      else {
        blockOut.end();
      }

      out.resetAfterMessage();
    }
  }

  public void openDocument(Module module, XmlFile psiFile) throws IOException {
    DocumentFactoryManager documentFileManager = DocumentFactoryManager.getInstance(module.getProject());
    VirtualFile virtualFile = psiFile.getVirtualFile();
    FileDocumentManager fileDocumentManager = FileDocumentManager.getInstance();
    assert virtualFile != null;
    if (documentFileManager.isRegistered(virtualFile) && ArrayUtil.indexOf(fileDocumentManager.getUnsavedDocuments(),
                                                                           fileDocumentManager.getDocument(virtualFile)) != -1) {
      updateDocumentFactory(documentFileManager.getId(virtualFile), module, psiFile);
      return;
    }

    int factoryId = registerDocumentFactoryIfNeed(module, psiFile, documentFileManager, false);
    beginMessage(ClientMethod.openDocument);
    writeId(module);
    out.writeShort(factoryId);
  }
  
  public void updateDocumentFactory(int factoryId, Module module, XmlFile psiFile) throws IOException {
    beginMessage(ClientMethod.updateDocumentFactory);
    writeId(module);
    out.writeShort(factoryId);
    writeDocumentFactory(module, psiFile, psiFile.getVirtualFile(), DocumentFactoryManager.getInstance(module.getProject()));

    beginMessage(ClientMethod.updateDocuments);
    writeId(module);
    out.writeShort(factoryId);
  }

  private int registerDocumentFactoryIfNeed(Module module, XmlFile psiFile, DocumentFactoryManager documentFileManager, boolean force)
    throws IOException {
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    final boolean registered = !force && documentFileManager.isRegistered(virtualFile);
    final int id = documentFileManager.getId(virtualFile);
    if (!registered) {
      beginMessage(ClientMethod.registerDocumentFactory);
      writeId(module);
      out.writeShort(id);
      writeVirtualFile(virtualFile, out);
      
      JSClass jsClass = XmlBackedJSClassImpl.getXmlBackedClass(psiFile);
      assert jsClass != null;
      out.writeAmfUtf(jsClass.getQualifiedName());

      writeDocumentFactory(module, psiFile, virtualFile, documentFileManager);
    }

    return id;
  }
  
  private void writeDocumentFactory(Module module, XmlFile psiFile, VirtualFile virtualFile, DocumentFactoryManager documentFileManager)
    throws IOException {
    MxmlWriter.Result result = mxmlWriter.write(psiFile);
    if (result.problems != null) {
      DocumentProblemManager.getInstance().report(module.getProject(), result.problems);
    }

    if (result.subDocuments != null) {
      for (XmlFile subDocument : result.subDocuments) {
        Module moduleForFile = ModuleUtil.findModuleForFile(virtualFile, psiFile.getProject());
        if (module != moduleForFile) {
          FlexUIDesignerApplicationManager.LOG.error("Currently, support subdocument only from current module");
        }

        // force register, subDocuments from unregisteredDocumentFactories, so, it is registered (id allocated) only on server side
        registerDocumentFactoryIfNeed(module, subDocument, documentFileManager, true);
      }
    }
  }

  public void qualifyExternalInlineStyleSource() {
    beginMessage(ClientMethod.qualifyExternalInlineStyleSource);
  }

  public void registerBinaryFile(int id, final @NotNull VirtualFile file, final BinaryFileType type) throws IOException {
    int length = (int)file.getLength();
    ImageWrapper imageWrapper = null;
    if (type == BinaryFileType.IMAGE) {
      imageWrapper = new ImageWrapper(length);
      length = imageWrapper.getLength();
    }

    OutputStream directOut = blockOut.writeUnbufferedHeader(2 + 1 + 2 + 4 + length);
    directOut.write(ClientMethod.METHOD_CLASS);
    directOut.write(ClientMethod.registerBinaryFile.ordinal());
    directOut.write(type.ordinal());

    directOut.write((id >>> 8) & 0xFF);
    directOut.write(id & 0xFF);

    directOut.write((length >>> 24) & 0xFF);
    directOut.write((length >>> 16) & 0xFF);
    directOut.write((length >>> 8) & 0xFF);
    directOut.write(length & 0xFF);

    final InputStream inputStream = file.getInputStream();
    try {
      if (type == BinaryFileType.IMAGE) {
        imageWrapper.wrap(inputStream, directOut);
      }
      else {
        FileUtil.copy(inputStream, directOut);
      }
    }
    finally {
      inputStream.close();
    }
  }

  @SuppressWarnings({"UnusedDeclaration"})
  public static void writeAmfVirtualFile(@NotNull VirtualFile file, @NotNull AmfOutputStream out) {
    out.write(Amf3Types.OBJECT);
    out.writeObjectTraits("f");
    writeVirtualFile(file, out);
  }

  public static void writeVirtualFile(VirtualFile file, AmfOutputStream out) {
    out.writeAmfUtf(file.getUrl());
    out.writeAmfUtf(file.getPresentableUrl());
  }

  public void initStringRegistry() throws IOException {
    StringRegistry stringRegistry = ServiceManager.getService(StringRegistry.class);
    beginMessage(ClientMethod.initStringRegistry);
    out.write(stringRegistry.toArray());

    blockOut.end();
  }

  private void writeId(Module module) {
    writeId(registeredModules.getId(module));
  }

  private void writeId(Project project) {
    writeId(registeredProjects.getId(project));
  }

  private void writeId(int id) {
    out.writeShort(id);
  }

  public static enum ClientMethod {
    openProject, closeProject, registerLibrarySet, registerModule, registerDocumentFactory, updateDocumentFactory, openDocument, updateDocuments,
    qualifyExternalInlineStyleSource, initStringRegistry, updateStringRegistry,
    registerBitmap, registerBinaryFile;
    
    public static final int METHOD_CLASS = 0;
  }
}