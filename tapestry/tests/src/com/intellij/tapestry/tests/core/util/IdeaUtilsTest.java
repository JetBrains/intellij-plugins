package com.intellij.tapestry.tests.core.util;

import com.intellij.openapi.actionSystem.*;
import com.intellij.tapestry.intellij.util.IdeaUtils;
import com.intellij.tapestry.tests.core.BaseTestCase;
import com.intellij.testFramework.MapDataContext;
import com.intellij.testFramework.fixtures.IdeaProjectTestFixture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IdeaUtilsTest extends BaseTestCase {

    @BeforeClass
    public void defaultConstructor() {
        new IdeaUtils();
    }

    @Test(dataProvider = JAVA_MODULE_FIXTURE_PROVIDER)
    public void isModuleNode(IdeaProjectTestFixture fixture) {
        MapDataContext dataContext = new MapDataContext();
        dataContext.put(CommonDataKeys.PROJECT.getName(), fixture.getProject());
        dataContext.put(LangDataKeys.MODULE_CONTEXT.getName(), fixture.getModule());

        AnActionEvent actionEvent = new AnActionEvent(null, dataContext, "", new Presentation(), ActionManager.getInstance(), 0);
        assert IdeaUtils.isModuleNode(actionEvent);


      dataContext.put(CommonDataKeys.PROJECT.getName(), null);
      dataContext.put(LangDataKeys.MODULE_CONTEXT.getName(), null);

        actionEvent = new AnActionEvent(null, dataContext, "", new Presentation(), ActionManager.getInstance(), 0);
        assert !IdeaUtils.isModuleNode(actionEvent);
    }

}