var intellijParameters = require('./karma-intellij-parameters')
  , fs = require('fs')
  , path = require('path')
  , MAPPINGS_NAME = '%MAPPINGS%'
  , HTML_FILE_NAME = 'context.html'
  , os = require('os')
  , intellijUtil = require('./intellijUtil')
  , REMOTE_DEBUGGING_PORT = '--remote-debugging-port';

function createPatchedContextHtmlFile() {
  var contextHtmlFilePath = intellijUtil.getKarmaFilePath('./static/' + HTML_FILE_NAME);
  var contextHtmlFileContent = fs.readFileSync(contextHtmlFilePath, {encoding: 'utf8'});
  if (typeof contextHtmlFileContent !== 'string') {
    return printFailure('not a string');
  }
  var mappingsIndex = contextHtmlFileContent.indexOf(MAPPINGS_NAME);
  if (mappingsIndex < 0) {
    return printFailure('cannot find ' + MAPPINGS_NAME + ' in ' + contextHtmlFilePath);
  }
  var jsFilePath = path.resolve(__dirname, '../static/delay-karma-start-in-debug-mode.js');
  var jsFileContent = fs.readFileSync(jsFilePath, {encoding: 'utf8'});
  var result = contextHtmlFileContent.substring(0, mappingsIndex + MAPPINGS_NAME.length) + '\n' +
      jsFileContent +
      contextHtmlFileContent.substring(mappingsIndex + MAPPINGS_NAME.length);
  var dir = fs.mkdtempSync(path.join(os.tmpdir(), 'intellij-karma-'));
  var patchedHtmlFilePath = path.join(dir, 'intellij-' + HTML_FILE_NAME);
  fs.writeFileSync(patchedHtmlFilePath, result);
  return patchedHtmlFilePath;
}

function printFailure(message, err) {
  var text = 'intellij integration: failed to provide custom /' + HTML_FILE_NAME + ': ' + message;
  if (err) {
    console.error(text, err);
  }
  else {
    console.error(text);
  }
}

exports.initCustomContextFile = function (config) {
  if (config.customContextFile == null) {
    try {
      config.customContextFile = createPatchedContextHtmlFile();
    }
    catch (e) {
      printFailure('failed to create custom ' + HTML_FILE_NAME + ': ', e);
    }
  }
};

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
