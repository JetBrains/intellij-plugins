var cli = require('./intellijCli.js')
  , server = cli.requireKarmaModule('lib/server.js');

server.start({
  configFile: require.resolve('./intellij.conf.js')
});
