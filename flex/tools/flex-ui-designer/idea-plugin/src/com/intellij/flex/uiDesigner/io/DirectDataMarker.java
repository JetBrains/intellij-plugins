package com.intellij.flex.uiDesigner.io;

import java.io.DataOutputStream;
import java.io.IOException;

public interface DirectDataMarker {
  void write(DataOutputStream out) throws IOException;
}
