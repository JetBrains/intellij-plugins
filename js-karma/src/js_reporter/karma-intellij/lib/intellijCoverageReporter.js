var cli = require('./intellijCli.js')
  , intellijUtil = require('./intellijUtil.js')
  , fs = require('fs')
  , path = require('path')
  , EventEmitter = require('events').EventEmitter;

function findLcovInfoFile(coverageDir, callback) {
  var first = true;
  fs.readdir(coverageDir, function(err, files) {
    if (!err && files) {
      files.forEach(function(fileName) {
        var browserDir = path.join(coverageDir, fileName);
        fs.stat(browserDir, function(err, stats) {
          if (!err && stats && stats.isDirectory()) {
            var lcovFilePath = path.join(browserDir, "lcov.info");
            fs.stat(lcovFilePath, function(err, stats) {
              if (!err && stats && stats.isFile()) {
                if (first) {
                  first = false;
                  callback(lcovFilePath);
                }
              }
            });
          }
        });
      });
    }
  });
}

function findKarmaCoverageReporterConstructor(injector) {
  try {
    var someKarmaCoverageReporter = injector.get('reporter:coverage');
    return someKarmaCoverageReporter.constructor;
  }
  catch (ex) {
    return null;
  }
}

function IntellijCoverageReporter(injector, config, helper, logger) {
  var KarmaCoverageReporterConstructor = findKarmaCoverageReporterConstructor(injector);
  if (KarmaCoverageReporterConstructor != null) {
    init.call(this, KarmaCoverageReporterConstructor, config, helper, logger);
  }
  else {
    console.warn("IDE coverage reporter is disabled");
    this.adapters = [];
  }
  IntellijCoverageReporter.reportCoverageStartupStatus(true, KarmaCoverageReporterConstructor != null);
}

IntellijCoverageReporter.reportCoverageStartupStatus = function (coverageReporterSpecifiedInConfig, coverageReporterFound) {
  var event = {
    coverageReporterSpecifiedInConfig : coverageReporterSpecifiedInConfig
  };
  if (coverageReporterFound == null) {
    coverageReporterFound = true;
  }
  event.coverageReporterFound = coverageReporterFound;
  intellijUtil.sendIntellijEvent('coverageStartupStatus', event);
};

function init(KarmaCoverageReporter, config, helper, logger) {
  var rootConfig = {
    coverageReporter : {
      type : 'lcovonly',
      dir : cli.getCoverageTempDirPath()
    },
    basePath : config.basePath
  };

  var emitter = new EventEmitter();
  KarmaCoverageReporter.call(this, rootConfig, emitter, helper, logger);

  // methods
  //   this.onRunStart
  //   this.onBrowserComplete
  // are inherited from CoverageReporter defined in karma-coverage/lib/reporter.js

  var superOnRunComplete = this.onRunComplete.bind(this);
  this.onRunComplete = function (browsers, results) {
    superOnRunComplete(browsers, results);
    emitter.emit('exit', function() {
      findLcovInfoFile(rootConfig.coverageReporter.dir, function(lcovFilePath) {
        intellijUtil.sendIntellijEvent('coverageFinished', lcovFilePath);
      });
    });
  };
}

IntellijCoverageReporter.$inject = ['injector', 'config', 'helper', 'logger'];
IntellijCoverageReporter.reporterName = 'intellijCoverage_33e284dac2b015a9da50d767dc3fa58a';

module.exports = IntellijCoverageReporter;
