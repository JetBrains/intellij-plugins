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
