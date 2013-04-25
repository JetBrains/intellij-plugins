var config = require('./config');
var constants = config.requireKarmaModule('lib/constants.js');

var reporter = config.requireKarmaModule('lib/reporter.js');
// registering 'intellij' reporter
reporter.Intellij = require('./intellij-reporter');

var server = config.requireKarmaModule('lib/server.js');

server.start({
    autoWatch: false,
    colors: false,
    configFile: config.configFilePath,
    reporters: ['intellij'],
    singleRun: false,
    logLevel : constants.LOG_INFO
});
