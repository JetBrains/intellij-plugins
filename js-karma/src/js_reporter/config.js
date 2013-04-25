var join = require('path').join;
var argv = process.argv;

var karmaPackageDir = argv.length >= 2 ? argv[2] : null;
if (!karmaPackageDir) {
    throw Error("Karma package directory isn't specified!");
}

var configFilePath = argv.length >= 3 ? argv[3] : null;
if (!configFilePath) {
    throw Error("Config file isn't specified!");
}

exports.configFilePath = configFilePath;

exports.karmaPackageDir = karmaPackageDir;

exports.requireKarmaModule = function(moduleName) {
    return require(join(karmaPackageDir, moduleName));
};
