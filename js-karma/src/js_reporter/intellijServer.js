var ideCfg = require('./ideConfig.js');
var constants = ideCfg.requireKarmaModule('lib/constants.js');

var reporter = ideCfg.requireKarmaModule('lib/reporter.js');
// registering 'intellij' reporter
reporter.Intellij = require('./intellijReporter.js');

var server = ideCfg.requireKarmaModule('lib/server.js');

var options = {
    autoWatch: false,
    colors: false,
    configFile: ideCfg.getConfigFile(),
    reporters: ['intellij'],
    singleRun: false,
    logLevel : constants.LOG_INFO,
    runnerPort: constants.DEFAULT_RUNNER_PORT + 1
};

server.start(options);

function writeConfigToStdOut() {
    var cfg = ideCfg.requireKarmaModule('lib/config.js');
    var config = cfg.parseConfig(options.configFile, options);
    process.stdout.write('##intellij-command[' + JSON.stringify(config) + ']\n');
}

writeConfigToStdOut();
