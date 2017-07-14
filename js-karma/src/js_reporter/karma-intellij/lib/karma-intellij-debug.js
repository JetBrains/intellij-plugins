var cli = require('./intellijCli')
  , fs = require('fs')
  , path = require('path')
  , MAPPINGS_NAME = '%MAPPINGS%'
  , HTML_FILE_NAME = 'context.html'
  , os = require('os');

function createPatchedContextHtmlFile () {
  var contextHtmlFilePath = path.normalize(cli.getKarmaFilePath('./static/' + HTML_FILE_NAME));
  var contextHtmlFileContent = fs.readFileSync(contextHtmlFilePath, {encoding: 'utf8'});
  if (typeof contextHtmlFileContent !== 'string') {
    return printFailure('not a string');
  }
  var mappingsIndex = contextHtmlFileContent.indexOf(MAPPINGS_NAME);
  if (mappingsIndex < 0) {
    return printFailure('cannot find ' + MAPPINGS_NAME + ' in ' + contextHtmlFilePath);
  }
  var jsFilePath = path.resolve(__dirname, '../static/delay-karma-start-in-debug-mode.js');
  var jsFileContent = fs.readFileSync(jsFilePath, {encoding: 'utf8'});
  var result = contextHtmlFileContent.substring(0, mappingsIndex + MAPPINGS_NAME.length) + '\n' +
               jsFileContent +
               contextHtmlFileContent.substring(mappingsIndex + MAPPINGS_NAME.length);
  var dir = fs.mkdtempSync(path.join(os.tmpdir(), 'intellij-karma-'));
  var patchedHtmlFilePath = path.join(dir, 'intellij-' + HTML_FILE_NAME);
  fs.writeFileSync(patchedHtmlFilePath, result);
  return patchedHtmlFilePath;
}

function printFailure(message, err) {
  var text = 'intellij integration: failed to provide custom /' + HTML_FILE_NAME + ': ' + message;
  if (err) {
    console.error(text, err);
  }
  else {
    console.error(text);
  }
}

exports.initCustomContextFile = function (config) {
  if (config.customContextFile == null) {
    try {
      config.customContextFile = createPatchedContextHtmlFile();
    }
    catch (e) {
      printFailure('failed to create custom ' + HTML_FILE_NAME + ': ', e);
    }
  }
};
