"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.binarySearch = void 0;
function binarySearch(values, searchValue) {
    let low = 0;
    let high = values.length - 1;
    let match;
    while (low <= high) {
        const mid = Math.floor((low + high) / 2);
        const midValue = values[mid];
        if (midValue < searchValue) {
            low = mid + 1;
        }
        else if (midValue > searchValue) {
            high = mid - 1;
        }
        else {
            low = mid;
            high = mid;
            match = mid;
            break;
        }
    }
    const finalLow = Math.max(Math.min(low, high, values.length - 1), 0);
    const finalHigh = Math.min(Math.max(low, high, 0), values.length - 1);
    return { low: finalLow, high: finalHigh, match };
}
exports.binarySearch = binarySearch;
//# sourceMappingURL=binarySearch.js.map