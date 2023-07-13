import { TemplateRef, ViewContainerRef, Type } from '@angular/core';
export declare type View = string | TemplateRef<any> | Type<any>;
export declare class TemplateHandler {
    private view;
    private vcr;
    private injector;
    constructor(view: View, vcr: ViewContainerRef);
    attachView(): void;
    detachView(): void;
    private createComponent;
}
