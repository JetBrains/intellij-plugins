import { ComponentFactoryResolver, ElementRef, Injector, ViewContainerRef } from '@angular/core';
import { AngularDelegate } from '../../providers/angular-delegate';
export declare class NavDelegate {
    constructor(ref: ElementRef, resolver: ComponentFactoryResolver, injector: Injector, angularDelegate: AngularDelegate, location: ViewContainerRef);
}
