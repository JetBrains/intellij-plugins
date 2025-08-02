import { ChangeDetectorRef, ElementRef, EventEmitter, Injector, NgZone } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import type { SearchbarInputEventDetail, SearchbarChangeEventDetail, Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare class IonSearchbar extends ValueAccessor {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone, injector: Injector);
    handleIonInput(el: HTMLIonSearchbarElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonSearchbar, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonSearchbar, "ion-searchbar", never, { "animated": { "alias": "animated"; "required": false; }; "autocomplete": { "alias": "autocomplete"; "required": false; }; "autocorrect": { "alias": "autocorrect"; "required": false; }; "cancelButtonIcon": { "alias": "cancelButtonIcon"; "required": false; }; "cancelButtonText": { "alias": "cancelButtonText"; "required": false; }; "clearIcon": { "alias": "clearIcon"; "required": false; }; "color": { "alias": "color"; "required": false; }; "debounce": { "alias": "debounce"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "enterkeyhint": { "alias": "enterkeyhint"; "required": false; }; "inputmode": { "alias": "inputmode"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "name": { "alias": "name"; "required": false; }; "placeholder": { "alias": "placeholder"; "required": false; }; "searchIcon": { "alias": "searchIcon"; "required": false; }; "showCancelButton": { "alias": "showCancelButton"; "required": false; }; "showClearButton": { "alias": "showClearButton"; "required": false; }; "spellcheck": { "alias": "spellcheck"; "required": false; }; "type": { "alias": "type"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, {}, never, ["*"], true, never>;
}
export declare interface IonSearchbar extends Components.IonSearchbar {
    /**
     * Emitted when the `value` of the `ion-searchbar` element has changed.
     */
    ionInput: EventEmitter<CustomEvent<SearchbarInputEventDetail>>;
    /**
     * The `ionChange` event is fired for `<ion-searchbar>` elements when the user
  modifies the element's value. Unlike the `ionInput` event, the `ionChange`
  event is not necessarily fired for each alteration to an element's value.
  
  The `ionChange` event is fired when the value has been committed
  by the user. This can happen when the element loses focus or
  when the "Enter" key is pressed. `ionChange` can also fire
  when clicking the clear or cancel buttons.
     */
    ionChange: EventEmitter<CustomEvent<SearchbarChangeEventDetail>>;
    /**
     * Emitted when the cancel button is clicked.
     */
    ionCancel: EventEmitter<CustomEvent<void>>;
    /**
     * Emitted when the clear input button is clicked.
     */
    ionClear: EventEmitter<CustomEvent<void>>;
    /**
     * Emitted when the input loses focus.
     */
    ionBlur: EventEmitter<CustomEvent<void>>;
    /**
     * Emitted when the input has focus.
     */
    ionFocus: EventEmitter<CustomEvent<void>>;
}
