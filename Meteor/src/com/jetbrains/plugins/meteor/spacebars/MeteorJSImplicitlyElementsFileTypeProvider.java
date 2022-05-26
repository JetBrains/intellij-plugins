package com.jetbrains.plugins.meteor.spacebars;

import com.dmarcotte.handlebars.file.HbFileType;
import com.intellij.lang.javascript.index.JSImplicitElementsIndexFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.jetbrains.plugins.meteor.spacebars.lang.SpacebarsFileType;

import java.util.Arrays;
import java.util.List;

final class MeteorJSImplicitlyElementsFileTypeProvider implements JSImplicitElementsIndexFileTypeProvider {
  @Override
  public List<FileType> getFileTypes() {
    return Arrays.asList(HbFileType.INSTANCE, SpacebarsFileType.SPACEBARS_INSTANCE);
  }
}
