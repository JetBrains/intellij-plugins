package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.Client;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SwfValueWriter extends BinaryValueWriter {
  private final String symbol;

  public SwfValueWriter(VirtualFile virtualFile, @Nullable String symbol) {
    super(virtualFile);
    this.symbol = symbol;
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out, BaseWriter writer) {
    out.write(AmfExtendedTypes.SWF);

    if (symbol == null) {
      out.write(0);
    }
    else {
      out.writeAmfUtf(symbol);
    }

    int id;
    if ((id = checkRegistered(out)) == -1) {
      return;
    }
    
    try {
      final int length = (int)virtualFile.getLength();
      
      final OutputStream output = writer.getBlockOut().writeUnbufferedHeader(2 + 2 + 4 + length);
      output.write(Client.ClientMethod.METHOD_CLASS);
      output.write(Client.ClientMethod.registerSwf.ordinal());
      
      writeId(id, output);
      
      output.write((length >>> 24) & 0xFF);
      output.write((length >>> 16) & 0xFF);
      output.write((length >>> 8) & 0xFF);
      output.write(length & 0xFF);
      
      InputStream inputStream = virtualFile.getInputStream();
      try {
        FileUtil.copy(inputStream, output);
      }
      finally {
        inputStream.close();
      }
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }
}