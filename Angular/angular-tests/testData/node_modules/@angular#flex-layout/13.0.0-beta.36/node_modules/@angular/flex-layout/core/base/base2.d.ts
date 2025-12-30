/**
 * @license
 * Copyright Google LLC All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { ElementRef, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { StyleDefinition, StyleUtils } from '../style-utils/style-utils';
import { StyleBuilder } from '../style-builder/style-builder';
import { MediaMarshaller } from '../media-marshaller/media-marshaller';
import * as i0 from "@angular/core";
export declare abstract class BaseDirective2 implements OnChanges, OnDestroy {
    protected elementRef: ElementRef;
    protected styleBuilder: StyleBuilder;
    protected styler: StyleUtils;
    protected marshal: MediaMarshaller;
    protected DIRECTIVE_KEY: string;
    protected inputs: string[];
    /** The most recently used styles for the builder */
    protected mru: StyleDefinition;
    protected destroySubject: Subject<void>;
    protected currentValue: any;
    /** Access to host element's parent DOM node */
    protected get parentElement(): HTMLElement | null;
    /** Access to the HTMLElement for the directive */
    protected get nativeElement(): HTMLElement;
    /** Access to the activated value for the directive */
    get activatedValue(): string;
    set activatedValue(value: string);
    /** Cache map for style computation */
    protected styleCache: Map<string, StyleDefinition>;
    protected constructor(elementRef: ElementRef, styleBuilder: StyleBuilder, styler: StyleUtils, marshal: MediaMarshaller);
    /** For @Input changes */
    ngOnChanges(changes: SimpleChanges): void;
    ngOnDestroy(): void;
    /** Register with central marshaller service */
    protected init(extraTriggers?: Observable<any>[]): void;
    /** Add styles to the element using predefined style builder */
    protected addStyles(input: string, parent?: Object): void;
    /** Remove generated styles from an element using predefined style builder */
    protected clearStyles(): void;
    /** Force trigger style updates on DOM element */
    protected triggerUpdate(): void;
    /**
     * Determine the DOM element's Flexbox flow (flex-direction).
     *
     * Check inline style first then check computed (stylesheet) style.
     * And optionally add the flow value to element's inline style.
     */
    protected getFlexFlowDirection(target: HTMLElement, addIfMissing?: boolean): string;
    protected hasWrap(target: HTMLElement): boolean;
    /** Applies styles given via string pair or object map to the directive element */
    protected applyStyleToElement(style: StyleDefinition, value?: string | number, element?: HTMLElement): void;
    protected setValue(val: any, bp: string): void;
    protected updateWithValue(input: string): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<BaseDirective2, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<BaseDirective2, never, never, {}, {}, never>;
}
