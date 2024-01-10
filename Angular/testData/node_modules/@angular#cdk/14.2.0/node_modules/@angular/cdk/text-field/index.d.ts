import { AfterViewInit } from '@angular/core';
import { BooleanInput } from '@angular/cdk/coercion';
import { DoCheck } from '@angular/core';
import { ElementRef } from '@angular/core';
import { EventEmitter } from '@angular/core';
import * as i0 from '@angular/core';
import { NgZone } from '@angular/core';
import { NumberInput } from '@angular/cdk/coercion';
import { Observable } from 'rxjs';
import { OnDestroy } from '@angular/core';
import { OnInit } from '@angular/core';
import { Platform } from '@angular/cdk/platform';

/** An event that is emitted when the autofill state of an input changes. */
export declare type AutofillEvent = {
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
export declare class AutofillMonitor implements OnDestroy {
    private _platform;
    private _ngZone;
    private _monitoredElements;
    constructor(_platform: Platform, _ngZone: NgZone);
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
export declare class CdkAutofill implements OnDestroy, OnInit {
    private _elementRef;
    private _autofillMonitor;
    /** Emits when the autofill state of the element changes. */
    readonly cdkAutofill: EventEmitter<AutofillEvent>;
    constructor(_elementRef: ElementRef<HTMLElement>, _autofillMonitor: AutofillMonitor);
    ngOnInit(): void;
    ngOnDestroy(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkAutofill, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkAutofill, "[cdkAutofill]", never, {}, { "cdkAutofill": "cdkAutofill"; }, never, never, false>;
}

/** Directive to automatically resize a textarea to fit its content. */
export declare class CdkTextareaAutosize implements AfterViewInit, DoCheck, OnDestroy {
    private _elementRef;
    private _platform;
    private _ngZone;
    /** Keep track of the previous textarea value to avoid resizing when the value hasn't changed. */
    private _previousValue?;
    private _initialHeight;
    private readonly _destroyed;
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
    set enabled(value: BooleanInput);
    get placeholder(): string;
    set placeholder(value: string);
    /** Cached height of a textarea with a single row. */
    private _cachedLineHeight;
    /** Cached height of a textarea with only the placeholder. */
    private _cachedPlaceholderHeight?;
    /** Used to reference correct document/window */
    protected _document?: Document;
    private _hasFocus;
    private _isViewInited;
    constructor(_elementRef: ElementRef<HTMLElement>, _platform: Platform, _ngZone: NgZone, 
    /** @breaking-change 11.0.0 make document required */
    document?: any);
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
    /** Access injected document if available or fallback to global document reference */
    private _getDocument;
    /** Use defaultView of injected document if available or fallback to global window reference */
    private _getWindow;
    /**
     * Scrolls a textarea to the caret position. On Firefox resizing the textarea will
     * prevent it from scrolling to the caret position. We need to re-set the selection
     * in order for it to scroll to the proper position.
     */
    private _scrollToCaretPosition;
    static ɵfac: i0.ɵɵFactoryDeclaration<CdkTextareaAutosize, [null, null, null, { optional: true; }]>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<CdkTextareaAutosize, "textarea[cdkTextareaAutosize]", ["cdkTextareaAutosize"], { "minRows": "cdkAutosizeMinRows"; "maxRows": "cdkAutosizeMaxRows"; "enabled": "cdkTextareaAutosize"; "placeholder": "placeholder"; }, {}, never, never, false>;
}

declare namespace i1 {
    export {
        AutofillEvent,
        AutofillMonitor,
        CdkAutofill
    }
}

declare namespace i2 {
    export {
        CdkTextareaAutosize
    }
}

export declare class TextFieldModule {
    static ɵfac: i0.ɵɵFactoryDeclaration<TextFieldModule, never>;
    static ɵmod: i0.ɵɵNgModuleDeclaration<TextFieldModule, [typeof i1.CdkAutofill, typeof i2.CdkTextareaAutosize], never, [typeof i1.CdkAutofill, typeof i2.CdkTextareaAutosize]>;
    static ɵinj: i0.ɵɵInjectorDeclaration<TextFieldModule>;
}

export { }
