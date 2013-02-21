package org.jetbrains.plugins.cucumber.groovy.resolve

import com.intellij.psi.PsiMethod
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase

/**
 * @author Max Medvedev
 */
class ResolveWorldTest extends GrCucumberLightTestCase {
  final String basePath = null

  void testResolveCustomWorldInHook() {
    assertResolveToMethod('''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

class CustomWorld {
    String customMethod() {
        "foo"
    }
}

World {
    new CustomWorld()
}

Before() {
    assert "foo" == custom<caret>Method()
}
''')
  }

  void testResolveCustomWorldInStep() {
    assertResolveToMethod( '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

class CustomWorld {
    String customMethod() {
        "foo"
    }
}

World {
    new CustomWorld()
}

Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
''')
  }

  void testWorldFromSameDirectoryFile() {
    myFixture.addFileToProject('otherSteps.groovy', '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

class CustomWorld {
    String customMethod() {
        "foo"
    }
}

World {
    new CustomWorld()
}
''')

    assertResolveToMethod('''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
''')
  }

  private void assertResolveToMethod(final String text) {
    myFixture.configureByText(getTestName(false) + '.groovy', text)

    final ref = myFixture.getReferenceAtCaretPosition()
    assertNotNull(ref)
    assertInstanceOf(ref.resolve(), PsiMethod)
  }
}
