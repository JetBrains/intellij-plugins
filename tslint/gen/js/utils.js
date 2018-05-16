"use strict";
exports.__esModule = true;
function getVersion(tslint) {
    var version = tslint.VERSION || (tslint.Linter && tslint.Linter.VERSION);
    if (version == null) {
        return { versionString: version, kind: 0 /* VERSION_3_AND_BEFORE */ };
    }
    var firstDot = version.indexOf(".");
    var majorVersion = firstDot == -1 ? version : version.substr(0, firstDot + 1);
    var kind = majorVersion && (Number(majorVersion) > 3) ?
        1 /* VERSION_4_AND_HIGHER */ :
        0 /* VERSION_3_AND_BEFORE */;
    return { versionString: version, kind: kind };
}
exports.getVersion = getVersion;
