var cli = require("./intellijCli.js")
  , fs = require('fs')
  , path = require('path')
  , EventEmitter = require('events').EventEmitter;

function copyFile(sourceFilePath, targetFilePath, callback) {
  var firstCall = true;

  var readStream = fs.createReadStream(sourceFilePath);
  readStream.on("error", function(err) {
    done(err);
  });
  var writeStream = fs.createWriteStream(targetFilePath);
  writeStream.on("error", function(err) {
    done(err);
  });
  writeStream.on("close", function() {
    done();
  });
  readStream.pipe(writeStream);

  function done(err) {
    if (firstCall) {
      callback(err);
      firstCall = false;
    }
  }
}

function extractLcovInfoFile(coverageDir, outLcovFilePath) {
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
                  copyFile(lcovFilePath, outLcovFilePath, function(err) {
                    if (!err) {
                      process.stdout.write('##intellij-event[coverage-finished:' + outLcovFilePath + ']\n');
                    }
                  });
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
  var lcovFilePath = null;

  var superOnRunStart = this.onRunStart.bind(this);
  this.onRunStart = function(browsers) {
    lcovFilePath = IntellijCoverageReporter.sharedLcovFilePath;
    IntellijCoverageReporter.sharedLcovFilePath = null;
    if (lcovFilePath) {
      superOnRunStart(browsers);
    }
  };

  var superOnBrowserComplete = this.onBrowserComplete.bind(this);
  this.onBrowserComplete = function(browser, result) {
    if (lcovFilePath) {
      superOnBrowserComplete(browser, result);
    }
  };

  var superOnRunComplete = this.onRunComplete.bind(this);
  this.onRunComplete = function (browsers, results) {
    if (lcovFilePath) {
      superOnRunComplete(browsers, results);
      emitter.emit('exit', function() {
        extractLcovInfoFile(rootConfig.coverageReporter.dir, lcovFilePath);
      });
    }
  };
}

(function () {
  process.stdin.resume();
  process.stdin.setEncoding('utf8');
  var buffer = '';
  var WRITE_COVERAGE_TO_MSG = 'write coverage to ';
  process.stdin.on('data', function(data) {
    buffer += data;
    var lines = buffer.split('\n');
    lines.forEach(function (line) {
      if (line.indexOf(WRITE_COVERAGE_TO_MSG) === 0) {
        IntellijCoverageReporter.sharedLcovFilePath = line.substring(WRITE_COVERAGE_TO_MSG.length);
      }
    });
    if (lines.length > 0) {
      buffer = lines[lines.length - 1];
    }
  });
}());


IntellijCoverageReporter.$inject = ['reporter:coverage', 'config', 'helper', 'logger'];

module.exports = IntellijCoverageReporter;
