package com.intellij.javascript.flex.compiled;

import com.intellij.idea.LoggerFactory;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.lang.javascript.flex.importer.FlexImporter;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.psi.stubs.PsiFileStubImpl;
import com.intellij.psi.stubs.StubElement;

import java.io.ByteArrayInputStream;

/**
 * @author Maxim.Mossienko
 *         Date: Sep 18, 2008
 *         Time: 3:49:24 PM
 */
public class SwfFileStubBuilder implements BinaryFileStubBuilder {
  private static final int VERSION = 1;

  public boolean acceptsFile(final VirtualFile file) {
    return file.getFileType() == FlexApplicationComponent.SWF_FILE_TYPE &&
           file.getPath().indexOf(JarFileSystem.JAR_SEPARATOR) != -1;
  }

  public StubElement buildStubTree(final VirtualFile file, final byte[] content, final Project project) {
    return buildFileStub(file, content);
  }

  static PsiFileStub buildFileStub(VirtualFile file, byte[] content) {
    PsiFileStubImpl stub = new PsiFileStubImpl(null);
    try {

      FlexImporter.buildStubsInterfaceFromStream(
        new ByteArrayInputStream(content),
        stub
      );

    } catch (Exception ex) {
      LoggerFactory.getInstance().getLoggerInstance(SwfFileStubBuilder.class.getName()).warn(file.getPath(), ex);
    }

    return stub;
  }

  public int getStubVersion() {
    return JSFileElementType.VERSION + VERSION;
  }
}