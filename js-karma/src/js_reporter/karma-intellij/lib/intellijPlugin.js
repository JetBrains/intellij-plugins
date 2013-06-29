var IntellijReporter = require('./intellijReporter.js')
  , IntellijCoverageReporter = require('./intellijCoverageReporter.js');

module.exports = {
  'reporter:intellij' : ['type', IntellijReporter],
  'reporter:intellijCoverage' : ['type', IntellijCoverageReporter]
};
