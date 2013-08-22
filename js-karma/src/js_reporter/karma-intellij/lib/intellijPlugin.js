var IntellijReporter = require('./intellijReporter')
  , IntellijCoverageReporter = require('./intellijCoverageReporter');

var extensions = {};
extensions['reporter:' + IntellijReporter.reporterName] = ['type', IntellijReporter];
extensions['reporter:' + IntellijCoverageReporter.reporterName] = ['type', IntellijCoverageReporter];

module.exports = extensions;
