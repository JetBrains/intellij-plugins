var ideCfg = require('./ideConfig.js')
  , constants = ideCfg.requireKarmaModule('lib/constants.js')
  , originalConfigPath = ideCfg.getConfigFile()
  , originalConfigFunc = require(originalConfigPath)
  , path = require('path');

function copy(obj) {
  var out = {};
  Object.keys(obj).forEach(function(key) {
    out[key] = obj[key];
  });
  return out;
}

module.exports = function(karma) {
  var config = {};

  var karmaStub = copy(karma);
  karmaStub.configure = function(originalConfig) {
    config = copy(originalConfig);
  };

  originalConfigFunc(karmaStub);

  var plugins = config.plugins || [];
  var reporters = config.reporters || [];

  var coverageEnabled = plugins.indexOf('karma-coverage') >= 0 && reporters.indexOf('coverage') >= 0;

  plugins.push(require.resolve('./intellijReporter.js'));
  // reset 'reporters' to remove 'progress' and other reporters that aren't necessary
  reporters = ['intellij'];

  if (coverageEnabled) {
    plugins.push(require.resolve('./intellijCoverageReporter.js'));
    reporters.push('coverage');
    reporters.push('intellijCoverage');
  }

  config.plugins = plugins;
  config.reporters = reporters;

  config.singleRun = false;
  config.autoWatch = false;
  // specify runner port to have runner port info dumped to standard output
  config.runnerPort = constants.DEFAULT_RUNNER_PORT + 1;

  config.basePath = path.resolve(path.dirname(originalConfigPath), config.basePath);

  karma.configure(config);
};
