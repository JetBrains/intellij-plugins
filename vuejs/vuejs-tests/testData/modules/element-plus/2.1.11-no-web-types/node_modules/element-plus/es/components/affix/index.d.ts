export declare const ElAffix: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
    readonly zIndex: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ZIndexProperty>, 100, unknown, unknown, unknown>;
    readonly target: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly offset: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
    readonly position: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "top", unknown, "top" | "bottom", unknown>;
}, {
    COMPONENT_NAME: string;
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly zIndex: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ZIndexProperty>, 100, unknown, unknown, unknown>;
        readonly target: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly offset: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
        readonly position: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "top", unknown, "top" | "bottom", unknown>;
    }>> & {
        onChange?: ((fixed: boolean) => any) | undefined;
        onScroll?: ((args_0: {
            scrollTop: number;
            fixed: boolean;
        }) => any) | undefined;
    }>>;
    emit: ((event: "change", fixed: boolean) => void) & ((event: "scroll", args_0: {
        scrollTop: number;
        fixed: boolean;
    }) => void);
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
    target: import("vue").ShallowRef<HTMLElement | undefined>;
    root: import("vue").ShallowRef<HTMLDivElement | undefined>;
    scrollContainer: import("vue").ShallowRef<Window | HTMLElement | undefined>;
    windowHeight: import("vue").Ref<number>;
    rootHeight: import("vue").Ref<number>;
    rootWidth: import("vue").Ref<number>;
    rootTop: import("vue").Ref<number>;
    rootBottom: import("vue").Ref<number>;
    updateRoot: () => void;
    targetRect: {
        height: import("vue").Ref<number>;
        bottom: import("vue").Ref<number>;
        left: import("vue").Ref<number>;
        right: import("vue").Ref<number>;
        top: import("vue").Ref<number>;
        width: import("vue").Ref<number>;
        x: import("vue").Ref<number>;
        y: import("vue").Ref<number>;
        update: () => void;
    };
    fixed: import("vue").Ref<boolean>;
    scrollTop: import("vue").Ref<number>;
    transform: import("vue").Ref<number>;
    rootStyle: import("vue").ComputedRef<import("vue").CSSProperties>;
    affixStyle: import("vue").ComputedRef<import("vue").CSSProperties>;
    update: () => void;
    handleScroll: () => void;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    scroll: ({ scrollTop, fixed }: {
        scrollTop: number;
        fixed: boolean;
    }) => boolean;
    change: (fixed: boolean) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly zIndex: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<import("csstype").ZIndexProperty>, 100, unknown, unknown, unknown>;
    readonly target: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly offset: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
    readonly position: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "top", unknown, "top" | "bottom", unknown>;
}>> & {
    onChange?: ((fixed: boolean) => any) | undefined;
    onScroll?: ((args_0: {
        scrollTop: number;
        fixed: boolean;
    }) => any) | undefined;
}, {
    zIndex: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<import("csstype").ZIndexProperty>, unknown, unknown>;
    target: string;
    offset: number;
    position: import("element-plus/es/utils").BuildPropType<StringConstructor, "top" | "bottom", unknown>;
}>> & Record<string, any>;
export default ElAffix;
export * from './src/affix';
