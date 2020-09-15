/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var join = require('path').join;

var SERVER_PORT_KEY = 'serverPort'
  , CONFIG_FILE_KEY = 'configFile'
  , DEBUG_KEY = 'debug'
  , PROTOCOL_KEY = 'protocol'
  , URL_ROOT_KEY = 'urlRoot'
  , BROWSERS_KEY = 'browsers'
  , COVERAGE_TEMP_DIR = 'coverageTempDir'
  , TEST_NAME_PATTERN = 'testNamePattern';

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

function getProtocol() {
  return options[PROTOCOL_KEY];
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

function getTestNamePattern() {
  return options[TEST_NAME_PATTERN];
}

function isLastTestRunWithTestNameFilter() {
  return options['lastTestRunWithTestNameFilter'] === 'true';
}

exports.getConfigFile = getConfigFile;
exports.getServerPort = getServerPort;
exports.isDebug = isDebug;
exports.getProtocol = getProtocol;
exports.getUrlRoot = getUrlRoot;
exports.getBrowsers = getBrowsers;
exports.getCoverageTempDirPath = getCoverageTempDirPath;
exports.isWithCoverage = isWithCoverage;
exports.getTestNamePattern = getTestNamePattern;
exports.isLastTestRunWithTestNameFilter = isLastTestRunWithTestNameFilter;
