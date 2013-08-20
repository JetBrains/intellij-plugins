var cli = require('./intellijCli.js')
  , intellijUtil = require('./intellijUtil.js')
  , originalConfigPath = cli.getConfigFile();

function setBasePath(config) {
  var path = require('path');
  var basePath = config.basePath || '';
  config.basePath = path.resolve(path.dirname(originalConfigPath), basePath);
}

module.exports = function(config) {
  var originalConfigModule = require(originalConfigPath);
  originalConfigModule(config);

  var originalReporters = config.reporters || [];
  var coverageEnabled = originalReporters.indexOf('coverage') >= 0;
  // Is resetting 'reporters' list safe?
  var reporters = ['intellij'];
  if (coverageEnabled) {
    reporters.push('coverage');
    reporters.push('intellijCoverage');
  }
  config.reporters = reporters;

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

  setBasePath(config);

  intellijUtil.sendIntellijEvent(
    'configFile',
    {
      autoWatch: originalAutoWatch,
      basePath: config.basePath,
      browsers: config.browsers || [],
      hostname: config.hostname || 'localhost',
      urlRoot: config.urlRoot || '/'
    }
  );
};
