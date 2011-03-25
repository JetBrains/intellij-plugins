package com.intellij.flex.uiDesigner.mxml;

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
    
    int length = (int)virtualFile.getLength();
    out.writeUInt29(length);
    writer.addDirectWriter(length, new MyDirectWriter(id, virtualFile));
  }
  
  private static class MyDirectWriter extends AbstractDirectWriter {
    private final VirtualFile virtualFile;
    
    public MyDirectWriter(int id, VirtualFile virtualFile) {
      super(id << 1);
      this.virtualFile = virtualFile;
    }

    @Override
    public void write(OutputStream out) throws IOException {
      writeId(out);
        
      InputStream inputStream = virtualFile.getInputStream();
      try {
        FileUtil.copy(inputStream, out);
      }
      finally {
        inputStream.close();
      }
    }
  }
}
