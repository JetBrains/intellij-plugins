var ideCfg = require('./ideConfig');

var runner = ideCfg.requireKarmaModule('lib/runner.js');

var runnerPort = ideCfg.getRunnerPort();
runner.run(
  { runnerPort: runnerPort },
  function() {}
);
