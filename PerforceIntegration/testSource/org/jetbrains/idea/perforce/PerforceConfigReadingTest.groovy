package org.jetbrains.idea.perforce
import com.intellij.testFramework.HeavyPlatformTestCase
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionCalculator
import org.jetbrains.idea.perforce.perforce.connections.P4ConnectionParameters
class PerforceConfigReadingTest extends HeavyPlatformTestCase {

  void testSlashInUserName() throws Exception {
    def file = createTempFile("p4config.txt", '''
P4USER=foo/bar\\goo
P4CLIENT= foo=bar
''')
    def params = P4ConnectionCalculator.getParametersFromConfig(file.parentFile, file.name)
    assert !params.exception
    assert params.user == 'foo/bar\\goo'
    assert params.client == 'foo=bar'

  }

  void "test p4 set output"() {
    def output = """
P4CLIENT=p4test
P4PORT=localhost (enviro)
P4CONFIG=customName (config file customPath)
P4USER=user (config)
P4IGNORE=p4Ignore.txt (set)
"""

    def defaultParameters = new P4ConnectionParameters()
    def parameters = new P4ConnectionParameters()
    P4ConnectionCalculator.parseSetOutput(defaultParameters, parameters, output)

    assert parameters.configFileName == 'customName'
    assert parameters.client == 'p4test'
    assert parameters.user == 'user'
    assert parameters.server == 'localhost'
    assert parameters.ignoreFileName == 'p4Ignore.txt'
    
    assert !defaultParameters.configFileName
    assert !defaultParameters.user
    assert defaultParameters.client == 'p4test'
    assert defaultParameters.server == 'localhost'
    assert defaultParameters.ignoreFileName == 'p4Ignore.txt'
  }
}
