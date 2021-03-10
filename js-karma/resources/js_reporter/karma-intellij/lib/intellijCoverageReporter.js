/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

var intellijParameters = require('./karma-intellij-parameters')
  , intellijUtil = require('./intellijUtil.js')
  , fs = require('fs')
  , path = require('path')
  , LCOV_FILE_NAMES = ['lcov.info', 'lcovonly']
  , extraLcovLocations = ['coverage'];

const KARMA_COVERAGE_REPORTER_NAME = 'coverage';

function hasCoveragePreprocessor(config) {
  const preprocessorValues = Object.values(config.preprocessors);
  return typeof preprocessorValues.flat === 'function' && preprocessorValues.flat().indexOf('coverage') >= 0;
}

function isKarmaCoveragePluginDeclared(config) {
  const plugins = config.plugins
  return Array.isArray(plugins) && (
    plugins.some((plugin) => plugin.includes('/karma-coverage/')) ||
    plugins.indexOf('karma-*') >= 0 && hasKarmaCoverageDependency()
  );
}

function hasKarmaCoverageDependency() {
  const packageJsonPath = path.join(process.cwd(), 'package.json')
  if (fs.existsSync(packageJsonPath)) {
    try {
      const packageJson = require(packageJsonPath);
      const karmaCoverage = 'karma-coverage';
      return (packageJson.dependencies && karmaCoverage in packageJson.dependencies) ||
        (packageJson.devDependencies && karmaCoverage in packageJson.devDependencies)
    }
    finally {
    }
  }
}

function addCoverageReporterIfNeeded(config, reporters) {
  if (reporters.indexOf(KARMA_COVERAGE_REPORTER_NAME) < 0 && hasCoveragePreprocessor(config) && isKarmaCoveragePluginDeclared(config)) {
    reporters.push(KARMA_COVERAGE_REPORTER_NAME);
    console.log('intellij: \'' + KARMA_COVERAGE_REPORTER_NAME + '\' reporter was added as karma-coverage is used');
  }
}

/**
 * Configures coverage if 'Run with coverage' action performed
 * @param {Object} config
 */
function configureCoverage(config) {
  var reporters = config.reporters || [];
  if (intellijParameters.isWithCoverage()) {
    reporters.push(IntellijCoverageReporter.reporterName);
    if (!config.coverageReporter) {
      config.coverageReporter = {reporters: []};
    }
    else if (!config.coverageReporter.reporters) {
      // https://github.com/karma-runner/karma-coverage/blob/master/docs/configuration.md
      // the trick from https://github.com/karma-runner/karma-coverage/blob/v1.1.2/lib/reporter.js#L53
      Object.defineProperty(config.coverageReporter, 'reporters', {
        value: [config.coverageReporter],
        enumerable: false // to avoid "TypeError: Converting circular structure to JSON" (WEB-33542)
      });
    }
    config.coverageReporter.reporters.push({
      type : 'lcovonly',
      dir : path.join(intellijParameters.getCoverageTempDirPath())
    });
    configureKarmaTypeScript(config);
    addCoverageReporterIfNeeded(config, reporters);
  }
  else if (canCoverageBeDisabledSafely(config.coverageReporter)) {
    if (reporters.indexOf(KARMA_COVERAGE_REPORTER_NAME) >= 0) {
      reporters = intellijUtil.removeAll(reporters, KARMA_COVERAGE_REPORTER_NAME);
      console.log('intellij: \'' + KARMA_COVERAGE_REPORTER_NAME + '\' reporter was removed for faster ' +
        (intellijParameters.isDebug() ? 'debug' : 'run'));
    }
  }
  config.reporters = reporters;
}

function configureKarmaTypeScript(config) {
  var reporters = config.reporters || [];
  var frameworks = config.frameworks || [];
  if (reporters.indexOf('karma-typescript') >= 0 || frameworks.indexOf('karma-typescript') >= 0) {
    if (!config.karmaTypescriptConfig) {
      config.karmaTypescriptConfig = {};
    }
  }
  if (config.karmaTypescriptConfig) {
    var reports = config.karmaTypescriptConfig.reports;
    if (!reports) {
      reports = {};
      config.karmaTypescriptConfig.reports = reports;
    }
    if (!reports.lcovonly) {
      reports.lcovonly = intellijParameters.getCoverageTempDirPath();
    }
    extraLcovLocations.push(reports.lcovonly);
  }
}

/**
 * @param {Object} coverageReporter
 * @returns {boolean} true if tests can be successfully run without coverage reporter and preprocessor
 */
function canCoverageBeDisabledSafely(coverageReporter) {
  return !coverageReporter || (
      !Object.prototype.hasOwnProperty.call(coverageReporter, 'instrumenter') &&
      !Object.prototype.hasOwnProperty.call(coverageReporter, 'instrumenters')
    );
}

function sendCoverageReportFile(filePath) {
  intellijUtil.sendIntellijEvent('coverageFinished', filePath || '');
}

function IntellijCoverageReporter(config) {
  this.adapters = [];
  var initialCoverageReports;
  this.onRunStart = function () {
    initialCoverageReports = findCoverageReports(config);
  };
  this.onRunComplete = function () {
    checkRepeatedlyUntilPassed(function (expired) {
      var coverageReports = findCoverageReports(config);
      var filePath = findModifiedCoverageReport(initialCoverageReports, coverageReports);
      if (filePath) {
        reportOnceModificationsSettleDown(filePath, coverageReports[filePath]);
        return true;
      }
      if (expired) {
        sendCoverageReportFile(null);
      }
      return false;
    }, 100, 3000);
  };
}

/**
 * @param {Function} checkCallback
 * @param {number} delay
 * @param {number} expireTimeout
 */
function checkRepeatedlyUntilPassed(checkCallback, delay, expireTimeout) {
  var startTime = new Date().getTime();
  setTimeout(function f() {
    if (new Date().getTime() - startTime > expireTimeout) {
      checkCallback(true);
    }
    else if (!checkCallback(false)) {
      setTimeout(f, delay);
    }
  }, delay);
}

function reportOnceModificationsSettleDown(filePath, mtime) {
  checkRepeatedlyUntilPassed(function (expired) {
    try {
      var stat = fs.statSync(filePath);
      if (stat && stat.isFile()) {
        if (stat.mtime.getTime() === mtime.getTime()) {
          sendCoverageReportFile(filePath);
          return true;
        }
        else {
          if (expired) {
            sendCoverageReportFile(null);
          }
          console.log('IntelliJ: ' + filePath + ' has been modified ' + stat.mtime + ', waiting until it settles down');
          mtime = stat.mtime;
          return false;
        }
      }
    }
    catch (e) {}
    sendCoverageReportFile(null);
    return true;
  }, 500, 10000);
}

function findModifiedCoverageReport(initialCoverageInfo, coverageInfo) {
  return Object.keys(coverageInfo).find(function (filePath) {
    var initial_mtime = initialCoverageInfo[filePath];
    var mtime = coverageInfo[filePath];
    return !initial_mtime || initial_mtime.getTime() !== mtime.getTime();
  });
}

function findCoverageReports(config) {
  var locations = {};
  config.coverageReporter.reporters.forEach(function (reporter) {
    if (reporter.dir) {
      locations[reporter.dir] = true;
    }
  });
  extraLcovLocations.forEach(function (location) {
    locations[location] = true;
  });
  var coverageReports = {};
  Object.keys(locations).forEach(function (location) {
    findCoverageReportsInDirectory(location, coverageReports);
  });
  return coverageReports;
}

function findCoverageReportsInDirectory(coverageDir, coverageReports) {
  try {
    coverageDir = path.resolve(coverageDir);
    var children = fs.readdirSync(coverageDir);
    if (children) {
      children.forEach(function (fileName) {
        var filePath = path.join(coverageDir, fileName);
        tryAddLcovInfo(filePath, coverageReports);
        try {
          var stats = fs.statSync(filePath);
          if (stats && stats.isDirectory()) {
            LCOV_FILE_NAMES.forEach(function (name) {
              tryAddLcovInfo(path.join(filePath, name), coverageReports);
            });
          }
        }
        catch (e) {}
      });
    }
  }
  catch (e) {}
}

function tryAddLcovInfo(filePath, coverageReports) {
  if (LCOV_FILE_NAMES.indexOf(path.basename(filePath)) >= 0) {
    try {
      var stat = fs.statSync(filePath);
      if (stat && stat.isFile()) {
        coverageReports[filePath] = stat.mtime;
      }
    }
    catch (e) {}
  }
}
IntellijCoverageReporter.$inject = ['config'];
IntellijCoverageReporter.reporterName = 'intellijCoverage_33e284dac2b015a9da50d767dc3fa58a';
IntellijCoverageReporter.configureCoverage = configureCoverage;

module.exports = IntellijCoverageReporter;
