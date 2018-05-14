var intellijParameters = require('./karma-intellij-parameters');

exports.configureTimeouts = (injector) => {
  // Execute on next tick to resolve circular dependency! (Resolving: webServer -> reporter -> webServer)
  if (intellijParameters.isDebug()) {
    process.nextTick(() => {
      var webServer = injector.get('webServer');
      if (webServer) {
        // IDE posts http '/run' request to trigger tests (see intellijRunner.js).
        // If a request executes more than `httpServer.timeout`, it will be timed out.
        // Disable timeout, as by default httpServer.timeout=120 seconds, not enough for suspended execution.
        webServer.timeout = 0;
      }
      var socketServer = injector.get('socketServer');
      if (socketServer) {
        // Disable socket.io heartbeat (ping) to avoid browser disconnecting when debugging tests,
        // because no ping requests are sent when test execution is suspended on a breakpoint.
        // Default values are not enough for suspended execution:
        //    'heartbeat timeout' (pingTimeout) = 60000 ms
        //    'heartbeat interval' (pingInterval) = 25000 ms
        socketServer.set('heartbeat timeout', 24 * 60 * 60 * 1000);
        socketServer.set('heartbeat interval', 24 * 60 * 60 * 1000);
      }
    });
  }
};
