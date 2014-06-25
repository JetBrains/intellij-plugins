var join = require('path').join;

var SERVER_PORT_KEY = 'serverPort'
  , CONFIG_FILE_KEY = 'configFile'
  , KARMA_PACKAGE_DIR_KEY = 'karmaPackageDir'
  , DEBUG_KEY = 'debug'
  , URL_ROOT_KEY = 'urlRoot'
  , BROWSERS_KEY = 'browsers'
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

function getServerPort() {
  var serverPortStr = options[SERVER_PORT_KEY] || '';
  var serverPort = parseInt(serverPortStr, 10);
  if (isNaN(serverPort)) {
    return undefined;
  }
  return serverPort;
}

function isDebug() {
  var debugStr = options[DEBUG_KEY];
  return 'true' === debugStr;
}

function getUrlRoot() {
  return options[URL_ROOT_KEY];
}

function getBrowsers() {
  var str = options[BROWSERS_KEY];
  if (str == null) {
    return null;
  }
  return str.split(',');
}

function getCoverageTempDirPath() {
  return options[COVERAGE_TEMP_DIR];
}

/**
 * @returns {boolean} true, if run with coverage was requested
 */
function isWithCoverage() {
  return options.hasOwnProperty(COVERAGE_TEMP_DIR);
}

exports.requireKarmaModule = requireKarmaModule;
exports.getConfigFile = getConfigFile;
exports.getServerPort = getServerPort;
exports.isDebug = isDebug;
exports.getUrlRoot = getUrlRoot;
exports.getBrowsers = getBrowsers;
exports.getCoverageTempDirPath = getCoverageTempDirPath;
exports.isWithCoverage = isWithCoverage;
