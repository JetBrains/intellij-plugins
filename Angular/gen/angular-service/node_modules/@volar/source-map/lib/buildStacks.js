"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.buildStacks = void 0;
function buildStacks(chunks, stacks) {
    let offset = 0;
    let index = 0;
    const result = [];
    for (const stack of stacks) {
        const start = offset;
        for (let i = 0; i < stack.length; i++) {
            const segment = chunks[index + i];
            if (typeof segment === 'string') {
                offset += segment.length;
            }
            else {
                offset += segment[0].length;
            }
        }
        index += stack.length;
        result.push({
            source: stack.stack,
            range: [start, offset],
        });
    }
    return result;
}
exports.buildStacks = buildStacks;
//# sourceMappingURL=buildStacks.js.map