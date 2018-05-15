var intellijUtil = require('./intellijUtil')
  , intellijParameters = require('./karma-intellij-parameters')
  , originalConfigPath = intellijParameters.getUserConfigFilePath()
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
  if (intellijUtil.isString(config.browserForDebugging)) {
    config.browsers = [config.browserForDebugging];
    console.info('intellij: set config.browsers to ' + JSON.stringify(config.browsers));
  }
  else if (Array.isArray(config.browsers) && config.browsers.length > 0) {
    config.browsers = [];
    console.info('intellij: a browser for tests debugging will be captured automatically');
  }
  (function fixMochaTimeout() {
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
      else {
        console.error('intellij: config.client.mocha is not an object')
      }
    }
    else {
      console.error('intellij: config.client is not an object')
    }
  })();
}

function disableSingleRun(config) {
  config.singleRun = false;
  const prevSet = config.set;
  // Workaround if karma server is instantiated with { singleRun: true }
  // For example, @angular/cli is the case:
  // https://github.com/angular/devkit/blob/v6.0.1/packages/angular_devkit/build_angular/src/karma/index.ts#L65
  if (typeof prevSet === 'function') {
    config.set = function (newConfig) {
      if (newConfig.singleRun === true) {
        newConfig.singleRun = false;
      }
      prevSet.apply(config, arguments);
    };
  }
}

module.exports = function (config) {
  IntellijCoverageReporter.preconfigureCoverage(config);
  var originalConfigModule = require(originalConfigPath);
  // https://github.com/karma-runner/karma/blob/v1.7.0/lib/config.js#L364
  if (typeof originalConfigModule === 'object' && typeof originalConfigModule.default !== 'undefined') {
    originalConfigModule = originalConfigModule.default;
  }
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
  if (intellijParameters.isDebug()) {
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

  disableSingleRun(config);
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
      protocol: config.protocol || 'http:',
      webpack: intellijUtil.isPreprocessorSpecified(config.preprocessors, 'webpack') ||
               intellijUtil.isPreprocessorSpecified(config.preprocessors, 'angular-cli')
    }
  );
};
