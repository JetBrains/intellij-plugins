import { ChangeDetectorRef, ElementRef, EventEmitter, Injector, NgZone } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import type { RangeChangeEventDetail, RangeKnobMoveStartEventDetail, RangeKnobMoveEndEventDetail, Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare class IonRange extends ValueAccessor {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone, injector: Injector);
    handleIonInput(el: HTMLIonRangeElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonRange, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonRange, "ion-range", never, { "activeBarStart": { "alias": "activeBarStart"; "required": false; }; "color": { "alias": "color"; "required": false; }; "debounce": { "alias": "debounce"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "dualKnobs": { "alias": "dualKnobs"; "required": false; }; "label": { "alias": "label"; "required": false; }; "labelPlacement": { "alias": "labelPlacement"; "required": false; }; "max": { "alias": "max"; "required": false; }; "min": { "alias": "min"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "name": { "alias": "name"; "required": false; }; "pin": { "alias": "pin"; "required": false; }; "pinFormatter": { "alias": "pinFormatter"; "required": false; }; "snaps": { "alias": "snaps"; "required": false; }; "step": { "alias": "step"; "required": false; }; "ticks": { "alias": "ticks"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, {}, never, ["*"], true, never>;
}
export declare interface IonRange extends Components.IonRange {
    /**
     * The `ionChange` event is fired for `<ion-range>` elements when the user
  modifies the element's value:
  - When the user releases the knob after dragging;
  - When the user moves the knob with keyboard arrows
  
  `ionChange` is not fired when the value is changed programmatically.
     */
    ionChange: EventEmitter<CustomEvent<RangeChangeEventDetail>>;
    /**
     * The `ionInput` event is fired for `<ion-range>` elements when the value
  is modified. Unlike `ionChange`, `ionInput` is fired continuously
  while the user is dragging the knob.
     */
    ionInput: EventEmitter<CustomEvent<RangeChangeEventDetail>>;
    /**
     * Emitted when the range has focus.
     */
    ionFocus: EventEmitter<CustomEvent<void>>;
    /**
     * Emitted when the range loses focus.
     */
    ionBlur: EventEmitter<CustomEvent<void>>;
    /**
     * Emitted when the user starts moving the range knob, whether through
  mouse drag, touch gesture, or keyboard interaction.
     */
    ionKnobMoveStart: EventEmitter<CustomEvent<RangeKnobMoveStartEventDetail>>;
    /**
     * Emitted when the user finishes moving the range knob, whether through
  mouse drag, touch gesture, or keyboard interaction.
     */
    ionKnobMoveEnd: EventEmitter<CustomEvent<RangeKnobMoveEndEventDetail>>;
}
