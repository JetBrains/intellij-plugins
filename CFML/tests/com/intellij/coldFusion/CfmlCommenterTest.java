/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion;

import com.intellij.codeInsight.actions.MultiCaretCodeInsightAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;

/**
 * Created by Lera Nikolaenko
 */
public class CfmlCommenterTest extends CfmlCodeInsightFixtureTestCase {
    public void testBetweenComments() {
      doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }
    public void testMultiLineBlockCommenter() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }
    public void testMultiLineBlockCommenter2() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK); //
    }
    public void testMultiLineByLineCommenter() {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testOneLineByLineCommenter() {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testOneLineByLineCommenter_2() {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testOneLineBlockCommenter() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc2() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc3() {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    private void doTest(final String actionId) {
        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
        MultiCaretCodeInsightAction action = (MultiCaretCodeInsightAction) ActionManager.getInstance().getAction(actionId);
        action.actionPerformedImpl(myModule.getProject(), myFixture.getEditor());
        myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
    }

    @Override
    protected String getBasePath() {
        return "/typedHandler/commenter";
    }
}
