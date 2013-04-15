package com.dmarcotte.handlebars;

import com.intellij.lang.Language;
import com.intellij.lexer.HtmlLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.LanguageSubstitutor;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class HbLanguageSubstitutor extends LanguageSubstitutor {
  @Nullable
  @Override
  public Language getLanguage(@NotNull VirtualFile file, @NotNull Project project) {
    return isMyFileType(file) ? HbLanguage.INSTANCE : null;
  }

  public boolean isMyFileType(VirtualFile file) {
    return FileTypeManager.getInstance().getFileTypeByFileName(file.getName()) == StdFileTypes.HTML &&
           hasHbScriptTag(file);
  }

  private static boolean hasHbScriptTag(VirtualFile file) {
    FindHbScriptHtmlLexer lexer = new FindHbScriptHtmlLexer();
    try {
      String text = VfsUtilCore.loadText(file);
      lexer.start(text);
      while (lexer.getTokenType() != null) {
        lexer.advance();
        if (lexer.isSeenHbScriptTag()) {
          return true;
        }
      }
    }
    catch (IOException e) {
      return false;
    }
    return false;
  }

  private static class FindHbScriptHtmlLexer extends HtmlLexer {
    private boolean seenHbScriptTag = false;

    private FindHbScriptHtmlLexer() {
      registerHandler(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, new TokenHandler() {
        @Override
        public void handleElement(Lexer lexer) {
          if (scriptType != null && ArrayUtilRt.find(HbLanguage.INSTANCE.getMimeTypes(), scriptType) >= 0) {
            seenHbScriptTag = true;
          }
        }
      });
    }

    private boolean isSeenHbScriptTag() {
      return seenHbScriptTag;
    }
  }
}
