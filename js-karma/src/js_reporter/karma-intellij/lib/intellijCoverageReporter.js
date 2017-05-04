var cli = require('./intellijCli.js')
  , intellijUtil = require('./intellijUtil.js')
  , fs = require('fs')
  , path = require('path')
  , LCOV_FILE_NAMES = ['lcov.info', 'lcovonly']
  , extraLcovLocations = ['coverage'];

/**
 * Preconfigures coverage if 'Run with coverage' action performed.
 * The preconfigure config is passed to original karma config, so it's possible to  makes sense
 * @param {Object} config
 */
function preconfigureCoverage(config) {
  if (cli.isWithCoverage()) {
    configureAngularCliCoverage(config);
  }
}

function configureAngularCliCoverage(config) {
  var angularCli = config.angularCli;
  if (!angularCli) {
    angularCli = {};
    config.angularCli = angularCli;
  }
  angularCli.codeCoverage = true;
  angularCli.sourcemaps = true;
}

/**
 * Configures coverage if 'Run with coverage' action performed
 * @param {Object} config
 */
function configureCoverage(config) {
  var reporters = config.reporters || [];
  if (cli.isWithCoverage()) {
    reporters.push(IntellijCoverageReporter.reporterName);
    var coverageReporter = config.coverageReporter;
    if (!coverageReporter) {
      coverageReporter = {reporters: []};
      config.coverageReporter = coverageReporter;
    }
    else if (!coverageReporter.reporters) {
      // https://github.com/karma-runner/karma-coverage/blob/master/docs/configuration.md
      // the trick from https://github.com/karma-runner/karma-coverage/blob/v1.1.1/lib/reporter.js#L53
      coverageReporter.reporters = [coverageReporter];
    }
    coverageReporter.reporters.push({
      type : 'lcovonly',
      dir : path.join(cli.getCoverageTempDirPath())
    });
    configureKarmaTypeScript(config);
  }
  else if (canCoverageBeDisabledSafely(config.coverageReporter)) {
    var karmaCoverageReporterName = 'coverage';
    if (reporters.indexOf(karmaCoverageReporterName) >= 0) {
      reporters = intellijUtil.removeAll(reporters, karmaCoverageReporterName);
      console.log('IntelliJ integration disabled coverage for faster run and debug capabilities');
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
      reports.lcovonly = cli.getCoverageTempDirPath();
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
IntellijCoverageReporter.preconfigureCoverage = preconfigureCoverage;

module.exports = IntellijCoverageReporter;
