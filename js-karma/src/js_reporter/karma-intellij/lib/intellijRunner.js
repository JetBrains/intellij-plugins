var cli = require('./intellijCli');
var RESUME_TEST_RUNNING_MESSAGE = 'resume-test-running';

function runTests() {
  var runner = cli.requireKarmaModule('lib/runner.js');
  var runnerPort = cli.getRunnerPort();
  runner.run(
    { runnerPort: runnerPort },
    function() {}
  );
}

if (cli.isDebug()) {
  process.stdin.resume();
  process.stdin.setEncoding('utf8');
  var text = '';
  var listener = function(data) {
    text += data;
    var lines = text.split('\n');
    if (lines.indexOf(RESUME_TEST_RUNNING_MESSAGE) >= 0) {
      process.stdin.removeListener('data', listener);
      process.stdin.pause();
      process.stdin.destroy();
      runTests();
    }
    else if (lines.length > 0) {
      text = lines[lines.length - 1];
    }
  };
  process.stdin.on('data', listener);
}
else {
  runTests();
}
