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
      // IDE posts http '/run' request to trigger tests (see intellijRunner.js).
      // If a request executes more than `httpServer.timeout`, it will be timed out.
      // Disable timeout, as by default httpServer.timeout=120 seconds, not enough for suspended execution.
      webServer.timeout = 0;
    }
    var socketServer = serverObj.get('socketServer');
    if (socketServer) {
      // Disable socket.io heartbeat (ping) to avoid browser disconnecting when debugging tests,
      // because no ping requests are sent when test execution is suspended on a breakpoint.
      // Default values are not enough for suspended execution:
      //    'heartbeat timeout' (pingTimeout) = 60000 ms
      //    'heartbeat interval' (pingInterval) = 25000 ms
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
