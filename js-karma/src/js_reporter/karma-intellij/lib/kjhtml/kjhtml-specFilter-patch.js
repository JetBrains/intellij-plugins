// Workaround for https://github.com/taras42/karma-jasmine-html-reporter/issues/25

var KJHTML_PATTERN = /([\\/]karma-jasmine-html-reporter[\\/])/i;

var createPattern = function(path) {
  return {pattern: path, included: true, served: true, watched: false};
};

exports.apply = function (configFiles) {
  var startInd = -1;
  var endInd = -1;
  configFiles.forEach(function(file, index) {
    if (KJHTML_PATTERN.test(file.pattern)) {
      if (startInd === -1) {
        startInd = index;
      }
      endInd = index;
    }
  });
  if (startInd !== -1) {
    configFiles.splice(startInd, 0, createPattern(__dirname + '/intellij-save-specFilter-before-kjhtml.js'));
    endInd++;
    configFiles.splice(endInd + 1, 0, createPattern(__dirname + '/intellij-restore-specFilter-after-kjhtml.js'))
  }
};
