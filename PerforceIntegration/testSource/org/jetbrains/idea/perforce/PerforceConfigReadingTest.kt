package org.jetbrains.idea.perforce

import com.intellij.testFramework.HeavyPlatformTestCase
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionParameters
import org.jetbrains.idea.perforce.perforce.connections.P4ParamsCalculator

class PerforceConfigReadingTest : HeavyPlatformTestCase() {

  fun testSlashInUserName() {
    val file = createTempFile("p4config.txt", """
P4USER=foo/bar\goo
P4CLIENT= foo=bar
""")
    val params = P4ParamsCalculator.getParametersFromConfig(file.parentFile, file.name)
    assertNull(params.exception)
    assertEquals("foo/bar\\goo", params.user)
    assertEquals("foo=bar", params.client)
  }

  fun `test p4 set output`() {
    val output = """
P4CLIENT=p4test
P4PORT=localhost (enviro)
P4CONFIG=customName (config file customPath)
P4USER=user (config)
P4IGNORE=p4Ignore.txt (set)
"""
    val defaultParameters = P4ConnectionParameters()
    val parameters = P4ConnectionParameters()
    P4ParamsCalculator.parseSetOutput(defaultParameters, parameters, output)

    assertEquals("customName", parameters.configFileName)
    assertEquals("p4test", parameters.client)
    assertEquals("user", parameters.user)
    assertEquals("localhost", parameters.server)
    assertEquals("p4Ignore.txt", parameters.ignoreFileName)

    assertNull(defaultParameters.configFileName)
    assertNull(defaultParameters.user)
    assertEquals("p4test", defaultParameters.client)
    assertEquals("localhost", defaultParameters.server)
    assertEquals("p4Ignore.txt", defaultParameters.ignoreFileName)
  }
}