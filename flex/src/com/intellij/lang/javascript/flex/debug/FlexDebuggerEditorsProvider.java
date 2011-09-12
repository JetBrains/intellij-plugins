package com.intellij.lang.javascript.flex.debug;

import com.intellij.javascript.JSDebuggerSupportUtils;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.EvaluationMode;
import com.intellij.xdebugger.evaluation.XDebuggerEditorsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
* User: Maxim.Mossienko
* Date: Mar 6, 2008
* Time: 7:14:46 PM
* To change this template use File | Settings | File Templates.
*/
class FlexDebuggerEditorsProvider extends XDebuggerEditorsProvider {
  @NotNull
    public FileType getFileType() {
    return JavaScriptSupportLoader.JAVASCRIPT;
  }

  @NotNull
    public Document createDocument(@NotNull final Project project,
                                   @NotNull final String text, @Nullable final XSourcePosition position, @NotNull EvaluationMode mode) {
    return JSDebuggerSupportUtils.createDocument(
        text,
        project,
        position != null ? position.getFile():null, position != null ? position.getOffset():-1
    );
  }
}
