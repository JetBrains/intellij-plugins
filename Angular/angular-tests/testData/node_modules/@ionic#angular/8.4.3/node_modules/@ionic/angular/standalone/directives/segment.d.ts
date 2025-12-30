import { ChangeDetectorRef, ElementRef, EventEmitter, Injector, NgZone } from '@angular/core';
import { ValueAccessor } from '@ionic/angular/common';
import type { SegmentChangeEventDetail, Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare class IonSegment extends ValueAccessor {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone, injector: Injector);
    handleIonChange(el: HTMLIonSegmentElement): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<IonSegment, never>;
    static ɵcmp: i0.ɵɵComponentDeclaration<IonSegment, "ion-segment", never, { "color": { "alias": "color"; "required": false; }; "disabled": { "alias": "disabled"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "scrollable": { "alias": "scrollable"; "required": false; }; "selectOnFocus": { "alias": "selectOnFocus"; "required": false; }; "swipeGesture": { "alias": "swipeGesture"; "required": false; }; "value": { "alias": "value"; "required": false; }; }, {}, never, ["*"], true, never>;
}
export declare interface IonSegment extends Components.IonSegment {
    /**
     * Emitted when the value property has changed and any
  dragging pointer has been released from `ion-segment`.
     */
    ionChange: EventEmitter<CustomEvent<SegmentChangeEventDetail>>;
}
