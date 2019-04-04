package org.angularjs;

import com.intellij.ide.highlighter.HtmlFileType;
import com.intellij.openapi.fileTypes.FileNameMatcherEx;
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
    consumer.consume(Angular2HtmlFileType.INSTANCE, new FileNameMatcherEx() {
      @Override
      public boolean acceptsCharSequence(@NotNull CharSequence fileName) {
        return false;
      }

      @NotNull
      @Override
      public String getPresentableString() {
        return "Angular HTML Template";
      }
    });
  }
}
