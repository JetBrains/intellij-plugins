var path = require('path')
  , cli = require('./intellijCli')
  , intellijUtil = require('./intellijUtil')
  , originalConfigPath = cli.getConfigFile()
  , IntellijReporter = require('./intellijReporter')
  , IntellijCoverageReporter = require('./intellijCoverageReporter');

function setBasePath(config) {
  var path = require('path');
  var basePath = config.basePath || '.';
  config.basePath = path.resolve(path.dirname(originalConfigPath), basePath);
}

function configureDebug(config) {
  // Disable browser activity checker as when execution is suspended, no activity is being sent.
  // By default, browserNoActivityTimeout=10000 ms, not enough for suspended execution.
  // https://github.com/karma-runner/karma/blob/master/docs/config/01-configuration-file.md#browsernoactivitytimeout
  config.browserNoActivityTimeout = null;
  config.browsers = intellijUtil.isString(config.browserForDebugging) ? [config.browserForDebugging] : [];
  console.error('intellij: config.browsers = ' + JSON.stringify(config.browsers));
  fixMochaTimeout(config);
}

function fixMochaTimeout(config) {
  var client = config.client;
  if (typeof client === 'undefined') {
    config.client = client = {};
  }
  if (client === Object(client)) {
    var mocha = client.mocha;
    if (typeof mocha === 'undefined') {
      client.mocha = mocha = {};
    }
    if (mocha === Object(mocha)) {
      mocha.timeout = 0;
    }
  }
}

module.exports = function (config) {
  var originalConfigModule = require(originalConfigPath);
  originalConfigModule(config);

  var reporters = config.reporters;
  if (intellijUtil.isString(reporters)) {
    // logic from 'normalizeConfig' in config.js
    reporters = reporters.length === 0 ? [] : reporters.split(',');
  }
  else {
    if (!Array.isArray(reporters)) {
      throw Error("'reporters' is expected to be an array");
    }
  }
  var filteredReporters = intellijUtil.removeAll(reporters, ['dots', 'progress']);
  filteredReporters.push(IntellijReporter.reporterName);
  config.reporters = filteredReporters;

  IntellijCoverageReporter.configureCoverage(config);
  if (cli.isDebug()) {
    configureDebug(config);
  }

  var plugins = config.plugins || [];
  plugins.push(require.resolve('./intellijPlugin.js'));
  config.plugins = plugins;

  // Don't load the original configuration file in the browser.
  // https://github.com/karma-runner/karma-intellij/issues/9
  config.exclude = config.exclude || [];
  config.exclude.push(originalConfigPath);

  // remove 'logLevel' changing as soon as
  // https://github.com/karma-runner/karma/issues/614 is ready
  var logLevel = config.logLevel;
  if (logLevel === config.LOG_DISABLE ||
      logLevel === config.LOG_ERROR ||
      logLevel === config.LOG_WARN) {
    console.log("IntelliJ integration changed logLevel to LOG_INFO, because otherwise it doesn't work.");
    config.logLevel = config.LOG_INFO;
  }

  config.singleRun = false;
  var originalAutoWatch = config.autoWatch;
  config.autoWatch = false;
  config.autoWatchBatchDelay = 0;
  var angularCli = config.angularCli;
  if (angularCli != null && typeof angularCli.sourcemap === 'undefined') {
    angularCli.sourcemap = true;
  }

  setBasePath(config);

  intellijUtil.sendIntellijEvent(
    'configFile',
    {
      autoWatch: originalAutoWatch,
      basePath: config.basePath,
      browsers: config.browsers || [],
      hostname: config.hostname || 'localhost',
      urlRoot: config.urlRoot || '/',
      webpack: intellijUtil.isPreprocessorSpecified(config.preprocessors, 'webpack') ||
               intellijUtil.isPreprocessorSpecified(config.preprocessors, 'angular-cli')
    }
  );
};
