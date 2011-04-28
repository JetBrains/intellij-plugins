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
    final List<Library> libraries = librarySet.getLibraries();
    out.writeShort(sizeOfOriginalLibrary(libraries));
    final LibraryManager libraryManager = LibraryManager.getInstance();
    for (Library library : libraries) {
      final OriginalLibrary originalLibrary;
      final boolean unregisteredLibrary;
      if (library instanceof OriginalLibrary) {
        originalLibrary = (OriginalLibrary)library;
        if (!originalLibrary.hasDefinitions()) {
          continue;
        }

        unregisteredLibrary = !libraryManager.isRegistered(originalLibrary);
        if (originalLibrary.filtered) {
          out.write(unregisteredLibrary ? 2 : 3);
        }
        else {
          out.write(unregisteredLibrary ? 0 : 1);
        }
      }
      else {
        final EmbedLibrary embedLibrary = (EmbedLibrary)library;
        out.write(4);
        out.writeShort(indexOfOriginalLibrary(libraries, embedLibrary.parent));
        out.writeAmfUtf(embedLibrary.getPath());
        continue;
      }

      if (unregisteredLibrary) {
        out.writeShort(libraryManager.add(originalLibrary));
        writeParents(libraries, originalLibrary);
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
        out.writeShort(originalLibrary.getId());
        if (originalLibrary.filtered) {
          writeParents(libraries, originalLibrary);
        }
      }
    }

    blockOut.end();
  }

  private int sizeOfOriginalLibrary(List<Library> libraries) {
    int size = 0;
    for (Library library : libraries) {
      if (library instanceof OriginalLibrary) {
        final OriginalLibrary originalLibrary = (OriginalLibrary)library;
        if (originalLibrary.hasDefinitions()) {
          size++;
        }
      }
      else {
        size++;
      }
    }

    return size;
  }

  // can't use standard List.indexOf, because server list contains resource libraries, but client doesn't
  private int indexOfOriginalLibrary(List<Library> libraries, OriginalLibrary o) {
    int index = 0;
    for (Library library : libraries) {
      if (library instanceof OriginalLibrary) {
        final OriginalLibrary originalLibrary = (OriginalLibrary)library;
        if (originalLibrary.hasDefinitions()) {
          if (o == originalLibrary) {
            return index;
          }
          index++;
        }
      }
      else {
        index++;
      }
    }

    throw new IllegalArgumentException();
  }

  private void writeParents(List<Library> libraries, OriginalLibrary originalLibrary) {
    if (originalLibrary.parents.isEmpty()) {
      out.write(0);
    }
    else {
      out.write(originalLibrary.parents.size());
      for (OriginalLibrary parent : originalLibrary.parents) {
        // can't use parent.getId(), because parents/successors related to filtered, but not to original id
        out.writeShort(indexOfOriginalLibrary(libraries, parent));
      }
    }
  }

  public void registerModule(Project project, ModuleInfo moduleInfo, String[] librarySetIds, StringRegistry.StringWriter stringWriter)
    throws IOException {
    beginMessage(ClientMethod.registerModule);

    stringWriter.writeToIfStarted(out);

    out.writeShort(registeredModules.add(moduleInfo));
    writeId(project);
    out.write(librarySetIds);
    out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);

    out.resetAfterMessage();
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
    qualifyExternalInlineStyleSource, initStringRegistry,
    registerBitmap, registerBinaryFile;
    
    public static final int METHOD_CLASS = 0;
  }
}