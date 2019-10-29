// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy.resolve

import com.intellij.psi.PsiMethod
import groovy.transform.CompileStatic
import org.jetbrains.plugins.cucumber.groovy.GrCucumberLightTestCase
import org.jetbrains.plugins.groovy.util.ResolveTest
import org.junit.Before
import org.junit.Test

@CompileStatic
class ResolveWorldTest extends GrCucumberLightTestCase implements ResolveTest {

  @Before
  void addCustomWorld() {
    fixture.addFileToProject 'classes.groovy', '''\
class CustomWorld {
    String customMethod() {
        "foo"
    }
}
'''
  }

  private void resolveMethodTest(String text) {
    resolveTest """
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

World {
    new CustomWorld()
}

$text
""", PsiMethod
  }

  @Test
  void resolveCustomWorldInHook() {
    resolveMethodTest '''\
Before() {
    assert "foo" == custom<caret>Method()
}
'''
  }

  @Test
  void 'custom world method in hook in inner closure'() {
    resolveMethodTest '''\
Before() {
    [1,2,3].each { <caret>customMethod() }
}
'''
  }

  @Test
  void resolveCustomWorldInStep() {
    resolveMethodTest '''\
Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
'''
  }

  @Test
  void 'custom world method in step in inner closure'() {
    resolveMethodTest '''\
Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    [1,2,3].each { <caret>customMethod() }
}
'''
  }

  @Test
  void worldFromSameDirectoryFile() {
    fixture.addFileToProject('otherSteps.groovy', '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

World {
    new CustomWorld()
}
''')

    resolveTest '''\
this.metaClass.mixin(cucumber.runtime.groovy.Hooks)
this.metaClass.mixin(cucumber.runtime.groovy.EN)

Given(~"I have entered (\\\\d+) into (.*) calculator") { int number, String ignore ->
    assert "foo" == custom<caret>Method()
}
''', PsiMethod
  }
}
