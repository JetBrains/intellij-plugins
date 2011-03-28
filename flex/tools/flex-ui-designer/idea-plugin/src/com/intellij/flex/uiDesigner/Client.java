package com.intellij.flex.uiDesigner;

import com.intellij.flex.uiDesigner.io.Amf3Types;
import com.intellij.flex.uiDesigner.io.AmfOutputStream;
import com.intellij.flex.uiDesigner.io.BlockDataOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.flex.uiDesigner.mxml.MxmlWriter;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntIterator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class Client {
  protected AmfOutputStream out;
  protected BlockDataOutputStream blockOut;

  private int registeredLibraryCounter;
  protected int sessionId;

  private final MxmlWriter mxmlWriter = new MxmlWriter();

  private final TIntObjectHashMap<ModuleInfo> registeredModules = new TIntObjectHashMap<ModuleInfo>();

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
    return registeredModules.containsKey(module.hashCode());
  }

  @NotNull
  public Module getModule(int id) {
    return registeredModules.get(id).getModule();
  }

  public void flush() throws IOException {
    out.flush();
  }

  public void close() throws IOException {
    if (out == null) {
      return;
    }
    blockOut = null;
    registeredLibraryCounter = 0;
    sessionId++;

    registeredModules.clear();

    mxmlWriter.reset();

    DocumentFileManager.getInstance().reset(sessionId);
    BinaryFileManager.getInstance().reset(sessionId);

    out.closeWithoutFlush();
    out = null;
  }

  public void openProject(Project project) throws IOException {
    beginMessage(ClientMethod.openProject);
    out.writeInt(project.hashCode());
    out.writeAmfUtf(project.getName());

    blockOut.end();
  }

  private void beginMessage(ClientMethod method) {
    blockOut.assertStart();
    out.write(ClientMethod.METHOD_CLASS);
    out.write(method);
  }

  public void closeProject(Project project) throws IOException {
    beginMessage(ClientMethod.closeProject);
    out.writeInt(project.hashCode());
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
    final int id = moduleInfo.getModule().hashCode();
    registeredModules.put(id, moduleInfo);

    beginMessage(ClientMethod.registerModule);

    stringWriter.writeTo(out);

    out.writeInt(id);
    out.writeInt(project.hashCode());
    out.write(librarySetIds);
    out.write(moduleInfo.getLocalStyleHolders(), "lsh", true);

    out.reset();

    blockOut.end();
  }

  public void openDocument(Module module, XmlFile psiFile) throws IOException {
    int factoryId = registerDocumentFactoryIfNeed(module, psiFile, false);
    beginMessage(ClientMethod.openDocument);
    out.writeShort(factoryId);
  }

  private int registerDocumentFactoryIfNeed(Module module, XmlFile psiFile, boolean force) throws IOException {
    VirtualFile virtualFile = psiFile.getVirtualFile();
    assert virtualFile != null;
    DocumentFileManager documentFileManager = DocumentFileManager.getInstance();
    final boolean registered = !force && documentFileManager.isRegistered(virtualFile);
    final DocumentFileManager.DocumentInfo factoryInfo = documentFileManager.getInfo(virtualFile, psiFile, null);
    if (!registered) {
      beginMessage(ClientMethod.registerDocumentFactory);
      writeVirtualFile(virtualFile, out);
      out.writeAmfUtf(factoryInfo.getClassName());
      out.writeInt(module.hashCode());
      out.writeShort(factoryInfo.getId());

      XmlFile[] subDocuments = mxmlWriter.write(psiFile);
      if (subDocuments != null) {
        for (XmlFile subDocument : subDocuments) {
          Module moduleForFile = ModuleUtil.findModuleForFile(virtualFile, psiFile.getProject());
          if (module != moduleForFile) {
            FlexUIDesignerApplicationManager.LOG.error("Currently, support subdocument only from current module");
          }

          registerDocumentFactoryIfNeed(module, subDocument, true);
        }
      }
    }

    return factoryInfo.getId();
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

    int size = stringRegistry.getSize();
    TObjectIntIterator<String> iterator = stringRegistry.getIterator();
    String[] strings = new String[size];
    for (int i = size; i-- > 0; ) {
      iterator.advance();
      strings[iterator.value() - 1] = iterator.key();
    }

    out.write(strings);

    blockOut.end();
  }

  private enum ClientMethod {
    openProject, closeProject, registerLibrarySet, registerModule, registerDocumentFactory, openDocument, 
    qualifyExternalInlineStyleSource, initStringRegistry;
    private static final int METHOD_CLASS = 0;
  }
}