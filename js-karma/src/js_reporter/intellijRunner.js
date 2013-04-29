var ideCfg = require('./ideConfig');

var runner = ideCfg.requireKarmaModule('lib/runner.js');

function run(runnerPort) {
  runner.run(
    { runnerPort: runnerPort },
    function() {}
  );
}

var runnerPort = ideCfg.getRunnerPort();
if (typeof runnerPort === 'number') {
  run(runnerPort);
}
else {
  process.stdin.resume();
  process.stdin.setEncoding('utf8');
  var text = '';
  var listener = function(data) {
    text += data;
    console.log('checking for ' + text);
    var m = text.match(/^runner port (\d+)\n/);
    if (m) {
      var runnerPort = parseInt(m[1], 10);
      console.log('check successful, port is ' + runnerPort);
      run(runnerPort);
      process.stdin.removeListener('data', listener);
      process.stdin.pause();
      process.stdin.destroy();
    }
  };
  process.stdin.on('data', listener);
}
