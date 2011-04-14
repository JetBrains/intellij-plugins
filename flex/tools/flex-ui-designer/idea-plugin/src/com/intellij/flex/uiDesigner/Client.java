package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.*;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class Client implements Closable {
  protected AmfOutputStream out;
  protected BlockDataOutputStream blockOut;

  private int registeredLibraryCounter;
  protected int sessionId;

  private final MxmlWriter mxmlWriter = new MxmlWriter();

  private final InfoList<Module, ModuleInfo> registeredModules = new InfoList<Module, ModuleInfo>();
  private final InfoList<Project, ProjectInfo> registeredProjects = new InfoList<Project, ProjectInfo>();

  public Client(OutputStream out) {
    setOutput(out);
  }

  public AmfOutputStream getOutput() {
    return out;
  }

  public void setOutput(OutputStream out) {
    blockOut = new BlockDataOutputStream(out);
    this.out = new AmfOutputStream(blockOut);

    mxmlWriter.setOutput(this.out);
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
    if (out == null) {
      return;
    }
    blockOut = null;
    registeredLibraryCounter = 0;
    sessionId++;

    registeredModules.clear();
    registeredProjects.clear();

    mxmlWriter.reset();

    BinaryFileManager.getInstance().reset();

    try {
      out.closeWithoutFlush();
    }
    finally {
      out = null;
    }
  }

  public void openProject(Project project) throws IOException {
    beginMessage(ClientMethod.openProject);
    writeId(project);
    out.writeAmfUtf(project.getName());
    ProjectWindowBounds.write(project, out);
    blockOut.end();
  }

  private void beginMessage(ClientMethod method) {
    blockOut.assertStart();
    out.write(ClientMethod.METHOD_CLASS);
    out.write(method);
  }

  public void closeProject(Project project) throws IOException {
    beginMessage(ClientMethod.closeProject);
    writeId(project);
    out.flush();
  }

  public void registerLibrarySet(LibrarySet librarySet, StringRegistry.StringWriter stringWriter) throws IOException {
    beginMessage(ClientMethod.registerLibrarySet);
    out.writeAmfUtf(librarySet.getId());
    out.writeInt(-1); // todo parent

    stringWriter.writeTo(out);

    out.write(librarySet.getApplicationDomainCreationPolicy());
    out.writeShort(librarySet.getLibraries().size());
    for (Library library : librarySet.getLibraries()) {
      final OriginalLibrary originalLibrary;
      final boolean unregisteredLibrary;
      if (library instanceof OriginalLibrary) {
        originalLibrary = (OriginalLibrary)library;
        unregisteredLibrary = originalLibrary.sessionId != sessionId;
        out.write(unregisteredLibrary ? 0 : 1);
      }
      else if (library instanceof FilteredLibrary) {
        FilteredLibrary filteredLibrary = (FilteredLibrary)library;
        originalLibrary = filteredLibrary.getOrigin();
        unregisteredLibrary = originalLibrary.sessionId != sessionId;
        out.write(unregisteredLibrary ? 2 : 3);
      }
      else {
        out.write(4);
        out.writeNotEmptyString(((EmbedLibrary)library).getPath());
        continue;
      }

      if (unregisteredLibrary) {
        originalLibrary.id = registeredLibraryCounter++;
        originalLibrary.sessionId = sessionId;
        out.writeAmfUtf(originalLibrary.getPath());
        writeVirtualFile(originalLibrary.getFile(), out);

        if (originalLibrary.inheritingStyles == null) {
          out.writeShort(0);
        }
        else {
          out.write(originalLibrary.inheritingStyles);
        }

        if (originalLibrary.defaultsStyle == null) {
          out.write(0);
        }
        else {
          out.write(1);
          out.write(originalLibrary.defaultsStyle);
        }
      }
      else {
        out.writeShort(originalLibrary.id);
      }
    }

    blockOut.end();
  }

  public void registerModule(Project project, ModuleInfo moduleInfo, String[] librarySetIds, StringRegistry.StringWriter stringWriter)
    throws IOException {
    beginMessage(ClientMethod.registerModule);

    stringWriter.writeToIfStarted(out);

    out.writeShort(registeredModules.add(moduleInfo));
    writeId(project);
    out.write(librarySetIds);
    out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);

    out.reset();

    blockOut.end();
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
    if (stringRegistry.isEmpty()) {
      return;
    }

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
    qualifyExternalInlineStyleSource, initStringRegistry,
    registerBitmap, registerSwf;
    
    public static final int METHOD_CLASS = 0;
  }
}