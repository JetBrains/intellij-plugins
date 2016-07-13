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
    val output = "(node:113) fs: re-evaluating native module sources is not supported. If you are using the graceful-fs module, please update it to a more recent version.\n" +
        "Could not start watchman; falling back to NodeWatcher for file system events.\n" +
        "Visit http://ember-cli.com/user-guide/#watchman for more info.\n" +
        "Requested ember-cli commands:\n" +
        "\n" +
        "ng generate <blueprint> <options...>\n" +
        "  Generates new code from blueprints.\n" +
        "  aliases: g\n" +
        "  --dry-run (Boolean) (Default: false)\n" +
        "    aliases: -d\n" +
        "  --verbose (Boolean) (Default: false)\n" +
        "    aliases: -v\n" +
        "  --pod (Boolean) (Default: false)\n" +
        "    aliases: -p\n" +
        "  --classic (Boolean) (Default: false)\n" +
        "    aliases: -c\n" +
        "  --dummy (Boolean) (Default: false)\n" +
        "    aliases: -dum, -id\n" +
        "  --in-repo-addon (String) (Default: null)\n" +
        "    aliases: --in-repo <value>, -ir <value>\n" +
        "\n" +
        "\n" +
        "  Available blueprints:\n" +
        "    ng2:\n" +
        "      class <class-type>\n" +
        "      component <name> <options...>\n" +
        "        --flat (Boolean) (Default: false)\n" +
        "        --route (Boolean) (Default: false)\n" +
        "        --inline-template (Boolean) (Default: false)\n" +
        "          aliases: -it\n" +
        "        --inline-style (Boolean) (Default: false)\n" +
        "          aliases: -is\n" +
        "        --prefix (Boolean) (Default: true)\n" +
        "      component-test <name>\n" +
        "      directive <name> <options...>\n" +
        "        --flat (Boolean) (Default: true)\n" +
        "      enum <name>\n" +
        "      interface <interface-type>\n" +
        "      mobile <name> <options...>\n" +
        "        --source-dir (String) (Default: src)\n" +
        "          aliases: -sd <value>\n" +
        "        --prefix (String) (Default: app)\n" +
        "          aliases: -p <value>\n" +
        "        --mobile (Boolean) (Default: false)\n" +
        "      ng2 <name> <options...>\n" +
        "        --source-dir (String) (Default: src)\n" +
        "          aliases: -sd <value>\n" +
        "        --prefix (String) (Default: app)\n" +
        "          aliases: -p <value>\n" +
        "        --style (String) (Default: css)\n" +
        "        --mobile (Boolean) (Default: false)\n" +
        "      pipe <name> <options...>\n" +
        "        --flat (Boolean) (Default: true)\n" +
        "      route <name>\n" +
        "      route-test <name>\n" +
        "      service <name> <options...>\n" +
        "        --flat (Boolean) (Default: true)\n" +
        "      service-test <name>\n" +
        "    angular-cli:\n" +
        "      acceptance-test <name>\n" +
        "        Generates an acceptance test for a feature.\n" +
        "      adapter <name> <options...>\n" +
        "        Generates an ng-data adapter.\n" +
        "        --base-class (String)\n" +
        "      adapter-test <name>\n" +
        "        Generates an ng-data adapter unit test\n" +
        "      addon <name>\n" +
        "        The default blueprint for angular-cli addons.\n" +
        "      addon-import <name>\n" +
        "        Generates an import wrapper.\n" +
        "      app <name>\n" +
        "        The default blueprint for angular-cli projects.\n" +
        "      blueprint <name>\n" +
        "        Generates a blueprint and definition.\n" +
        "      component-addon <name>\n" +
        "        Generates a component. Name must contain a hyphen.\n" +
        "      controller <name>\n" +
        "        Generates a controller.\n" +
        "      controller-test <name>\n" +
        "        Generates a controller unit test.\n" +
        "      helper <name>\n" +
        "        Generates a helper function.\n" +
        "      helper-addon <name>\n" +
        "        Generates an import wrapper.\n" +
        "      helper-test <name>\n" +
        "        Generates a helper unit test.\n" +
        "      http-mock <endpoint-path>\n" +
        "        Generates a mock api endpoint in /api prefix.\n" +
        "      http-proxy <local-path> <remote-url>\n" +
        "        Generates a relative proxy to another server.\n" +
        "      in-repo-addon <name>\n" +
        "        The blueprint for addon in repo angular-cli addons.\n" +
        "      initializer <name>\n" +
        "        Generates an initializer.\n" +
        "      initializer-addon <name>\n" +
        "        Generates an import wrapper.\n" +
        "      initializer-test <name>\n" +
        "        Generates an initializer unit test.\n" +
        "      instance-initializer <name>\n" +
        "        Generates an instance initializer.\n" +
        "      instance-initializer-addon <name>\n" +
        "        Generates an import wrapper.\n" +
        "      instance-initializer-test <name>\n" +
        "        Generates an instance initializer unit test.\n" +
        "      lib <name>\n" +
        "        Generates a lib directory for in-repo addons.\n" +
        "      mixin <name>\n" +
        "        Generates a mixin.\n" +
        "      mixin-test <name>\n" +
        "        Generates a mixin unit test.\n" +
        "      model <name> <attr:type>\n" +
        "        Generates an ng-data model.\n" +
        "      model-test <name>\n" +
        "        Generates a model unit test.\n" +
        "      resource <name>\n" +
        "        Generates a model, template, route, and registers the route with the router.\n" +
        "      route-addon <name>\n" +
        "        Generates import wrappers for a route and its template.\n" +
        "      serializer <name>\n" +
        "        Generates an ng-data serializer.\n" +
        "      serializer-test <name>\n" +
        "        Generates a serializer unit test.\n" +
        "      server <name>\n" +
        "        Generates a server directory for mocks and proxies.\n" +
        "      template <name>\n" +
        "        Generates a template.\n" +
        "      test-helper <name>\n" +
        "        Generates a test helper.\n" +
        "      transform <name>\n" +
        "        Generates an ng-data value transform.\n" +
        "      transform-test <name>\n" +
        "        Generates a transform unit test.\n" +
        "      util <name>\n" +
        "        Generates a simple utility module/function.\n" +
        "      util-test <name>\n" +
        "        Generates a util unit test.\n" +
        "      vendor-shim <name>\n" +
        "        Generates an ES6 module shim for global libraries.\n" +
        "\n"

    val blueprints = BlueprintParser().parse(output)
    val requiredBlueprints = Arrays.asList("class", "component", "model")
    val existingBlueprints = ContainerUtil.filter(blueprints) { requiredBlueprints.contains(it.name) }
    TestCase.assertEquals(3, existingBlueprints.size)
    TestCase.assertEquals(5, existingBlueprints[1].args.size)
    TestCase.assertNotNull(existingBlueprints[2].description)
  }
}
