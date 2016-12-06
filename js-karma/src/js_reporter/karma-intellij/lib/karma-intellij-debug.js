var cli = require('./intellijCli')
  , fs = require('fs')
  , path = require('path')
  , MAPPINGS_NAME = '%MAPPINGS%'
  , os = require('os');

function createPatchedDebugHtmlFile () {
  var debugHtmlFilePath = path.normalize(cli.getKarmaFilePath('./static/debug.html'));
  var debugHtmlFileContent = fs.readFileSync(debugHtmlFilePath, {encoding: 'utf8'});
  if (typeof debugHtmlFileContent !== 'string') {
    return printFailure('not a string');
  }
  var mappingsIndex = debugHtmlFileContent.indexOf(MAPPINGS_NAME);
  if (mappingsIndex < 0) {
    return printFailure('cannot find ' + MAPPINGS_NAME + ' in ' + debugHtmlFilePath);
  }
  var jsFilePath = path.resolve(__dirname, '../static/delay-karma-start-in-debug-mode.js');
  var jsFileContent = fs.readFileSync(jsFilePath, {encoding: 'utf8'});
  var result = debugHtmlFileContent.substring(0, mappingsIndex + MAPPINGS_NAME.length) + '\n' +
               jsFileContent +
               debugHtmlFileContent.substring(mappingsIndex + MAPPINGS_NAME.length);
  var dir = fs.mkdtempSync(path.join(os.tmpdir(), 'intellij-karma-'));
  var patchedDebugHtmlFilePath = path.join(dir, 'intellij-debug.html');
  fs.writeFileSync(patchedDebugHtmlFilePath, result);
  return patchedDebugHtmlFilePath;
}

function printFailure(message, err) {
  var text = 'intellij integration: failed to provide custom /debug.html: ' + message;
  if (err) {
    console.error(text, err);
  }
  else {
    console.error(text);
  }
}

exports.initCustomDebugFile = function (config) {
  if (config.customDebugFile == undefined) {
    try {
      config.customDebugFile = createPatchedDebugHtmlFile();
    }
    catch (e) {
      printFailure('failed to create custom debug.html: ', e);
    }
  }
};
