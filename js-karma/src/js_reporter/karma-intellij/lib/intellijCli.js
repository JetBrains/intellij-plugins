var join = require('path').join;

var RUNNER_PORT_KEY = 'runnerPort'
  , CONFIG_FILE_KEY = 'configFile'
  , KARMA_PACKAGE_DIR_KEY = 'karmaPackageDir'
  , DEBUG_KEY = 'debug'
  , COVERAGE_TEMP_DIR = 'coverageTempDir';

function parseArguments() {
  var argv = process.argv
    , options = {};
  for (var i = 2; i < argv.length; i++) {
    var arg = argv[i];
    if (arg.indexOf('--') !== 0) {
      throw new Error('Illegal prefix ' + arg);
    }
    var ind = arg.indexOf('=');
    if (ind === -1) {
      throw new Error('No "=" symbol found in ' + arg);
    }
    var key = arg.substring(2, ind);
    options[key] = arg.substring(ind + 1);
  }
  return options;
}

var options = parseArguments();

function getKarmaPackageDir() {
  var karmaPackageDir = options[KARMA_PACKAGE_DIR_KEY];
  if (!karmaPackageDir) {
    throw Error("Karma package dir isn't specified.");
  }
  return karmaPackageDir;
}

function requireKarmaModule(moduleName) {
  var karmaPath = getKarmaPackageDir();
  return require(join(karmaPath, moduleName));
}

function getConfigFile() {
  var configFile = options[CONFIG_FILE_KEY];
  if (!configFile) {
    throw Error("Config file isn't specified.");
  }
  return configFile;
}

function getRunnerPort() {
  var runnerPortStr = options[RUNNER_PORT_KEY] || '';
  var runnerPort = parseInt(runnerPortStr, 10);
  if (isNaN(runnerPort)) {
    return undefined;
  }
  return runnerPort;
}

function isDebug() {
  var debugStr = options[DEBUG_KEY];
  return 'true' === debugStr;
}

function getCoverageTempDirPath() {
  return options[COVERAGE_TEMP_DIR];
}

exports.requireKarmaModule = requireKarmaModule;
exports.getConfigFile = getConfigFile;
exports.getRunnerPort = getRunnerPort;
exports.isDebug = isDebug;
exports.getCoverageTempDirPath = getCoverageTempDirPath;
