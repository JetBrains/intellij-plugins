/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.check;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

public class CheckNextHeaderOccurenceAction implements Check{


    @Override
    public void set(Project project, Editor editor) {

    }

    @Override
    public void before() {

    }

    @Override
    public boolean check() {
        return false;
    }

    @Override
    public boolean listenAllKeys() {
        return false;
    }
}
