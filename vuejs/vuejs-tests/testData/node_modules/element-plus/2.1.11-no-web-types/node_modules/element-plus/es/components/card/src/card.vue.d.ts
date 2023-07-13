declare const _default: import("vue").DefineComponent<{
    readonly header: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly bodyStyle: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<import("vue").StyleValue>, "", unknown, unknown, unknown>;
    readonly shadow: import("../../../utils").BuildPropReturn<StringConstructor, "always", unknown, unknown, unknown>;
}, {
    ns: {
        namespace: import("vue").ComputedRef<string>;
        b: (blockSuffix?: string) => string;
        e: (element?: string | undefined) => string;
        m: (modifier?: string | undefined) => string;
        be: (blockSuffix?: string | undefined, element?: string | undefined) => string;
        em: (element?: string | undefined, modifier?: string | undefined) => string;
        bm: (blockSuffix?: string | undefined, modifier?: string | undefined) => string;
        bem: (blockSuffix?: string | undefined, element?: string | undefined, modifier?: string | undefined) => string;
        is: {
            (name: string, state: boolean | undefined): string;
            (name: string): string;
        };
    };
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly header: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly bodyStyle: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<import("vue").StyleValue>, "", unknown, unknown, unknown>;
    readonly shadow: import("../../../utils").BuildPropReturn<StringConstructor, "always", unknown, unknown, unknown>;
}>>, {
    header: string;
    bodyStyle: import("vue").StyleValue;
    shadow: string;
}>;
export default _default;
