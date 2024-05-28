"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.translateOffset = void 0;
function translateOffset(start, fromOffsets, toOffsets, fromLengths, toLengths = fromLengths) {
    for (let i = 0; i < fromOffsets.length; i++) {
        const fromOffset = fromOffsets[i];
        const fromLength = fromLengths[i];
        if (start >= fromOffset && start <= fromOffset + fromLength) {
            const toLength = toLengths[i];
            const toOffset = toOffsets[i];
            let rangeOffset = Math.min(start - fromOffset, toLength);
            return toOffset + rangeOffset;
        }
    }
}
exports.translateOffset = translateOffset;
//# sourceMappingURL=translateOffset.js.map