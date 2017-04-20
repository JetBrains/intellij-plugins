var cli = require('./intellijCli.js')
  , server = cli.requireKarmaModule('lib/server.js')
  , cliOptions = { configFile: require.resolve('./intellij.conf.js') };

var browsers = cli.getBrowsers();
if (browsers) {
  cliOptions.browsers = browsers;
}

if (typeof server === 'function') {
  var serverObj = new server(cliOptions);
  serverObj.start();
  if (cli.isDebug()) {
    var webServer = serverObj.get('webServer');
    if (webServer) {
      webServer.timeout = 0;
    }
    var socketServer = serverObj.get('socketServer');
    if (socketServer) {
      socketServer.set('heartbeat timeout', 24 * 60 * 60 * 1000);
      socketServer.set('heartbeat interval', 24 * 60 * 60 * 1000);
    }
  }
}
else {
  // prior to karma@0.13
  server.start(cliOptions);
}

// Prevent karma server from being an orphan process.
// For example, if WebStorm is killed using SIGKILL, karma server will still be alive.
// When WebStorm is terminated, karma server's standard input is closed automatically.
process.stdin.resume();
process.stdin.on('close', function () {
  // terminating orphan process
  process.exit(123);
});
