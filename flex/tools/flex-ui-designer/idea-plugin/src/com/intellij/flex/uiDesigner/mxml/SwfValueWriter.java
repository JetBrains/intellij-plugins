package com.intellij.flex.uiDesigner.mxml;

import com.intellij.flex.uiDesigner.io.AbstractMarker;
import com.intellij.flex.uiDesigner.io.DirectMarker;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SwfValueWriter extends BinaryValueWriter {
  public SwfValueWriter(VirtualFile virtualFile) {
    super(virtualFile);
  }

  @Override
  protected void write(PrimitiveAmfOutputStream out) {
    out.write(AmfExtendedTypes.SWF);
    if (checkRegistered(out)) {
      return;
    }
    
    int length = (int)virtualFile.getLength();
    out.writeUInt29(length);
    out.getBlockOut().addDirectMarker(length, new MyDirectWriter(virtualFile, out.size()));
  }
  
  private static class MyDirectWriter extends AbstractMarker implements DirectMarker {
    private final VirtualFile virtualFile;
    
    public MyDirectWriter(VirtualFile virtualFile, int position) {
      super(position);
      this.virtualFile = virtualFile;
    }

    @Override
    public void write(OutputStream out) throws IOException {
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
