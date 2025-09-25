import * as i0 from '@angular/core';
import { OnDestroy, ElementRef, OnInit, EventEmitter, AfterViewInit, DoCheck } from '@angular/core';
import { Observable } from 'rxjs';
import { NumberInput } from '../number-property.d-BzBQchZ2.js';

/** An event that is emitted when the autofill state of an input changes. */
type AutofillEvent = {
    /** The element whose autofill state changes. */
    target: Element;
    /** Whether the element is currently autofilled. */
    isAutofilled: boolean;
};
/**
 * An injectable service that can be used to monitor the autofill state of an input.
 * Based on the following blog post:
 * https://medium.com/@brunn/detecting-autofilled-fields-in-javascript-aed598d25da7
 */
declare class AutofillMonitor implements OnDestroy {
    private _platform;
    private _ngZone;
    private _renderer;
    private _styleLoader;
    private _monitoredElements;
    constructor(...args: unknown[]);
    /**
     * Monitor for changes in the autofill state of the given input element.
     * @param element The element to monitor.
     * @return A stream of autofill state changes.
     */
    monitor(element: Element): Observable<AutofillEvent>;
    /**
     * Monitor for changes in the autofill state of the given input element.
     * @param element The element to monitor.
     * @return A stream of autofill state changes.
     */
    monitor(element: ElementRef<Element>): Observable<AutofillEvent>;
    /**
     * Stop monitoring the autofill state of the given input element.
     * @param element The element to stop monitoring.
     */
    stopMonitoring(element: Element): void;
    /**
     * Stop monitoring the autofill state of the given input element.
     * @param element The element to stop monitoring.
     */
    stopMonitoring(element: ElementRef<Element>): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<AutofillMonitor, never>;
    static ɵprov: i0.ɵɵInjectableDeclaration<AutofillMonitor>;
}
/** A directive that can be used to monitor the autofill state of an input. */
declare class CdkAutofill implements OnDestroy, OnInit {
    private _elementRef;
    private _autofillMonitor;
    /** Emits when the autofill state of the element changes. */
    readonly cdkAutofill: EventEmitter<AutofillEvent>;
    constructor(...args: unknown[]);
    ngOnInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAutofill, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAutofill, "[cdkAutofill]", never, {}, { "cdkAutofill": "cdkAutofill"; }, never, never, true, never>;
}

/** Directive to automatically resize a textarea to fit its content. */
declare class CdkTextareaAutosize implements AfterViewInit, DoCheck, OnDestroy {
    private _elementRef;
    private _platform;
    private _ngZone;
    private _renderer;
    private _resizeEvents;
    /** Keep track of the previous textarea value to avoid resizing when the value hasn't changed. */
    private _previousValue?;
    private _initialHeight;
    private readonly _destroyed;
    private _listenerCleanups;
    private _minRows;
    private _maxRows;
    private _enabled;
    /**
     * Value of minRows as of last resize. If the minRows has decreased, the
     * height of the textarea needs to be recomputed to reflect the new minimum. The maxHeight
     * does not have the same problem because it does not affect the textarea's scrollHeight.
     */
    private _previousMinRows;
    private _textareaElement;
    /** Minimum amount of rows in the textarea. */
    get minRows(): number;
    set minRows(value: NumberInput);
    /** Maximum amount of rows in the textarea. */
    get maxRows(): number;
    set maxRows(value: NumberInput);
    /** Whether autosizing is enabled or not */
    get enabled(): boolean;
    set enabled(value: boolean);
    get placeholder(): string;
    set placeholder(value: string);
    /** Cached height of a textarea with a single row. */
    private _cachedLineHeight?;
    /** Cached height of a textarea with only the placeholder. */
    private _cachedPlaceholderHeight?;
    /** Cached scroll top of a textarea */
    private _cachedScrollTop;
    /** Used to reference correct document/window */
    protected _document?: Document | null | undefined;
    private _hasFocus;
    private _isViewInited;
    constructor(...args: unknown[]);
    /** Sets the minimum height of the textarea as determined by minRows. */
    _setMinHeight(): void;
    /** Sets the maximum height of the textarea as determined by maxRows. */
    _setMaxHeight(): void;
    ngAfterViewInit(): void;
    ngOnDestroy(): void;
    /**
     * Cache the height of a single-row textarea if it has not already been cached.
     *
     * We need to know how large a single "row" of a textarea is in order to apply minRows and
     * maxRows. For the initial version, we will assume that the height of a single line in the
     * textarea does not ever change.
     */
    private _cacheTextareaLineHeight;
    private _measureScrollHeight;
    private _cacheTextareaPlaceholderHeight;
    /** Handles `focus` and `blur` events. */
    private _handleFocusEvent;
    ngDoCheck(): void;
    /**
     * Resize the textarea to fit its content.
     * @param force Whether to force a height recalculation. By default the height will be
     *    recalculated only if the value changed since the last call.
     */
    resizeToFitContent(force?: boolean): void;
    /**
     * Resets the textarea to its original size
     */
    reset(): void;
    _noopInputHandler(): void;
    /**
     * Scrolls a textarea to the caret position. On Firefox resizing the textarea will
     * prevent it from scrolling to the caret position. We need to re-set the selection
     * in order for it to scroll to the proper position.
     */
    private _scrollToCaretPosition;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTextareaAutosize, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTextareaAutosize, "textarea[cdkTextareaAutosize]", ["cdkTextareaAutosize"], { "minRows": { "alias": "cdkAutosizeMinRows"; "required": false; }; "maxRows": { "alias": "cdkAutosizeMaxRows"; "required": false; }; "enabled": { "alias": "cdkTextareaAutosize"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; }, {}, never, never, true, never>;
    static ngAcceptInputType_enabled: unknown;
}

declare class TextFieldModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<TextFieldModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<TextFieldModule, never, [typeof CdkAutofill, typeof CdkTextareaAutosize], [typeof CdkAutofill, typeof CdkTextareaAutosize]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<TextFieldModule>;
}

export { AutofillMonitor, CdkAutofill, CdkTextareaAutosize, TextFieldModule };
export type { AutofillEvent };
