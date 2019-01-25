"use strict";
// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
