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

function IntellijCoverageReporter(someKarmaCoverageReporter, config, helper, logger) {
  var rootConfig = {
    coverageReporter : {
      type : 'lcovonly',
      dir : cli.getCoverageTempDirPath()
    },
    basePath : config.basePath
  };
  var emitter = new EventEmitter();
  var KarmaCoverageReporter = someKarmaCoverageReporter.constructor;
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

IntellijCoverageReporter.$inject = ['reporter:coverage', 'config', 'helper', 'logger'];

module.exports = IntellijCoverageReporter;
