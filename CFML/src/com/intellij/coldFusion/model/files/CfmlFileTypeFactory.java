package com.intellij.coldFusion.model.files;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: vnikolaenko
 * Date: 26.12.2008
 * Time: 13:48:45
 * To change this template use File | Settings | File Templates.
 */
public class CfmlFileTypeFactory extends FileTypeFactory {
  public void createFileTypes(final @NotNull FileTypeConsumer consumer) {
    consumer.consume(CfmlFileType.INSTANCE, StringUtil.join(CfmlFileType.INSTANCE.getExtensions(), ";"));
  }
}
