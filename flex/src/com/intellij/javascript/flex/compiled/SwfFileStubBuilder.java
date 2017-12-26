package com.intellij.javascript.flex.compiled;

import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.lang.javascript.psi.stubs.impl.JSFileCachedData;
import com.intellij.lang.javascript.psi.stubs.impl.JSFileStubImpl;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;

/**
 * @author Maxim.Mossienko
 */
public class SwfFileStubBuilder implements BinaryFileStubBuilder {
  private static final int VERSION = 3;

  public boolean acceptsFile(@NotNull final VirtualFile file) {
    return file.getFileType() == FlexApplicationComponent.SWF_FILE_TYPE &&
           file.getPath().endsWith(JarFileSystem.JAR_SEPARATOR + file.getName());
  }

  public StubElement buildStubTree(FileContent fileContent) {
    return buildFileStub(fileContent.getFile(), fileContent.getContent());
  }

  static PsiFileStub buildFileStub(VirtualFile file, byte[] content) {
    PsiFileStubImpl stub = new JSFileStubImpl(JavaScriptSupportLoader.ECMA_SCRIPT_L4, new JSFileCachedData());
    try {

      FlexImporter.buildStubsInterfaceFromStream(
        new ByteArrayInputStream(content),
        stub
      );

    } catch (Exception ex) {
      Logger.getInstance(SwfFileStubBuilder.class.getName()).warn(file.getPath(), ex);
    }

    return stub;
  }

  public int getStubVersion() {
    return JSFileElementType.getVersion() + VERSION;
  }
}