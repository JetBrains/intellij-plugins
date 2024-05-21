"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.translateOffset = void 0;
function translateOffset(start, fromOffsets, toOffsets, lengths) {
    for (let i = 0; i < fromOffsets.length; i++) {
        const fromOffset = fromOffsets[i];
        const toOffset = toOffsets[i];
        const length = lengths[i];
        if (start >= fromOffset && start <= fromOffset + length) {
            return toOffset + start - fromOffset;
        }
    }
}
exports.translateOffset = translateOffset;
//# sourceMappingURL=translateOffset.js.map