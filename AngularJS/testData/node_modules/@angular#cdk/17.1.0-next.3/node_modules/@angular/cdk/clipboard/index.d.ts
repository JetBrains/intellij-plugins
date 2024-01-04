import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import { InjectionToken } from '@angular/core';
import { NgZone } from '@angular/core';
import { OnDestroy } from '@angular/core';

/** Injection token that can be used to provide the default options to `CdkCopyToClipboard`. */
export declare const CDK_COPY_TO_CLIPBOARD_CONFIG: InjectionToken<CdkCopyToClipboardConfig>;

/**
 * Provides behavior for a button that when clicked copies content into user's
 * clipboard.
 */
export declare class CdkCopyToClipboard implements OnDestroy {
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
    constructor(_clipboard: Clipboard_2, _ngZone: NgZone, config?: CdkCopyToClipboardConfig);
    /** Copies the current text to the clipboard. */
    copy(attempts?: number): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkCopyToClipboard, [null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkCopyToClipboard, "[cdkCopyToClipboard]", never, { "text": { "alias": "cdkCopyToClipboard"; "required": false; }; "attempts": { "alias": "cdkCopyToClipboardAttempts"; "required": false; }; }, { "copied": "cdkCopyToClipboardCopied"; }, never, never, true, never>;
}

/** Object that can be used to configure the default options for `CdkCopyToClipboard`. */
export declare interface CdkCopyToClipboardConfig {
    /** Default number of attempts to make when copying text to the clipboard. */
    attempts?: number;
}

/**
 * A service for copying text to the clipboard.
 */
declare class Clipboard_2 {
    private readonly _document;
    constructor(document: any);
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
    static ɵfac: i0.ɵɵFactoryDeclaration<Clipboard_2, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<Clipboard_2>;
}
export { Clipboard_2 as Clipboard }

export declare class ClipboardModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<ClipboardModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<ClipboardModule, never, [typeof i1.CdkCopyToClipboard], [typeof i1.CdkCopyToClipboard]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<ClipboardModule>;
}

declare namespace i1 {
    export {
        CdkCopyToClipboardConfig,
        CDK_COPY_TO_CLIPBOARD_CONFIG,
        CdkCopyToClipboard
    }
}


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
export declare class PendingCopy {
    private readonly _document;
    private _textarea;
    constructor(text: string, _document: Document);
    /** Finishes copying the text. */
    copy(): boolean;
    /** Cleans up DOM changes used to perform the copy operation. */
    destroy(): void;
}

export { }
