package org.jetbrains.idea.perforce.perforce;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.perforce.PerforceBundle;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

public interface PerforcePhysicalConnectionParametersI {
  String getPathToExec();
  String getPathToIgnore();
  Project getProject();
  void disable();

  int getServerTimeout();
  @NotNull String getCharsetName();

  default Charset getConsoleCharset() {
    String charsetName = getCharsetName();
    if (StringUtil.isEmptyOrSpaces(charsetName) || charsetName.equals(getCharsetNone())) {
      return EncodingManager.getInstance().getDefaultConsoleEncoding();
    }

    // Manual says that with utf16 or utf32 charsets, command line charset should be set to some other value
    // Since we do not support P4COMMANDCHARSET variable yet, let's pretend it's utf8
    // see https://www.perforce.com/manuals/v15.2/p4sag/chapter.configuring.html
    if (charsetName.startsWith("utf16") || charsetName.startsWith("utf32"))
      return StandardCharsets.UTF_8;

    try {
      return Charset.forName(charsetName);
    }
    catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
      return StandardCharsets.UTF_8;
    }
  }


  static @NlsSafe String getCharsetNone() {
    return PerforceBundle.message("none.charset.presentation");
  }
}
