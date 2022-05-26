package org.intellij.plugin.mdx.js;

import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import org.jetbrains.annotations.NotNull;

public class MdxEditorFactoryListener implements EditorFactoryListener {
    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        MdxTagNameSynchronizer.createSynchronizerFor(event.getEditor());
    }
}
