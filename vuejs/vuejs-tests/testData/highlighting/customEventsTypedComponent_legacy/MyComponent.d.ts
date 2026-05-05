declare const _default: import("vue").DefineComponent<{
    message: {
        type: StringConstructor;
        required: true;
    };
}, () => import("vue").VNode<import("vue").RendererNode, import("vue").RendererElement, {
    [key: string]: any;
}>, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    custom(message: string): boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    message: {
        type: StringConstructor;
        required: true;
    };
}>> & {
    onCustom?: ((message: string) => any) | undefined;
}, {}>;
export default _default;
