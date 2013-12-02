package org.angularjs.codeInsight;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

/**
 * Created by denofevil on 27/11/13.
 */
public class AttributesTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return AngularTestUtil.getBaseTestDataPath(getClass()) + "attributes";
    }

    public void testStandartAttributesCompletion() {
        myFixture.testCompletion("simple.html", "simple.after.html");
    }

    public void testCustomAttributesCompletion() {
        myFixture.testCompletion("custom.html", "custom.after.html", "custom.js");
    }

    public void testCustomAttributesResolve() {
        myFixture.configureByFiles("custom.after.html", "custom.js");
        int offsetBySignature = AngularTestUtil.findOffsetBySignature("my<caret>Customer", myFixture.getFile());
        PsiReference ref = myFixture.getFile().findReferenceAt(offsetBySignature);
        assertNotNull(ref);
        PsiElement resolve = ref.resolve();
        assertNotNull(resolve);
        assertEquals("custom.js", resolve.getContainingFile().getName());
    }
}
