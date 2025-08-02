import { ElementRef, Injector, EnvironmentInjector, NgZone, ChangeDetectorRef, EventEmitter } from '@angular/core';
import type { Components } from '@ionic/core';
import { AngularDelegate } from '../../providers/angular-delegate';
import * as i0 from "@angular/core";
export declare interface IonNav extends Components.IonNav {
    /**
     * Event fired when the nav will change components
     */
    ionNavWillChange: EventEmitter<CustomEvent<void>>;
    /**
     * Event fired when the nav has changed components
     */
    ionNavDidChange: EventEmitter<CustomEvent<void>>;
}
export declare class IonNav {
    protected z: NgZone;
    protected el: HTMLElement;
    constructor(ref: ElementRef, environmentInjector: EnvironmentInjector, injector: Injector, angularDelegate: AngularDelegate, z: NgZone, c: ChangeDetectorRef);
    static ɵfac: i0.ɵɵFactoryDeclaration<IonNav, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<IonNav, never, never, { "animated": { "alias": "animated"; "required": false; }; "animation": { "alias": "animation"; "required": false; }; "root": { "alias": "root"; "required": false; }; "rootParams": { "alias": "rootParams"; "required": false; }; "swipeGesture": { "alias": "swipeGesture"; "required": false; }; }, {}, never, never, false, never>;
}
