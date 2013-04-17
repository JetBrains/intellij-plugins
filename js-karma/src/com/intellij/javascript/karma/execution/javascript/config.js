var argv = process.argv;
var configFilePath = argv.length >= 2 ? argv[2] : null;

if (!configFilePath) {
    throw Error("Config file isn't specified!");
}

var karmaPackageDir = argv.length >= 3 ? argv[3] : null;
if (!karmaPackageDir) {
    throw Error("Karma package directory isn't specified!");
}

exports.configFilePath = configFilePath;
exports.karmaPackageDir = karmaPackageDir;
