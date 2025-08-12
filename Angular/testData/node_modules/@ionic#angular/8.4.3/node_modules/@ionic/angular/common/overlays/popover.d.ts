import { ChangeDetectorRef, ElementRef, EventEmitter, NgZone, TemplateRef } from '@angular/core';
import type { Components } from '@ionic/core/components';
import * as i0 from "@angular/core";
export declare interface IonPopover extends Components.IonPopover {
    /**
     * Emitted after the popover has presented.
     */
    ionPopoverDidPresent: EventEmitter<CustomEvent>;
    /**
     * Emitted before the popover has presented.
     */
    ionPopoverWillPresent: EventEmitter<CustomEvent>;
    /**
     * Emitted after the popover has dismissed.
     */
    ionPopoverWillDismiss: EventEmitter<CustomEvent>;
    /**
     * Emitted after the popover has dismissed.
     */
    ionPopoverDidDismiss: EventEmitter<CustomEvent>;
    /**
     * Emitted after the popover has presented. Shorthand for ionPopoverDidPresent.
     */
    didPresent: EventEmitter<CustomEvent>;
    /**
     * Emitted before the popover has presented. Shorthand for ionPopoverWillPresent.
     */
    willPresent: EventEmitter<CustomEvent>;
    /**
     * Emitted after the popover has presented. Shorthand for ionPopoverWillDismiss.
     */
    willDismiss: EventEmitter<CustomEvent>;
    /**
     * Emitted after the popover has dismissed. Shorthand for ionPopoverDidDismiss.
     */
    didDismiss: EventEmitter<CustomEvent>;
}
export declare class IonPopover {
    protected z: NgZone;
    template: TemplateRef<any>;
    isCmpOpen: boolean;
    protected el: HTMLElement;
    constructor(c: ChangeDetectorRef, r: ElementRef, z: NgZone);
    static ɵfac: i0.ɵɵFactoryDeclaration<IonPopover, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<IonPopover, "ion-popover", never, { "alignment": { "alias": "alignment"; "required": false; }; "animated": { "alias": "animated"; "required": false; }; "arrow": { "alias": "arrow"; "required": false; }; "keepContentsMounted": { "alias": "keepContentsMounted"; "required": false; }; "backdropDismiss": { "alias": "backdropDismiss"; "required": false; }; "cssClass": { "alias": "cssClass"; "required": false; }; "dismissOnSelect": { "alias": "dismissOnSelect"; "required": false; }; "enterAnimation": { "alias": "enterAnimation"; "required": false; }; "event": { "alias": "event"; "required": false; }; "focusTrap": { "alias": "focusTrap"; "required": false; }; "isOpen": { "alias": "isOpen"; "required": false; }; "keyboardClose": { "alias": "keyboardClose"; "required": false; }; "leaveAnimation": { "alias": "leaveAnimation"; "required": false; }; "mode": { "alias": "mode"; "required": false; }; "showBackdrop": { "alias": "showBackdrop"; "required": false; }; "translucent": { "alias": "translucent"; "required": false; }; "trigger": { "alias": "trigger"; "required": false; }; "triggerAction": { "alias": "triggerAction"; "required": false; }; "reference": { "alias": "reference"; "required": false; }; "size": { "alias": "size"; "required": false; }; "side": { "alias": "side"; "required": false; }; }, {}, ["template"], never, false, never>;
}
