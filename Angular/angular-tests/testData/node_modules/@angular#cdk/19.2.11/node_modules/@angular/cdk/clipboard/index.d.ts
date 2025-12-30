import * as i0 from '@angular/core';
import { OnDestroy, EventEmitter, InjectionToken } from '@angular/core';

/**
 * A pending copy-to-clipboard operation.
 *
 * The implementation of copying text to the clipboard modifies the DOM and
 * forces a re-layout. This re-layout can take too long if the string is large,
 * causing the execCommand('copy') to happen too long after the user clicked.
 * This results in the browser refusing to copy. This object lets the
 * re-layout happen in a separate tick from copying by providing a copy function
 * that can be called later.
 *
 * Destroy must be called when no longer in use, regardless of whether `copy` is
 * called.
 */
declare class PendingCopy {
    private readonly _document;
    private _textarea;
    constructor(text: string, _document: Document);
    /** Finishes copying the text. */
    copy(): boolean;
    /** Cleans up DOM changes used to perform the copy operation. */
    destroy(): void;
}

/**
 * A service for copying text to the clipboard.
 */
declare class Clipboard {
    private readonly _document;
    constructor(...args: unknown[]);
    /**
     * Copies the provided text into the user's clipboard.
     *
     * @param text The string to copy.
     * @returns Whether the operation was successful.
     */
    copy(text: string): boolean;
    /**
     * Prepares a string to be copied later. This is useful for large strings
     * which take too long to successfully render and be copied in the same tick.
     *
     * The caller must call `destroy` on the returned `PendingCopy`.
     *
     * @param text The string to copy.
     * @returns the pending copy operation.
     */
    beginCopy(text: string): PendingCopy;
    static ɵfac: i0.ɵɵFactoryDeclaration<Clipboard, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Clipboard>;
}

/** Object that can be used to configure the default options for `CdkCopyToClipboard`. */
interface CdkCopyToClipboardConfig {
    /** Default number of attempts to make when copying text to the clipboard. */
    attempts?: number;
}
/** Injection token that can be used to provide the default options to `CdkCopyToClipboard`. */
declare const CDK_COPY_TO_CLIPBOARD_CONFIG: InjectionToken<CdkCopyToClipboardConfig>;
/**
 * Provides behavior for a button that when clicked copies content into user's
 * clipboard.
 */
declare class CdkCopyToClipboard implements OnDestroy {
    private _clipboard;
    private _ngZone;
    /** Content to be copied. */
    text: string;
    /**
     * How many times to attempt to copy the text. This may be necessary for longer text, because
     * the browser needs time to fill an intermediate textarea element and copy the content.
     */
    attempts: number;
    /**
     * Emits when some text is copied to the clipboard. The
     * emitted value indicates whether copying was successful.
     */
    readonly copied: EventEmitter<boolean>;
    /** Copies that are currently being attempted. */
    private _pending;
    /** Whether the directive has been destroyed. */
    private _destroyed;
    /** Timeout for the current copy attempt. */
    private _currentTimeout;
    constructor(...args: unknown[]);
    /** Copies the current text to the clipboard. */
    copy(attempts?: number): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkCopyToClipboard, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkCopyToClipboard, "[cdkCopyToClipboard]", never, { "text": { "alias": "cdkCopyToClipboard"; "required": false; }; "attempts": { "alias": "cdkCopyToClipboardAttempts"; "required": false; }; }, { "copied": "cdkCopyToClipboardCopied"; }, never, never, true, never>;
}

declare class ClipboardModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<ClipboardModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<ClipboardModule, never, [typeof CdkCopyToClipboard], [typeof CdkCopyToClipboard]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<ClipboardModule>;
}

export { CDK_COPY_TO_CLIPBOARD_CONFIG, CdkCopyToClipboard, Clipboard, ClipboardModule, PendingCopy };
export type { CdkCopyToClipboardConfig };
