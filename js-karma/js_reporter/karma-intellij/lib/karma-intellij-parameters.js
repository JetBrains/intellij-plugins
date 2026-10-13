/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

const PREFIX = '_INTELLIJ_KARMA_INTERNAL_PARAMETER_';

function getParam(name) {
  return process.env[PREFIX + name];
}

function getRequiredParam(name) {
  const value = getParam(name);
  if (value == null) {
    throw Error('Unspecified intellij internal parameter: ' + name);
  }
  return value;
}

function getUserConfigFilePath() {
  return getRequiredParam('user_config')
}

function isDebug() {
  return getParam('debug') != null;
}

function getCoverageTempDirPath() {
  return getParam('coverage_temp_dir')
}

function isWithCoverage() {
  return getCoverageTempDirPath() != null;
}

exports.getParam = getParam;
exports.getRequiredParam = getRequiredParam;
exports.getUserConfigFilePath = getUserConfigFilePath;
exports.isDebug = isDebug;
exports.getCoverageTempDirPath = getCoverageTempDirPath;
exports.isWithCoverage = isWithCoverage;
