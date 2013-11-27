package org.angularjs.codeInsight;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.UsefulTestCase;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * Created by denofevil on 27/11/13.
 */
public class AttributesCompletionTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return PathManager.getJarPathForClass(getClass()) + "/" +
               getClass().getPackage().getName().replace('.', '/') + "/data/attributes";
    }

    public void testStandartAttributesCompletion() {
        myFixture.testCompletion("simple.html", "simple.after.html");
    }
}
