/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.refactoring.rename.inplace.InplaceRefactoring;

public class CheckExtractConstant implements Check{

    Project project;
    Editor editor;

    @Override
    public void set(Project project, Editor editor) {
        this.project = project;
        this.editor = editor;
    }

    @Override
    public void before() {
    }

    @Override
    public boolean check() {
        return InplaceRefactoring.getActiveInplaceRenamer(editor) == null;
    }

    @Override
    public boolean listenAllKeys() {
        return true;
    }

}
