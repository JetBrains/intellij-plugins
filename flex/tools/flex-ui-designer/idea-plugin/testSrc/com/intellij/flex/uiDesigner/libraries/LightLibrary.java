package com.intellij.flex.uiDesigner.libraries;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.javascript.flex.FlexApplicationComponent;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.*;

class LightLibrary extends Library {
  private final LightVirtualFile catalogFile;
  private final LightVirtualFile swfFile;

  LightLibrary(File file) throws IOException {
    super(new LightVirtualFile(file.getPath()));

    Pair<CharArrayReader, ByteArrayInputStream> data = LibraryUtil.openSwc(file);

    catalogFile = new LightVirtualFile(file.getPath() + "/catalog.xml", XmlFileType.INSTANCE, FileUtil.loadTextAndClose(data.first));
    catalogFile.setCharset(CharsetToolkit.UTF8_CHARSET);

    final byte[] swfBytes = FileUtil.loadBytes(data.second);
    swfFile = new LightVirtualFile(file.getPath() + "/library.swf") {
      @Override
      public InputStream getInputStream() {
        return new ByteArrayInputStream(swfBytes);
      }

      @NotNull
      @Override
      public byte[] contentsToByteArray() {
        return swfBytes;
      }
    };
    swfFile.setFileType(FlexApplicationComponent.SWF_FILE_TYPE);
  }

  @Override
  public VirtualFile getCatalogFile() {
    return catalogFile;
  }

  @Override
  public VirtualFile getSwfFile() {
    return swfFile;
  }
}