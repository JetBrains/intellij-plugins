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

import com.intellij.codeInsight.actions.CodeInsightAction;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

/**
 * Created by Lera Nikolaenko
 * Date: 30.01.2009
 */
public class CfmlCommenterTest extends CfmlCodeInsightFixtureTestCase {
    public void testBetweenComments() throws Throwable {
      doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }
    public void testMultiLineBlockCommenter() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }
    public void testMultiLineByLineCommenter() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testOneLineByLineCommenter() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_LINE);
    }

    public void testOneLineBlockCommenter() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc2() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    public void testCommentInFullyScriptedCfc3() throws Throwable {
        doTest(IdeActions.ACTION_COMMENT_BLOCK);
    }

    private void doTest(final String actionId) throws Throwable {
        myFixture.configureByFile(Util.getInputDataFileName(getTestName(true)));
        CodeInsightAction action = (CodeInsightAction) ActionManager.getInstance().getAction(actionId);
        action.actionPerformedImpl(myModule.getProject(), myFixture.getEditor());
        myFixture.checkResultByFile(Util.getExpectedDataFileName(getTestName(true)));
    }

    @Override
    protected String getBasePath() {
        return "/typedHandler/commenter";
    }
}
