var cli = require('./intellijCli.js')
  , intellijUtil = require('./intellijUtil.js')
  , constants = cli.requireKarmaModule('lib/constants.js')
  , originalConfigPath = cli.getConfigFile();

function isString(value) {
  var toString = {}.toString;
  return typeof value === 'string' || toString.call(value) === '[object String]';
}

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

  config.singleRun = false;
  var originalAutoWatch = config.autoWatch;
  config.autoWatch = false;
  // specify runner port to have runner port info dumped to standard output
  config.runnerPort = constants.DEFAULT_RUNNER_PORT + 1;

  setBasePath(config);
  intellijUtil.sendIntellijEvent('configFile', {basePath: config.basePath, autoWatch: originalAutoWatch});
};
