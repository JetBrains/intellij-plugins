import { FlexRenderComponentRef } from './flex-render-component-ref';
import { EmbeddedViewRef, TemplateRef, Type } from '@angular/core';
import type { FlexRenderContent } from '../flex-render';
import { FlexRenderComponent } from './flex-render-component';
export type FlexRenderTypedContent = {
    kind: 'null';
} | {
    kind: 'primitive';
    content: string | number | Record<string, any>;
} | {
    kind: 'flexRenderComponent';
    content: FlexRenderComponent<unknown>;
} | {
    kind: 'templateRef';
    content: TemplateRef<unknown>;
} | {
    kind: 'component';
    content: Type<unknown>;
};
export declare function mapToFlexRenderTypedContent(content: FlexRenderContent<any>): FlexRenderTypedContent;
export declare abstract class FlexRenderView<TView extends FlexRenderComponentRef<any> | EmbeddedViewRef<unknown> | null> {
    #private;
    readonly view: TView;
    protected constructor(initialContent: Exclude<FlexRenderTypedContent, {
        kind: 'null';
    }>, view: TView);
    get previousContent(): FlexRenderTypedContent;
    get content(): FlexRenderTypedContent;
    set content(content: FlexRenderTypedContent);
    abstract updateProps(props: Record<string, any>): void;
    abstract dirtyCheck(): void;
    abstract onDestroy(callback: Function): void;
}
export declare class FlexRenderTemplateView extends FlexRenderView<EmbeddedViewRef<unknown>> {
    constructor(initialContent: Extract<FlexRenderTypedContent, {
        kind: 'primitive' | 'templateRef';
    }>, view: EmbeddedViewRef<unknown>);
    updateProps(props: Record<string, any>): void;
    dirtyCheck(): void;
    onDestroy(callback: Function): void;
}
export declare class FlexRenderComponentView extends FlexRenderView<FlexRenderComponentRef<unknown>> {
    constructor(initialContent: Extract<FlexRenderTypedContent, {
        kind: 'component' | 'flexRenderComponent';
    }>, view: FlexRenderComponentRef<unknown>);
    updateProps(props: Record<string, any>): void;
    dirtyCheck(): void;
    onDestroy(callback: Function): void;
}
