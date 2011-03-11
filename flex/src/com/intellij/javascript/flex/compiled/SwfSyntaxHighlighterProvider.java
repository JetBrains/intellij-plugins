package com.intellij.javascript.flex.compiled;

import com.intellij.openapi.fileTypes.SyntaxHighlighterProvider;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Maxim.Mossienko
 * Date: 06.03.2009
 * Time: 22:29:37
 * To change this template use File | Settings | File Templates.
 */
public class SwfSyntaxHighlighterProvider implements SyntaxHighlighterProvider {
  public SyntaxHighlighter create(FileType fileType, @Nullable Project project, @Nullable VirtualFile file) {
    return SyntaxHighlighterFactory.getSyntaxHighlighter(JavaScriptSupportLoader.ECMA_SCRIPT_L4, project, file);
  }
}
