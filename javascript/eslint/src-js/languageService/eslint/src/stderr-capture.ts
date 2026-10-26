const activeCaptures = new Set<StderrCapture>();
let hookInstalled = false;

/** Node.js and Bun print a process warning, such as a deprecation, with this prefix. */
const RUNTIME_WARNING_PREFIX = "(node:";
/** A rule that logs on every node visit must not grow the response without a bound. The first lines hold the failure. */
const MAX_CAPTURED_LENGTH = 64 * 1024;

export interface CapturedStderr {
  /** The collected text. Empty when the process wrote nothing. */
  readonly text: string;
  /** True when the text lost its tail to the cap. */
  readonly truncated: boolean;
}

/**
 * Collects the text that the service process writes to stderr while it handles a request.
 *
 * A rule can catch its own failure and only log it. ESLint then returns a normal result, and the CLI user sees
 * the failure in the terminal only. The IDE shows the collected text as a file-level ESLint error (WEB-78300).
 *
 * The hook on `process.stderr.write` is installed once and forwards every chunk to the original stream.
 * Concurrent requests share the hook, and each active capture receives every chunk.
 */
export class StderrCapture {
  private readonly chunks: string[] = [];
  private capturedLength = 0;
  private truncated = false;

  private constructor() {
  }

  static start(): StderrCapture {
    installHookOnce();
    const capture = new StderrCapture();
    activeCaptures.add(capture);
    return capture;
  }

  append(text: string): void {
    if (isIgnoredChunk(text)) {
      return;
    }
    const remaining = MAX_CAPTURED_LENGTH - this.capturedLength;
    if (text.length > remaining) {
      this.chunks.push(text.substring(0, remaining));
      this.capturedLength = MAX_CAPTURED_LENGTH;
      this.truncated = true;
      return;
    }
    this.chunks.push(text);
    this.capturedLength += text.length;
  }

  /**
   * Stops the capture.
   * @returns the collected text without the process warnings of the runtime
   */
  stop(): CapturedStderr {
    activeCaptures.delete(this);
    return {text: this.chunks.join("").trim(), truncated: this.truncated};
  }
}

function installHookOnce(): void {
  if (hookInstalled) {
    return;
  }
  hookInstalled = true;
  const originalWrite = process.stderr.write.bind(process.stderr) as (...args: any[]) => boolean;
  process.stderr.write = ((chunk: unknown, ...args: any[]): boolean => {
    if (activeCaptures.size > 0) {
      const text = toText(chunk);
      if (text != null) {
        activeCaptures.forEach(capture => capture.append(text));
      }
    }
    return originalWrite(chunk, ...args);
  }) as typeof process.stderr.write;
}

/** The stream accepts a string or bytes. Any other chunk is left to the original `write`, which rejects it. */
function toText(chunk: unknown): string | null {
  if (typeof chunk === "string") {
    return chunk;
  }
  if (chunk instanceof Uint8Array) {
    return Buffer.from(chunk).toString();
  }
  return null;
}

function isIgnoredChunk(chunk: string): boolean {
  return chunk.startsWith(RUNTIME_WARNING_PREFIX);
}
