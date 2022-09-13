export declare const ElAvatar: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
    readonly size: import("element-plus/es/utils").BuildPropReturn<readonly [NumberConstructor, StringConstructor], "", unknown, "" | "default" | "small" | "large", number>;
    readonly shape: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "circle", unknown, "circle" | "square", unknown>;
    readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown, unknown, unknown>;
    readonly src: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly alt: StringConstructor;
    readonly srcSet: StringConstructor;
    readonly fit: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ObjectFitProperty>, "cover", unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<readonly [NumberConstructor, StringConstructor], "", unknown, "" | "default" | "small" | "large", number>;
        readonly shape: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "circle", unknown, "circle" | "square", unknown>;
        readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown, unknown, unknown>;
        readonly src: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly alt: StringConstructor;
        readonly srcSet: StringConstructor;
        readonly fit: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ObjectFitProperty>, "cover", unknown, unknown, unknown>;
    }>> & {
        onError?: ((evt: Event) => any) | undefined;
    }>>;
    emit: (event: "error", evt: Event) => void;
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
    hasLoadError: import("vue").Ref<boolean>;
    avatarClass: import("vue").ComputedRef<string[]>;
    sizeStyle: import("vue").ComputedRef<import("vue").CSSProperties | undefined>;
    fitStyle: import("vue").ComputedRef<import("vue").CSSProperties>;
    handleError: (e: Event) => void;
    ElIcon: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
            readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
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
        readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }>>, {
        size: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown>;
        color: string;
    }>> & Record<string, any>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    error: (evt: Event) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly size: import("element-plus/es/utils").BuildPropReturn<readonly [NumberConstructor, StringConstructor], "", unknown, "" | "default" | "small" | "large", number>;
    readonly shape: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "circle", unknown, "circle" | "square", unknown>;
    readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown, unknown, unknown>;
    readonly src: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly alt: StringConstructor;
    readonly srcSet: StringConstructor;
    readonly fit: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ObjectFitProperty>, "cover", unknown, unknown, unknown>;
}>> & {
    onError?: ((evt: Event) => any) | undefined;
}, {
    icon: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
    size: import("element-plus/es/utils").BuildPropType<readonly [NumberConstructor, StringConstructor], "" | "default" | "small" | "large", number>;
    shape: import("element-plus/es/utils").BuildPropType<StringConstructor, "circle" | "square", unknown>;
    src: string;
    fit: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<import("csstype").ObjectFitProperty>, unknown, unknown>;
}>> & Record<string, any>;
export default ElAvatar;
export * from './src/avatar';
