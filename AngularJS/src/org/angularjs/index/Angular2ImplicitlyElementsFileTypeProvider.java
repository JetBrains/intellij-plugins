package org.angularjs.index;

import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.index.JSImplicitElementsIndexFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;

public class Angular2ImplicitlyElementsFileTypeProvider implements JSImplicitElementsIndexFileTypeProvider {
  @Override
  public FileType[] getFileTypes() {
    return new FileType[] {JsonFileType.INSTANCE};
  }
}
