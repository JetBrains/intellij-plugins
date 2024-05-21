"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildMappings = void 0;
function buildMappings(chunks) {
    let length = 0;
    const mappings = [];
    for (const segment of chunks) {
        if (typeof segment === 'string') {
            length += segment.length;
        }
        else {
            mappings.push({
                source: segment[1],
                sourceOffsets: [segment[2]],
                generatedOffsets: [length],
                lengths: [segment[0].length],
                data: segment[3],
            });
            length += segment[0].length;
        }
    }
    return mappings;
}
exports.buildMappings = buildMappings;
//# sourceMappingURL=buildMappings.js.map