var config = require('./config');
var join = require('path').join;

var runner = config.requireKarmaModule('lib/runner.js');
var constant = config.requireKarmaModule('lib/constants');

var port = constant.DEFAULT_RUNNER_PORT;

function isPortBound(port, callback) {
    var net = require('net');
    var server = net.createServer();
    server.listen(port);
    var called = false;
    server.on('listening', function() {
        if (!called) {
            called = true;
            callback(false);
        }
        server.close();
    });
    server.on('error', function (e) {
        if (!called) {
            called = true;
            callback(true);
        }
    });
}

isPortBound(port, function(bound) {
    if (bound) {
        runner.run(config);
    }
    else {
        console.error("No server on port " + port);
    }
});
