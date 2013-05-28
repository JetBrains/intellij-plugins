var ideCfg = require('./ideConfig.js');
var constants = ideCfg.requireKarmaModule('lib/constants.js');
var server = ideCfg.requireKarmaModule('lib/server.js');
var cfg = ideCfg.requireKarmaModule('lib/config.js');

var config = cfg.parseConfig(ideCfg.getConfigFile(), {});
var extendedPlugins = config.plugins.slice();
extendedPlugins.push(require.resolve('./intellijReporter.js'));

var options = {
  autoWatch: false,
  colors: false,
  configFile: ideCfg.getConfigFile(),
  reporters: ['intellij'],
  singleRun: false,
  logLevel: constants.LOG_INFO,
  runnerPort: constants.DEFAULT_RUNNER_PORT + 1,
  plugins: extendedPlugins
};

process.stdout.write('##intellij-event[config:' + JSON.stringify(config) + ']\n');
server.start(options);
