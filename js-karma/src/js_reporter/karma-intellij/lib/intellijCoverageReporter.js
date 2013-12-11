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

function createKarmaCoverageReporter(injector, emitter, config) {
  try {
    var karmaCoverageReporterName = 'reporter:coverage';
    var locals = {
      emitter: ['value', emitter],
      config:  ['value', config]
    };
    var childInjector = injector.createChild([locals], [karmaCoverageReporterName]);
    return childInjector.get(karmaCoverageReporterName);
  }
  catch (ex) {
    return null;
  }
}

function IntellijCoverageReporter(injector, config) {
  var that = this;
  var emitter = new EventEmitter();
  var newConfig = {
    coverageReporter : {
      type : 'lcovonly',
      dir : cli.getCoverageTempDirPath()
    },
    basePath : config.basePath
  };

  var karmaCoverageReporter = createKarmaCoverageReporter(injector, emitter, newConfig);
  if (karmaCoverageReporter != null) {
    that = karmaCoverageReporter;
    init.call(karmaCoverageReporter, emitter, newConfig);
  }
  else {
    console.warn("IDE coverage reporter is disabled");
    this.adapters = [];
  }
  var coveragePreprocessorSpecifiedInConfig = isCoveragePreprocessorSpecified(config.preprocessors);
  IntellijCoverageReporter.reportCoverageStartupStatus(true, coveragePreprocessorSpecifiedInConfig, karmaCoverageReporter != null);
  return that;
}

function isCoveragePreprocessorSpecified(preprocessors) {
  if (!preprocessors) {
    return false;
  }
  for (var key in preprocessors) {
    if (Object.prototype.hasOwnProperty.call(preprocessors, key)) {
      if (preprocessors[key] == 'coverage') {
        return true;
      }
    }
  }
  return false;
}

IntellijCoverageReporter.reportCoverageStartupStatus = function (coverageReporterSpecifiedInConfig,
                                                                 coveragePreprocessorSpecifiedInConfig,
                                                                 coverageReporterFound) {
  var event = {
    coverageReporterSpecifiedInConfig : coverageReporterSpecifiedInConfig
  };
  if (coveragePreprocessorSpecifiedInConfig != null) {
    event.coveragePreprocessorSpecifiedInConfig = coveragePreprocessorSpecifiedInConfig;
  }
  if (coverageReporterFound != null) {
    event.coverageReporterFound = coverageReporterFound;
  }
  intellijUtil.sendIntellijEvent('coverageStartupStatus', event);
};

// invoked in context of original karmaCoverageReporter
function init(emitter, rootConfig) {
  var currentBrowser = null;

  var superOnRunStart = this.onRunStart.bind(this);
  this.onRunStart = function(browsers) {
    currentBrowser = findBestBrowser(browsers);
    var browserArray = [];
    if (currentBrowser) {
      browserArray = [currentBrowser];
    }
    superOnRunStart(browserArray);
  };

  if (typeof this.onBrowserStart === 'function') {
    var superOnBrowserStart = this.onBrowserStart.bind(this);
    this.onBrowserStart = function(browser) {
      if (browser === currentBrowser) {
        superOnBrowserStart.apply(this, arguments);
      }
    };
  }

  var superOnSpecComplete = this.onSpecComplete.bind(this);
  this.onSpecComplete = function(browser/*, result*/) {
    if (browser === currentBrowser) {
      superOnSpecComplete.apply(this, arguments);
    }
  };

  var superOnBrowserComplete = this.onBrowserComplete.bind(this);
  this.onBrowserComplete = function(browser/*, result*/) {
    if (browser === currentBrowser && currentBrowser) {
      currentBrowser.argumentsForOnBrowserComplete = arguments;
    }
  };

  var superOnRunComplete = this.onRunComplete.bind(this);
  this.onRunComplete = function (browsers/*, results*/) {
    var found = currentBrowser && containsBrowser(browsers, currentBrowser);
    if (found) {
      if (currentBrowser.argumentsForOnBrowserComplete) {
        superOnBrowserComplete.apply(this, currentBrowser.argumentsForOnBrowserComplete);
      }
      superOnRunComplete([currentBrowser]);

      var done = function() {
        findLcovInfoFile(rootConfig.coverageReporter.dir, function(lcovFilePath) {
          intellijUtil.sendIntellijEvent('coverageFinished', lcovFilePath);
        });
      };
      if (typeof this.onExit === 'function') {
        this.onExit(done);
      }
      else {
        // to keep backward compatibility
        emitter.emit('exit', done);
      }
    }
  };
}

function findBestBrowser(browsers) {
  if (browsers.length <= 1) {
    return getAnyBrowser(browsers);
  }
  var browserNamesInPreferredOrder = ['Chrome ', 'Firefox ', 'Safari ', 'Opera '];
  var len = browserNamesInPreferredOrder.length;
  for (var i = 0; i < len; i++) {
    var browser = findBrowserByName(browsers, browserNamesInPreferredOrder[i]);
    if (browser) {
      return browser;
    }
  }
  return getAnyBrowser(browsers);
}

function getAnyBrowser(browsers) {
  var result = null;
  browsers.forEach(function (browser) {
    if (result == null) {
      result = browser;
    }
  });
  return result;
}

function containsBrowser(browsers, targetBrowser) {
  var result = false;
  browsers.forEach(function (browser) {
    if (browser === targetBrowser) {
      result = true;
    }
  });
  return result;
}

function findBrowserByName(browsers, browserNamePrefix) {
  var result = null;
  browsers.forEach(function (browser) {
    var browserName = browser.name;
    if (result == null && intellijUtil.isString(browserName) && browserName.indexOf(browserNamePrefix) === 0) {
      result = browser;
    }
  });
  return result;
}

IntellijCoverageReporter.$inject = ['injector', 'config'];
IntellijCoverageReporter.reporterName = 'intellijCoverage_33e284dac2b015a9da50d767dc3fa58a';

module.exports = IntellijCoverageReporter;
