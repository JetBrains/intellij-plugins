var cli = require('./intellijCli.js')
  , constants = cli.requireKarmaModule('lib/constants.js')
  , originalConfigPath = cli.getConfigFile();

function isString(value) {
  var toString = {}.toString;
  return typeof value === 'string' || toString.call(value) === '[object String]';
}

function setBasePath(config) {
  var path = require('path');
  var basePath = config.basePath || '';
  // copy paste from 'normalizeConfig' in karma/lib/config.js
  if (isString(originalConfigPath)) {
    // resolve basePath
    basePath = path.resolve(path.dirname(originalConfigPath), basePath);
  }
  else {
    // TODO is 'else' possible?
    basePath = path.resolve(basePath || '.');
  }
  config.basePath = basePath;
}

module.exports = function(config) {
  var originalConfigModule = require(originalConfigPath);
  originalConfigModule(config);

  var plugins = config.plugins || [];
  var reporters = config.reporters || [];

  var coverageEnabled = plugins.indexOf('karma-coverage') >= 0 && reporters.indexOf('coverage') >= 0;

  plugins.push(require.resolve('./intellijPlugin.js'));
  // reset 'reporters' to remove 'progress' and other reporters that aren't necessary
  reporters = ['intellij'];

  if (coverageEnabled) {
    reporters.push('coverage');
    reporters.push('intellijCoverage');
  }

  config.plugins = plugins;
  config.reporters = reporters;

  config.singleRun = false;
  config.autoWatch = false;
  // specify runner port to have runner port info dumped to standard output
  config.runnerPort = constants.DEFAULT_RUNNER_PORT + 1;

  setBasePath(config);
  process.stdout.write('##intellij-event[basePath:' + config.basePath + ']\n');
};
