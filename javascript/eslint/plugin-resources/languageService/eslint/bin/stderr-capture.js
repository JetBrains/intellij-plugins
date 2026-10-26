"use strict";
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.StderrCapture = void 0;
var activeCaptures = new Set();
var hookInstalled = false;
/** Node.js and Bun print a process warning, such as a deprecation, with this prefix. */
var RUNTIME_WARNING_PREFIX = "(node:";
/** `js-language-service.ts` prints this heartbeat once a minute. */
var HEARTBEAT_PATTERN = /^Process \S+ heartbeat: "alive"\s*$/;
/** A rule that logs on every node visit must not grow the response without a bound. The first lines hold the failure. */
var MAX_CAPTURED_LENGTH = 64 * 1024;
/**
 * Collects the text that the service process writes to stderr while it handles a request.
 *
 * A rule can catch its own failure and only log it. ESLint then returns a normal result, and the CLI user sees
 * the failure in the terminal only. The IDE shows the collected text as a file-level ESLint error (WEB-78300).
 *
 * The hook on `process.stderr.write` is installed once and forwards every chunk to the original stream.
 * Concurrent requests share the hook, and each active capture receives every chunk.
 */
var StderrCapture = /** @class */ (function () {
    function StderrCapture() {
        this.chunks = [];
        this.capturedLength = 0;
        this.truncated = false;
    }
    StderrCapture.start = function () {
        installHookOnce();
        var capture = new StderrCapture();
        activeCaptures.add(capture);
        return capture;
    };
    StderrCapture.prototype.append = function (text) {
        if (isIgnoredChunk(text)) {
            return;
        }
        var remaining = MAX_CAPTURED_LENGTH - this.capturedLength;
        if (text.length > remaining) {
            this.chunks.push(text.substring(0, remaining));
            this.capturedLength = MAX_CAPTURED_LENGTH;
            this.truncated = true;
            return;
        }
        this.chunks.push(text);
        this.capturedLength += text.length;
    };
    /**
     * Stops the capture.
     * @returns the collected text without the process warnings of the runtime and without the heartbeat of the service
     */
    StderrCapture.prototype.stop = function () {
        activeCaptures.delete(this);
        return { text: this.chunks.join("").trim(), truncated: this.truncated };
    };
    return StderrCapture;
}());
exports.StderrCapture = StderrCapture;
function installHookOnce() {
    if (hookInstalled) {
        return;
    }
    hookInstalled = true;
    var originalWrite = process.stderr.write.bind(process.stderr);
    process.stderr.write = (function (chunk) {
        var args = [];
        for (var _i = 1; _i < arguments.length; _i++) {
            args[_i - 1] = arguments[_i];
        }
        if (activeCaptures.size > 0) {
            var text_1 = toText(chunk);
            if (text_1 != null) {
                activeCaptures.forEach(function (capture) { return capture.append(text_1); });
            }
        }
        return originalWrite.apply(void 0, __spreadArray([chunk], args, false));
    });
}
/** The stream accepts a string or bytes. Any other chunk is left to the original `write`, which rejects it. */
function toText(chunk) {
    if (typeof chunk === "string") {
        return chunk;
    }
    if (chunk instanceof Uint8Array) {
        return Buffer.from(chunk).toString();
    }
    return null;
}
function isIgnoredChunk(chunk) {
    return chunk.startsWith(RUNTIME_WARNING_PREFIX) || HEARTBEAT_PATTERN.test(chunk);
}
//# sourceMappingURL=stderr-capture.js.map