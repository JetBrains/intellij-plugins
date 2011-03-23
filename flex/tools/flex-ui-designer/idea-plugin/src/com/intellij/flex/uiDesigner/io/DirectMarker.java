package com.intellij.flex.uiDesigner.io;

import java.io.IOException;
import java.io.OutputStream;

public interface DirectMarker extends Marker {
  void write(OutputStream out) throws IOException;
}
