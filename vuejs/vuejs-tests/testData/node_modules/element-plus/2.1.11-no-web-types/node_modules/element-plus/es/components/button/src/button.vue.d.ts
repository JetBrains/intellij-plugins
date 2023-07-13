declare const _default: import("vue").DefineComponent<{
    readonly size: import("../../../utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
    readonly disabled: BooleanConstructor;
    readonly type: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    readonly icon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
    readonly nativeType: import("../../../utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
    readonly loading: BooleanConstructor;
    readonly loadingIcon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
    readonly plain: BooleanConstructor;
    readonly autofocus: BooleanConstructor;
    readonly round: BooleanConstructor;
    readonly circle: BooleanConstructor;
    readonly color: StringConstructor;
    readonly dark: BooleanConstructor;
    readonly autoInsertSpace: import("../../../utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("../../../utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
        readonly disabled: BooleanConstructor;
        readonly type: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        readonly icon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
        readonly nativeType: import("../../../utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
        readonly loading: BooleanConstructor;
        readonly loadingIcon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
        readonly plain: BooleanConstructor;
        readonly autofocus: BooleanConstructor;
        readonly round: BooleanConstructor;
        readonly circle: BooleanConstructor;
        readonly color: StringConstructor;
        readonly dark: BooleanConstructor;
        readonly autoInsertSpace: import("../../../utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
    }>> & {
        onClick?: ((evt: MouseEvent) => any) | undefined;
    }>>;
    emit: (event: "click", evt: MouseEvent) => void;
    slots: Readonly<{
        [name: string]: import("vue").Slot | undefined;
    }>;
    buttonGroupContext: import("element-plus/es/tokens").ButtonGroupContext | undefined;
    globalConfig: import("vue").Ref<import("./button").ButtonConfigContext | undefined>;
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
    form: import("element-plus/es/tokens").FormContext | undefined;
    _size: import("vue").ComputedRef<"" | "default" | "small" | "large">;
    _disabled: import("vue").ComputedRef<boolean>;
    _ref: import("vue").Ref<HTMLButtonElement | undefined>;
    _type: import("vue").ComputedRef<"" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text">;
    autoInsertSpace: import("vue").ComputedRef<import("../../../utils").BuildPropType<BooleanConstructor, unknown, unknown>>;
    shouldAddSpace: import("vue").ComputedRef<boolean>;
    buttonStyle: import("vue").ComputedRef<Record<string, string>>;
    handleClick: (evt: MouseEvent) => void;
    ElIcon: import("../../../utils").SFCWithInstall<import("vue").DefineComponent<{
        readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
            readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
        }>> & {
            [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
        }>>;
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
        style: import("vue").ComputedRef<import("vue").CSSProperties>;
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }>>, {
        size: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<string | number>, unknown, unknown>;
        color: string;
    }>> & Record<string, any>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    click: (evt: MouseEvent) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly size: import("../../../utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
    readonly disabled: BooleanConstructor;
    readonly type: import("../../../utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    readonly icon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
    readonly nativeType: import("../../../utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
    readonly loading: BooleanConstructor;
    readonly loadingIcon: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
    readonly plain: BooleanConstructor;
    readonly autofocus: BooleanConstructor;
    readonly round: BooleanConstructor;
    readonly circle: BooleanConstructor;
    readonly color: StringConstructor;
    readonly dark: BooleanConstructor;
    readonly autoInsertSpace: import("../../../utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
}>> & {
    onClick?: ((evt: MouseEvent) => any) | undefined;
}, {
    type: import("../../../utils").BuildPropType<StringConstructor, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    icon: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
    nativeType: import("../../../utils").BuildPropType<StringConstructor, "button" | "submit" | "reset", unknown>;
    loadingIcon: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
    autoInsertSpace: import("../../../utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    size: import("../../../utils").BuildPropType<StringConstructor, "" | "default" | "small" | "large", never>;
    disabled: boolean;
    loading: boolean;
    plain: boolean;
    autofocus: boolean;
    round: boolean;
    circle: boolean;
    dark: boolean;
}>;
export default _default;
