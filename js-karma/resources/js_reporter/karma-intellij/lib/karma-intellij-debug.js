/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var intellijParameters = require('./karma-intellij-parameters')
  , fs = require('fs')
  , path = require('path')
  , MAPPINGS_NAME = '%MAPPINGS%'
  , HTML_FILE_NAME = 'context.html'
  , os = require('os')
  , intellijUtil = require('./intellijUtil')
  , REMOTE_DEBUGGING_PORT = '--remote-debugging-port';

const SOCKET_IO_PING_TIMEOUT_MILLIS = 24 * 60 * 60 * 1000;
exports.SOCKET_IO_PING_TIMEOUT_MILLIS = SOCKET_IO_PING_TIMEOUT_MILLIS

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

function getRemoteDebuggingPortFromCustomLauncherFlags(config, browserName) {
  const customLaunchers = config.customLaunchers;
  if (customLaunchers != null) {
    var launcher = customLaunchers[browserName];
    if (launcher != null) {
      var flags = launcher.flags;
      if (Array.isArray(flags)) {
        const prefix = REMOTE_DEBUGGING_PORT + '=';
        var value = flags.find(element => intellijUtil.isString(element) && element.indexOf(prefix) === 0);
        if (value != null) {
          const port = parseInt(value.substring(prefix.length), 10);
          if (!isNaN(port) && port > 0) {
            return port;
          }
        }
      }
    }
  }
  return -1;
}

function isBrowserWithPreconfiguredRemoteDebuggingPort(browserName) {
  return browserName === 'ChromeHeadless' ||
         browserName === 'ChromeCanaryHeadless' ||
         browserName === 'ChromiumHeadless';
}

exports.configureBrowsers = function (config) {
  let newBrowsers = config.browsers;
  if (intellijUtil.isString(config.browserForDebugging)) {
    newBrowsers = [config.browserForDebugging];
  }
  if (!Array.isArray(newBrowsers)) {
    console.info('intellij: config.browsers is not an array');
    newBrowsers = [];
  }

  const headless = newBrowsers.find(browserName => {
    return isBrowserWithPreconfiguredRemoteDebuggingPort(browserName) ||
      getRemoteDebuggingPortFromCustomLauncherFlags(config, browserName) > 0;
  });

  let remoteDebuggingPort = -1;
  if (headless != null) {
    remoteDebuggingPort = getRemoteDebuggingPortFromCustomLauncherFlags(config, headless);
    if (remoteDebuggingPort < 0 && isBrowserWithPreconfiguredRemoteDebuggingPort(headless)) {
      remoteDebuggingPort = 9222;
    }
  }
  newBrowsers = remoteDebuggingPort > 0 ? [headless] : [];

  config.browsers = newBrowsers;
  if (config.browsers.length === 0) {
    console.info('intellij: a browser for tests debugging will be captured automatically');
  }
  else {
    console.info('intellij: config.browsers: ' + JSON.stringify(config.browsers) +
      ' with ' + REMOTE_DEBUGGING_PORT + '=' + remoteDebuggingPort);
  }
  return remoteDebuggingPort > 0 ? {'--remote-debugging-port': remoteDebuggingPort} : undefined;
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
      // The code below is compatible with karma < 6. For karma@4 or higher, config.pingTimeout is updated in intellij.conf.js
      if (socketServer && typeof socketServer.set === 'function') {
        // Disable socket.io heartbeat (ping) to avoid browser disconnecting when debugging tests,
        // because no ping requests are sent when test execution is suspended on a breakpoint.
        // Default values are not enough for suspended execution:
        //    'heartbeat timeout' (pingTimeout) = 60000 ms
        //    'heartbeat interval' (pingInterval) = 25000 ms
        socketServer.set('heartbeat timeout', SOCKET_IO_PING_TIMEOUT_MILLIS);
        socketServer.set('heartbeat interval', SOCKET_IO_PING_TIMEOUT_MILLIS);
      }
    });
  }
};
