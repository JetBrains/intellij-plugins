var cli = require('./intellijCli')
  , fs = require('fs')
  , path = require('path')
  , MAPPINGS_NAME = '%MAPPINGS%'
  , HTML_FILE_NAME = 'context.html'
  , os = require('os')
  , intellijUtil = require('./intellijUtil')
  , REMOTE_DEBUGGING_PORT = '--remote-debugging-port';

function createPatchedContextHtmlFile () {
  var contextHtmlFilePath = path.normalize(cli.getKarmaFilePath('./static/' + HTML_FILE_NAME));
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
