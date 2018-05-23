"use strict";
exports.__esModule = true;
function getVersion(tslint) {
    var version = tslint.VERSION || (tslint.Linter && tslint.Linter.VERSION);
    if (version == null) {
        return { major: 3 };
    }
    var numbers = version.split(".").map(function (value) { return Number(value); });
    return {
        raw: version,
        major: numbers.length > 0 ? numbers[0] : undefined,
        minor: numbers.length > 1 ? numbers[1] : undefined,
        patch: numbers.length > 2 ? numbers[2] : undefined
    };
}
exports.getVersion = getVersion;
