package org.angularjs;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.angular2.lang.html.Angular2HtmlFileType;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSFileFactory extends FileTypeFactory {
  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(HtmlFileType.INSTANCE, "ng");
    consumer.consume(Angular2HtmlFileType.INSTANCE, new FileNameMatcher() {
      @Override
      public boolean accept(@NotNull String fileName) {
        return false;
      }

      @NotNull
      @Override
      public String getPresentableString() {
        return "Angular 2 HTML";
      }
    });
  }
}
