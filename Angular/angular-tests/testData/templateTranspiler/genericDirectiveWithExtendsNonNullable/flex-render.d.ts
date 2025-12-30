import { DoCheck, Injector, OnChanges, SimpleChanges, TemplateRef, Type, ViewContainerRef } from '@angular/core';
import { FlexRenderFlags } from './flex-render/flags';
import { FlexRenderComponent } from './flex-render/flex-render-component';
import { FlexRenderView } from './flex-render/view';
import * as i0 from "@angular/core";
export { injectFlexRenderContext, type FlexRenderComponentProps, } from './flex-render/context';
export type FlexRenderContent<TProps extends NonNullable<unknown>> = string | number | Type<TProps> | FlexRenderComponent<TProps> | TemplateRef<{
    $implicit: TProps;
}> | null | Record<any, any> | undefined;
export declare class FlexRenderDirective<TProps extends NonNullable<unknown>> implements OnChanges, DoCheck {
    #private;
    private readonly viewContainerRef;
    private readonly templateRef;
    content: number | string | ((props: TProps) => FlexRenderContent<TProps>) | null | undefined;
    props: TProps;
    injector: Injector;
    renderFlags: FlexRenderFlags;
    renderView: FlexRenderView<any> | null;
    constructor(viewContainerRef: ViewContainerRef, templateRef: TemplateRef<any>);
    ngOnChanges(changes: SimpleChanges): void;
    ngDoCheck(): void;
    update(): void;
    render(): void;
    static ɵfac: i0.ɵɵFactoryDeclaration<FlexRenderDirective<any>, never>;
    static ɵdir: i0.ɵɵDirectiveDeclaration<FlexRenderDirective<any>, "[flexRender]", never, { "content": { "alias": "flexRender"; "required": true; }; "props": { "alias": "flexRenderProps"; "required": true; }; "injector": { "alias": "flexRenderInjector"; "required": false; }; }, {}, never, never, true, never>;
}
