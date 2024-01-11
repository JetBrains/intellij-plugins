"use strict";
/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
Object.defineProperty(exports, "__esModule", { value: true });
exports.computeLineStartsMap = exports.getLineAndCharacterFromPosition = void 0;
/*
 * Line mapping utilities which can be used to retrieve line and character based
 * on an absolute character position in a given file. This functionality is similar
 * to TypeScript's "ts.getLineAndCharacterFromPosition" utility, but we cannot leverage
 * their logic for line mappings as it's internal and we need to generate line mappings
 * for non-TypeScript files such as HTML templates or stylesheets.
 *
 * Line and character can be retrieved by splitting a given source text based on
 * line breaks into line start entries. Later when a specific position is requested,
 * the closest line-start position is determined based on the given position.
 */
const LF_CHAR = 10;
const CR_CHAR = 13;
const LINE_SEP_CHAR = 8232;
const PARAGRAPH_CHAR = 8233;
/** Gets the line and character for the given position from the line starts map. */
function getLineAndCharacterFromPosition(lineStartsMap, position) {
    const lineIndex = findClosestLineStartPosition(lineStartsMap, position);
    return { character: position - lineStartsMap[lineIndex], line: lineIndex };
}
exports.getLineAndCharacterFromPosition = getLineAndCharacterFromPosition;
/**
 * Computes the line start map of the given text. This can be used in order to
 * retrieve the line and character of a given text position index.
 */
function computeLineStartsMap(text) {
    const result = [0];
    let pos = 0;
    while (pos < text.length) {
        const char = text.charCodeAt(pos++);
        // Handles the "CRLF" line break. In that case we peek the character
        // after the "CR" and check if it is a line feed.
        if (char === CR_CHAR) {
            if (text.charCodeAt(pos) === LF_CHAR) {
                pos++;
            }
            result.push(pos);
        }
        else if (char === LF_CHAR || char === LINE_SEP_CHAR || char === PARAGRAPH_CHAR) {
            result.push(pos);
        }
    }
    result.push(pos);
    return result;
}
exports.computeLineStartsMap = computeLineStartsMap;
/** Finds the closest line start for the given position. */
function findClosestLineStartPosition(linesMap, position, low = 0, high = linesMap.length - 1) {
    while (low <= high) {
        const pivotIndex = Math.floor((low + high) / 2);
        const pivotEl = linesMap[pivotIndex];
        if (pivotEl === position) {
            return pivotIndex;
        }
        else if (position > pivotEl) {
            low = pivotIndex + 1;
        }
        else {
            high = pivotIndex - 1;
        }
    }
    // In case there was no exact match, return the closest "lower" line index. We also
    // subtract the index by one because want the index of the previous line start.
    return low - 1;
}
//# sourceMappingURL=data:application/json;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoibGluZS1tYXBwaW5ncy5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzIjpbIi4uLy4uLy4uLy4uLy4uLy4uLy4uLy4uL3NyYy9jZGsvc2NoZW1hdGljcy91cGRhdGUtdG9vbC91dGlscy9saW5lLW1hcHBpbmdzLnRzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiI7QUFBQTs7Ozs7O0dBTUc7OztBQUVIOzs7Ozs7Ozs7O0dBVUc7QUFFSCxNQUFNLE9BQU8sR0FBRyxFQUFFLENBQUM7QUFDbkIsTUFBTSxPQUFPLEdBQUcsRUFBRSxDQUFDO0FBQ25CLE1BQU0sYUFBYSxHQUFHLElBQUksQ0FBQztBQUMzQixNQUFNLGNBQWMsR0FBRyxJQUFJLENBQUM7QUFPNUIsbUZBQW1GO0FBQ25GLFNBQWdCLCtCQUErQixDQUFDLGFBQXVCLEVBQUUsUUFBZ0I7SUFDdkYsTUFBTSxTQUFTLEdBQUcsNEJBQTRCLENBQUMsYUFBYSxFQUFFLFFBQVEsQ0FBQyxDQUFDO0lBQ3hFLE9BQU8sRUFBQyxTQUFTLEVBQUUsUUFBUSxHQUFHLGFBQWEsQ0FBQyxTQUFTLENBQUMsRUFBRSxJQUFJLEVBQUUsU0FBUyxFQUFDLENBQUM7QUFDM0UsQ0FBQztBQUhELDBFQUdDO0FBRUQ7OztHQUdHO0FBQ0gsU0FBZ0Isb0JBQW9CLENBQUMsSUFBWTtJQUMvQyxNQUFNLE1BQU0sR0FBYSxDQUFDLENBQUMsQ0FBQyxDQUFDO0lBQzdCLElBQUksR0FBRyxHQUFHLENBQUMsQ0FBQztJQUNaLE9BQU8sR0FBRyxHQUFHLElBQUksQ0FBQyxNQUFNLEVBQUUsQ0FBQztRQUN6QixNQUFNLElBQUksR0FBRyxJQUFJLENBQUMsVUFBVSxDQUFDLEdBQUcsRUFBRSxDQUFDLENBQUM7UUFDcEMsb0VBQW9FO1FBQ3BFLGlEQUFpRDtRQUNqRCxJQUFJLElBQUksS0FBSyxPQUFPLEVBQUUsQ0FBQztZQUNyQixJQUFJLElBQUksQ0FBQyxVQUFVLENBQUMsR0FBRyxDQUFDLEtBQUssT0FBTyxFQUFFLENBQUM7Z0JBQ3JDLEdBQUcsRUFBRSxDQUFDO1lBQ1IsQ0FBQztZQUNELE1BQU0sQ0FBQyxJQUFJLENBQUMsR0FBRyxDQUFDLENBQUM7UUFDbkIsQ0FBQzthQUFNLElBQUksSUFBSSxLQUFLLE9BQU8sSUFBSSxJQUFJLEtBQUssYUFBYSxJQUFJLElBQUksS0FBSyxjQUFjLEVBQUUsQ0FBQztZQUNqRixNQUFNLENBQUMsSUFBSSxDQUFDLEdBQUcsQ0FBQyxDQUFDO1FBQ25CLENBQUM7SUFDSCxDQUFDO0lBQ0QsTUFBTSxDQUFDLElBQUksQ0FBQyxHQUFHLENBQUMsQ0FBQztJQUNqQixPQUFPLE1BQU0sQ0FBQztBQUNoQixDQUFDO0FBbEJELG9EQWtCQztBQUVELDJEQUEyRDtBQUMzRCxTQUFTLDRCQUE0QixDQUNuQyxRQUFhLEVBQ2IsUUFBVyxFQUNYLEdBQUcsR0FBRyxDQUFDLEVBQ1AsSUFBSSxHQUFHLFFBQVEsQ0FBQyxNQUFNLEdBQUcsQ0FBQztJQUUxQixPQUFPLEdBQUcsSUFBSSxJQUFJLEVBQUUsQ0FBQztRQUNuQixNQUFNLFVBQVUsR0FBRyxJQUFJLENBQUMsS0FBSyxDQUFDLENBQUMsR0FBRyxHQUFHLElBQUksQ0FBQyxHQUFHLENBQUMsQ0FBQyxDQUFDO1FBQ2hELE1BQU0sT0FBTyxHQUFHLFFBQVEsQ0FBQyxVQUFVLENBQUMsQ0FBQztRQUVyQyxJQUFJLE9BQU8sS0FBSyxRQUFRLEVBQUUsQ0FBQztZQUN6QixPQUFPLFVBQVUsQ0FBQztRQUNwQixDQUFDO2FBQU0sSUFBSSxRQUFRLEdBQUcsT0FBTyxFQUFFLENBQUM7WUFDOUIsR0FBRyxHQUFHLFVBQVUsR0FBRyxDQUFDLENBQUM7UUFDdkIsQ0FBQzthQUFNLENBQUM7WUFDTixJQUFJLEdBQUcsVUFBVSxHQUFHLENBQUMsQ0FBQztRQUN4QixDQUFDO0lBQ0gsQ0FBQztJQUVELG1GQUFtRjtJQUNuRiwrRUFBK0U7SUFDL0UsT0FBTyxHQUFHLEdBQUcsQ0FBQyxDQUFDO0FBQ2pCLENBQUMiLCJzb3VyY2VzQ29udGVudCI6WyIvKipcbiAqIEBsaWNlbnNlXG4gKiBDb3B5cmlnaHQgR29vZ2xlIExMQyBBbGwgUmlnaHRzIFJlc2VydmVkLlxuICpcbiAqIFVzZSBvZiB0aGlzIHNvdXJjZSBjb2RlIGlzIGdvdmVybmVkIGJ5IGFuIE1JVC1zdHlsZSBsaWNlbnNlIHRoYXQgY2FuIGJlXG4gKiBmb3VuZCBpbiB0aGUgTElDRU5TRSBmaWxlIGF0IGh0dHBzOi8vYW5ndWxhci5pby9saWNlbnNlXG4gKi9cblxuLypcbiAqIExpbmUgbWFwcGluZyB1dGlsaXRpZXMgd2hpY2ggY2FuIGJlIHVzZWQgdG8gcmV0cmlldmUgbGluZSBhbmQgY2hhcmFjdGVyIGJhc2VkXG4gKiBvbiBhbiBhYnNvbHV0ZSBjaGFyYWN0ZXIgcG9zaXRpb24gaW4gYSBnaXZlbiBmaWxlLiBUaGlzIGZ1bmN0aW9uYWxpdHkgaXMgc2ltaWxhclxuICogdG8gVHlwZVNjcmlwdCdzIFwidHMuZ2V0TGluZUFuZENoYXJhY3RlckZyb21Qb3NpdGlvblwiIHV0aWxpdHksIGJ1dCB3ZSBjYW5ub3QgbGV2ZXJhZ2VcbiAqIHRoZWlyIGxvZ2ljIGZvciBsaW5lIG1hcHBpbmdzIGFzIGl0J3MgaW50ZXJuYWwgYW5kIHdlIG5lZWQgdG8gZ2VuZXJhdGUgbGluZSBtYXBwaW5nc1xuICogZm9yIG5vbi1UeXBlU2NyaXB0IGZpbGVzIHN1Y2ggYXMgSFRNTCB0ZW1wbGF0ZXMgb3Igc3R5bGVzaGVldHMuXG4gKlxuICogTGluZSBhbmQgY2hhcmFjdGVyIGNhbiBiZSByZXRyaWV2ZWQgYnkgc3BsaXR0aW5nIGEgZ2l2ZW4gc291cmNlIHRleHQgYmFzZWQgb25cbiAqIGxpbmUgYnJlYWtzIGludG8gbGluZSBzdGFydCBlbnRyaWVzLiBMYXRlciB3aGVuIGEgc3BlY2lmaWMgcG9zaXRpb24gaXMgcmVxdWVzdGVkLFxuICogdGhlIGNsb3Nlc3QgbGluZS1zdGFydCBwb3NpdGlvbiBpcyBkZXRlcm1pbmVkIGJhc2VkIG9uIHRoZSBnaXZlbiBwb3NpdGlvbi5cbiAqL1xuXG5jb25zdCBMRl9DSEFSID0gMTA7XG5jb25zdCBDUl9DSEFSID0gMTM7XG5jb25zdCBMSU5FX1NFUF9DSEFSID0gODIzMjtcbmNvbnN0IFBBUkFHUkFQSF9DSEFSID0gODIzMztcblxuZXhwb3J0IGludGVyZmFjZSBMaW5lQW5kQ2hhcmFjdGVyIHtcbiAgY2hhcmFjdGVyOiBudW1iZXI7XG4gIGxpbmU6IG51bWJlcjtcbn1cblxuLyoqIEdldHMgdGhlIGxpbmUgYW5kIGNoYXJhY3RlciBmb3IgdGhlIGdpdmVuIHBvc2l0aW9uIGZyb20gdGhlIGxpbmUgc3RhcnRzIG1hcC4gKi9cbmV4cG9ydCBmdW5jdGlvbiBnZXRMaW5lQW5kQ2hhcmFjdGVyRnJvbVBvc2l0aW9uKGxpbmVTdGFydHNNYXA6IG51bWJlcltdLCBwb3NpdGlvbjogbnVtYmVyKSB7XG4gIGNvbnN0IGxpbmVJbmRleCA9IGZpbmRDbG9zZXN0TGluZVN0YXJ0UG9zaXRpb24obGluZVN0YXJ0c01hcCwgcG9zaXRpb24pO1xuICByZXR1cm4ge2NoYXJhY3RlcjogcG9zaXRpb24gLSBsaW5lU3RhcnRzTWFwW2xpbmVJbmRleF0sIGxpbmU6IGxpbmVJbmRleH07XG59XG5cbi8qKlxuICogQ29tcHV0ZXMgdGhlIGxpbmUgc3RhcnQgbWFwIG9mIHRoZSBnaXZlbiB0ZXh0LiBUaGlzIGNhbiBiZSB1c2VkIGluIG9yZGVyIHRvXG4gKiByZXRyaWV2ZSB0aGUgbGluZSBhbmQgY2hhcmFjdGVyIG9mIGEgZ2l2ZW4gdGV4dCBwb3NpdGlvbiBpbmRleC5cbiAqL1xuZXhwb3J0IGZ1bmN0aW9uIGNvbXB1dGVMaW5lU3RhcnRzTWFwKHRleHQ6IHN0cmluZyk6IG51bWJlcltdIHtcbiAgY29uc3QgcmVzdWx0OiBudW1iZXJbXSA9IFswXTtcbiAgbGV0IHBvcyA9IDA7XG4gIHdoaWxlIChwb3MgPCB0ZXh0Lmxlbmd0aCkge1xuICAgIGNvbnN0IGNoYXIgPSB0ZXh0LmNoYXJDb2RlQXQocG9zKyspO1xuICAgIC8vIEhhbmRsZXMgdGhlIFwiQ1JMRlwiIGxpbmUgYnJlYWsuIEluIHRoYXQgY2FzZSB3ZSBwZWVrIHRoZSBjaGFyYWN0ZXJcbiAgICAvLyBhZnRlciB0aGUgXCJDUlwiIGFuZCBjaGVjayBpZiBpdCBpcyBhIGxpbmUgZmVlZC5cbiAgICBpZiAoY2hhciA9PT0gQ1JfQ0hBUikge1xuICAgICAgaWYgKHRleHQuY2hhckNvZGVBdChwb3MpID09PSBMRl9DSEFSKSB7XG4gICAgICAgIHBvcysrO1xuICAgICAgfVxuICAgICAgcmVzdWx0LnB1c2gocG9zKTtcbiAgICB9IGVsc2UgaWYgKGNoYXIgPT09IExGX0NIQVIgfHwgY2hhciA9PT0gTElORV9TRVBfQ0hBUiB8fCBjaGFyID09PSBQQVJBR1JBUEhfQ0hBUikge1xuICAgICAgcmVzdWx0LnB1c2gocG9zKTtcbiAgICB9XG4gIH1cbiAgcmVzdWx0LnB1c2gocG9zKTtcbiAgcmV0dXJuIHJlc3VsdDtcbn1cblxuLyoqIEZpbmRzIHRoZSBjbG9zZXN0IGxpbmUgc3RhcnQgZm9yIHRoZSBnaXZlbiBwb3NpdGlvbi4gKi9cbmZ1bmN0aW9uIGZpbmRDbG9zZXN0TGluZVN0YXJ0UG9zaXRpb248VD4oXG4gIGxpbmVzTWFwOiBUW10sXG4gIHBvc2l0aW9uOiBULFxuICBsb3cgPSAwLFxuICBoaWdoID0gbGluZXNNYXAubGVuZ3RoIC0gMSxcbikge1xuICB3aGlsZSAobG93IDw9IGhpZ2gpIHtcbiAgICBjb25zdCBwaXZvdEluZGV4ID0gTWF0aC5mbG9vcigobG93ICsgaGlnaCkgLyAyKTtcbiAgICBjb25zdCBwaXZvdEVsID0gbGluZXNNYXBbcGl2b3RJbmRleF07XG5cbiAgICBpZiAocGl2b3RFbCA9PT0gcG9zaXRpb24pIHtcbiAgICAgIHJldHVybiBwaXZvdEluZGV4O1xuICAgIH0gZWxzZSBpZiAocG9zaXRpb24gPiBwaXZvdEVsKSB7XG4gICAgICBsb3cgPSBwaXZvdEluZGV4ICsgMTtcbiAgICB9IGVsc2Uge1xuICAgICAgaGlnaCA9IHBpdm90SW5kZXggLSAxO1xuICAgIH1cbiAgfVxuXG4gIC8vIEluIGNhc2UgdGhlcmUgd2FzIG5vIGV4YWN0IG1hdGNoLCByZXR1cm4gdGhlIGNsb3Nlc3QgXCJsb3dlclwiIGxpbmUgaW5kZXguIFdlIGFsc29cbiAgLy8gc3VidHJhY3QgdGhlIGluZGV4IGJ5IG9uZSBiZWNhdXNlIHdhbnQgdGhlIGluZGV4IG9mIHRoZSBwcmV2aW91cyBsaW5lIHN0YXJ0LlxuICByZXR1cm4gbG93IC0gMTtcbn1cbiJdfQ==