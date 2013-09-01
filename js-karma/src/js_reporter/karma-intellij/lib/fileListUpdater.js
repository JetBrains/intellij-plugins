var intellijUtil = require('./intellijUtil');

function FileListUpdater(config, fileList) {
  var FILE_CHANGED_PREFIX = "changed-file:"
    , FILE_ADDED_PREFIX = "added-file:"
    , FILE_REMOVED_PREFIX = "removed-file:";

  intellijUtil.processStdInput(function (line) {
    var path;
    if (line.indexOf(FILE_CHANGED_PREFIX) === 0) {
      path = line.substring(FILE_CHANGED_PREFIX.length);
      fileList.changeFile(path);
    }
    else if (line.indexOf(FILE_ADDED_PREFIX) === 0) {
      path = line.substring(FILE_ADDED_PREFIX.length);
      fileList.addFile(path);
    }
    else if (line.indexOf(FILE_REMOVED_PREFIX) === 0) {
      path = line.substring(FILE_REMOVED_PREFIX.length);
      fileList.removeFile(path);
    }
    return true;
  });

  var paths = [];
  config.files.forEach(function (file) {
    var pattern = file.pattern;
    if (intellijUtil.isString(pattern)) {
      paths.push(pattern);
    }
  });

  intellijUtil.sendIntellijEvent('configFilePatterns', paths);
}

exports.FileListUpdater = FileListUpdater;
