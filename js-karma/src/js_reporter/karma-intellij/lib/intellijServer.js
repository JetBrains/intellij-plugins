var ideCfg = require('./ideConfig.js')
  , server = ideCfg.requireKarmaModule('lib/server.js');

var cliOptions = {
  configFile: require.resolve('./intellijProxy.conf.js')
};

server.start(cliOptions);
