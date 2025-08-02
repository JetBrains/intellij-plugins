import { ChangeDetectorRef, ElementRef, EventEmitter, Injector, NgZone } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import type { TextareaChangeEventDetail, TextareaInputEventDetail, Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare class IonTextarea extends ValueAccessor {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone, injector: Injector);
    handleIonInput(el: HTMLIonTextareaElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonTextarea, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonTextarea, "ion-textarea", never, { "autoGrow": { "alias": "autoGrow"; "required": false; }; "autocapitalize": { "alias": "autocapitalize"; "required": false; }; "autofocus": { "alias": "autofocus"; "required": false; }; "clearOnEdit": { "alias": "clearOnEdit"; "required": false; }; "color": { "alias": "color"; "required": false; }; "cols": { "alias": "cols"; "required": false; }; "counter": { "alias": "counter"; "required": false; }; "counterFormatter": { "alias": "counterFormatter"; "required": false; }; "debounce": { "alias": "debounce"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "enterkeyhint": { "alias": "enterkeyhint"; "required": false; }; "errorText": { "alias": "errorText"; "required": false; }; "fill": { "alias": "fill"; "required": false; }; "helperText": { "alias": "helperText"; "required": false; }; "inputmode": { "alias": "inputmode"; "required": false; }; "label": { "alias": "label"; "required": false; }; "labelPlacement": { "alias": "labelPlacement"; "required": false; }; "maxlength": { "alias": "maxlength"; "required": false; }; "minlength": { "alias": "minlength"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "name": { "alias": "name"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "readonly": { "alias": "readonly"; "required": false; }; "required": { "alias": "required"; "required": false; }; "rows": { "alias": "rows"; "required": false; }; "shape": { "alias": "shape"; "required": false; }; "spellcheck": { "alias": "spellcheck"; "required": false; }; "value": { "alias": "value"; "required": false; }; "wrap": { "alias": "wrap"; "required": false; }; }, {}, never, ["*"], true, never>;
}
export declare interface IonTextarea extends Components.IonTextarea {
    /**
     * The `ionChange` event is fired when the user modifies the textarea's value.
  Unlike the `ionInput` event, the `ionChange` event is fired when
  the element loses focus after its value has been modified.
     */
    ionChange: EventEmitter<CustomEvent<TextareaChangeEventDetail>>;
    /**
     * The `ionInput` event is fired each time the user modifies the textarea's value.
  Unlike the `ionChange` event, the `ionInput` event is fired for each alteration
  to the textarea's value. This typically happens for each keystroke as the user types.
  
  When `clearOnEdit` is enabled, the `ionInput` event will be fired when
  the user clears the textarea by performing a keydown event.
     */
    ionInput: EventEmitter<CustomEvent<TextareaInputEventDetail>>;
    /**
     * Emitted when the input loses focus.
     */
    ionBlur: EventEmitter<CustomEvent<FocusEvent>>;
    /**
     * Emitted when the input has focus.
     */
    ionFocus: EventEmitter<CustomEvent<FocusEvent>>;
}
