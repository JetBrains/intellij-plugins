var config = require('./config');
var join = require('path').join;

var reporter = require(join(config.karmaPackageDir, 'lib/reporter.js'));
// registering 'intellij' reporter
reporter.Intellij = require('./intellij-reporter.js');

var server = require(join(config.karmaPackageDir, 'lib/server.js'));
server.start({
    autoWatch: false,
    colors: false,
    configFile: config.configFilePath,
    reporters: ['intellij'],
    singleRun: false
});
