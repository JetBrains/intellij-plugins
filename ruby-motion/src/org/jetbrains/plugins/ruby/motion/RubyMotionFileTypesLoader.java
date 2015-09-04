package org.jetbrains.plugins.ruby.motion;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.cidr.xcode.plist.PlistFileType;
import org.jetbrains.annotations.NotNull;

public class RubyMotionFileTypesLoader extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    if (SystemInfo.isMac) {
      consumer.consume(PlistFileType.INSTANCE);
    }
  }
}
