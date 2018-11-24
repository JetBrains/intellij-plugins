package org.angularjs.cli

import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.util.containers.ContainerUtil
import junit.framework.TestCase
import java.util.*

/**
 * @author Dennis.Ushakov
 */
class BlueprintListTest : LightPlatformTestCase() {
  fun testList() {
    val output = """
  Available blueprints:
    angular-cli:
      class <name> <options...>
        --spec (Boolean)
      component <name> <options...>
        --flat (Boolean) (Default: false)
        --inline-template (Boolean)
          aliases: -it
        --inline-style (Boolean)
          aliases: -is
        --prefix (Boolean) (Default: true)
        --spec (Boolean)
      component-test <name>
      directive <name> <options...>
        --flat (Boolean) (Default: true)
        --prefix (Boolean) (Default: true)
        --spec (Boolean)
      enum <name>
      interface <interface-type>
      mobile <name> <options...>
        --source-dir (String) (Default: src)
          aliases: -sd <value>
        --prefix (String) (Default: app)
          aliases: -p <value>
        --mobile (Boolean) (Default: false)
      model <name> <attr:type>
        Generates an ng-data model.
      module <name> <options...>
        --spec (Boolean)
        --routing (Boolean) (Default: false)
      ng2 <name> <options...>
        --source-dir (String) (Default: src)
          aliases: -sd <value>
        --prefix (String) (Default: app)
          aliases: -p <value>
        --style (String) (Default: css)
        --mobile (Boolean) (Default: false)
        --routing (Boolean) (Default: false)
        --inline-style (Boolean) (Default: false)
          aliases: -is
        --inline-template (Boolean) (Default: false)
          aliases: -it
      pipe <name> <options...>
        --flat (Boolean) (Default: true)
        --spec (Boolean)
      route <name>
      route-test <name>
      service <name> <options...>
        --flat (Boolean) (Default: true)
        --spec (Boolean)
      service-test <name>
    angular-cli:
      acceptance-test <name>
        Generates an acceptance test for a feature.
      adapter <name> <options...>
        Generates an ng-data adapter.
        --base-class (String)
      adapter-test <name>
        Generates an ng-data adapter unit test
      addon <name>
        The default blueprint for angular-cli addons.
      addon-import <name>
        Generates an import wrapper.
      app <name>
        The default blueprint for angular-cli projects.
      blueprint <name>
        Generates a blueprint and definition.
      component-addon <name>
        Generates a component. Name must contain a hyphen.
      controller <name>
        Generates a controller.
      controller-test <name>
        Generates a controller unit test.
      helper <name>
        Generates a helper function.
      helper-addon <name>
        Generates an import wrapper.
      helper-test <name>
        Generates a helper unit test.
      http-mock <endpoint-path>
        Generates a mock api endpoint in /api prefix.
      http-proxy <local-path> <remote-url>
        Generates a relative proxy to another server.
      in-repo-addon <name>
        The blueprint for addon in repo angular-cli addons.
      initializer <name>
        Generates an initializer.
      initializer-addon <name>
        Generates an import wrapper.
      initializer-test <name>
        Generates an initializer unit test.
      instance-initializer <name>
        Generates an instance initializer.
      instance-initializer-addon <name>
        Generates an import wrapper.
      instance-initializer-test <name>
        Generates an instance initializer unit test.
      lib <name>
        Generates a lib directory for in-repo addons.
      mixin <name>
        Generates a mixin.
      mixin-test <name>
        Generates a mixin unit test.
      model-test <name>
        Generates a model unit test.
      resource <name>
        Generates a model, template, route, and registers the route with the router.
      route-addon <name>
        Generates import wrappers for a route and its template.
      serializer <name>
        Generates an ng-data serializer.
      serializer-test <name>
        Generates a serializer unit test.
      server <name>
        Generates a server directory for mocks and proxies.
      template <name>
        Generates a template.
      test-helper <name>
        Generates a test helper.
      transform <name>
        Generates an ng-data value transform.
      transform-test <name>
        Generates a transform unit test.
      util <name>
        Generates a simple utility module/function.
      util-test <name>
        Generates a util unit test.
      vendor-shim <name>
        Generates an ES6 module shim for global libraries.

undefined

    """

    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = Arrays.asList("class", "component", "model")
    val existingBlueprints = ContainerUtil.filter(blueprints) { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(3, existingBlueprints.size)
    TestCase.assertEquals(5, existingBlueprints[1].args.size)
    TestCase.assertNotNull(existingBlueprints[2].description)
  }
}
