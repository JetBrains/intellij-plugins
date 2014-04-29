package com.jetbrains.lang.dart.ide.template;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DartEmptyApplicationGenerator extends BaseDartApplicationGenerator {

  @Override
  protected VirtualFile createPubspec(final VirtualFile baseDir) throws IOException { return null; }

  @Override
  @NotNull
  public String getName() {
    return DartBundle.message("dart.empty.application.title");
  }

  @Override
  @NotNull
  public String getDescription() {
    return DartBundle.message("dart.empty.application.description");
  }

}
