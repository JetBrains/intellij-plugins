import { ChangeDetectorRef, ElementRef, EventEmitter, Injector, NgZone } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import type { InputInputEventDetail as IIonInputInputInputEventDetail, InputChangeEventDetail as IIonInputInputChangeEventDetail, Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare class IonInput extends ValueAccessor {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone, injector: Injector);
    handleIonInput(el: HTMLIonInputElement): void;
    registerOnChange(fn: (_: any) => void): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonInput, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonInput, "ion-input", never, { "accept": { "alias": "accept"; "required": false; }; "autocapitalize": { "alias": "autocapitalize"; "required": false; }; "autocomplete": { "alias": "autocomplete"; "required": false; }; "autocorrect": { "alias": "autocorrect"; "required": false; }; "autofocus": { "alias": "autofocus"; "required": false; }; "clearInput": { "alias": "clearInput"; "required": false; }; "clearOnEdit": { "alias": "clearOnEdit"; "required": false; }; "color": { "alias": "color"; "required": false; }; "counter": { "alias": "counter"; "required": false; }; "counterFormatter": { "alias": "counterFormatter"; "required": false; }; "debounce": { "alias": "debounce"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "enterkeyhint": { "alias": "enterkeyhint"; "required": false; }; "errorText": { "alias": "errorText"; "required": false; }; "fill": { "alias": "fill"; "required": false; }; "helperText": { "alias": "helperText"; "required": false; }; "inputmode": { "alias": "inputmode"; "required": false; }; "label": { "alias": "label"; "required": false; }; "labelPlacement": { "alias": "labelPlacement"; "required": false; }; "max": { "alias": "max"; "required": false; }; "maxlength": { "alias": "maxlength"; "required": false; }; "min": { "alias": "min"; "required": false; }; "minlength": { "alias": "minlength"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "multiple": { "alias": "multiple"; "required": false; }; "name": { "alias": "name"; "required": false; }; "pattern": { "alias": "pattern"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "readonly": { "alias": "readonly"; "required": false; }; "required": { "alias": "required"; "required": false; }; "shape": { "alias": "shape"; "required": false; }; "size": { "alias": "size"; "required": false; }; "spellcheck": { "alias": "spellcheck"; "required": false; }; "step": { "alias": "step"; "required": false; }; "type": { "alias": "type"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, {}, never, ["*"], true, never>;
}
export declare interface IonInput extends Components.IonInput {
    /**
     * The `ionInput` event is fired each time the user modifies the input's value.
  Unlike the `ionChange` event, the `ionInput` event is fired for each alteration
  to the input's value. This typically happens for each keystroke as the user types.
  
  For elements that accept text input (`type=text`, `type=tel`, etc.), the interface
  is [`InputEvent`](https://developer.mozilla.org/en-US/docs/Web/API/InputEvent); for others,
  the interface is [`Event`](https://developer.mozilla.org/en-US/docs/Web/API/Event). If
  the input is cleared on edit, the type is `null`.
     */
    ionInput: EventEmitter<CustomEvent<IIonInputInputInputEventDetail>>;
    /**
     * The `ionChange` event is fired when the user modifies the input's value.
  Unlike the `ionInput` event, the `ionChange` event is only fired when changes
  are committed, not as the user types.
  
  Depending on the way the users interacts with the element, the `ionChange`
  event fires at a different moment:
  - When the user commits the change explicitly (e.g. by selecting a date
  from a date picker for `<ion-input type="date">`, pressing the "Enter" key, etc.).
  - When the element loses focus after its value has changed: for elements
  where the user's interaction is typing.
     */
    ionChange: EventEmitter<CustomEvent<IIonInputInputChangeEventDetail>>;
    /**
     * Emitted when the input loses focus.
     */
    ionBlur: EventEmitter<CustomEvent<FocusEvent>>;
    /**
     * Emitted when the input has focus.
     */
    ionFocus: EventEmitter<CustomEvent<FocusEvent>>;
}
