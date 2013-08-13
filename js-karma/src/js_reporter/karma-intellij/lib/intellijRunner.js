var cli = require('./intellijCli')
  , intellijUtil = require('./intellijUtil')
  , RESUME_TEST_RUNNING_MESSAGE = 'resume-test-running';

function runTests() {
  var runner = cli.requireKarmaModule('lib/runner.js');
  var serverPort = cli.getServerPort();
  var urlRoot = cli.getUrlRoot() || '/';
  if (urlRoot.charAt(urlRoot.length - 1) !== '/') {
    urlRoot = urlRoot + '/';
  }
  runner.run(
    {
      port: serverPort,
      refresh: false,
      urlRoot: urlRoot
    },
    function() {}
  );
}

if (cli.isDebug()) {
  intellijUtil.processStdInput(function(line) {
    var resume = RESUME_TEST_RUNNING_MESSAGE === line;
    if (resume) {
      runTests();
    }
    return !resume;
  });
}
else {
  runTests();
}
