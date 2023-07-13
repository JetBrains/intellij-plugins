declare const _default: import("vue").DefineComponent<{
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    value: {
        type: (StringConstructor | NumberConstructor)[];
        default: number;
    };
    max: {
        type: NumberConstructor;
        default: undefined;
    };
    appeared: {
        type: BooleanConstructor;
        required: true;
    };
}, () => JSX.Element, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    clsPrefix: {
        type: StringConstructor;
        required: true;
    };
    value: {
        type: (StringConstructor | NumberConstructor)[];
        default: number;
    };
    max: {
        type: NumberConstructor;
        default: undefined;
    };
    appeared: {
        type: BooleanConstructor;
        required: true;
    };
}>>, {
    value: string | number;
    max: number;
}>;
export default _default;
