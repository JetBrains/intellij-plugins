/// <reference types="node" />
export declare const ElCarousel: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
    readonly initialIndex: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
    readonly height: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly trigger: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
    readonly autoplay: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly interval: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 3000, unknown, unknown, unknown>;
    readonly indicatorPosition: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly indicator: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly arrow: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
    readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly loop: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly direction: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "horizontal", unknown, unknown, unknown>;
    readonly pauseOnHover: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly initialIndex: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
        readonly height: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly trigger: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
        readonly autoplay: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
        readonly interval: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 3000, unknown, unknown, unknown>;
        readonly indicatorPosition: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly indicator: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
        readonly arrow: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
        readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly loop: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
        readonly direction: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "horizontal", unknown, unknown, unknown>;
        readonly pauseOnHover: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    }>> & {
        onChange?: ((current: number, prev: number) => any) | undefined;
    }>>;
    emit: (event: "change", current: number, prev: number) => void;
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
    COMPONENT_NAME: string;
    THROTTLE_TIME: number;
    activeIndex: import("vue").Ref<number>;
    timer: import("vue").Ref<{
        hasRef: () => boolean;
        refresh: () => NodeJS.Timer;
        [Symbol.toPrimitive]: () => number;
        ref: () => NodeJS.Timer;
        unref: () => NodeJS.Timer;
    } | null>;
    hover: import("vue").Ref<boolean>;
    root: import("vue").Ref<HTMLDivElement | undefined>;
    items: import("vue").Ref<{
        props: {
            readonly name: string;
            readonly label: import("element-plus/es/utils").BuildPropType<readonly [StringConstructor, NumberConstructor], unknown, unknown>;
        };
        states: {
            hover: boolean;
            translate: number;
            scale: number;
            active: boolean;
            ready: boolean;
            inStage: boolean;
            animating: boolean;
        };
        uid: number | undefined;
        translateItem: (index: number, activeIndex: number, oldIndex?: number | undefined) => void;
    }[]>;
    arrowDisplay: import("vue").ComputedRef<boolean>;
    hasLabel: import("vue").ComputedRef<boolean>;
    carouselClasses: import("vue").ComputedRef<string[]>;
    indicatorsClasses: import("vue").ComputedRef<string[]>;
    isCardType: import("vue").ComputedRef<boolean>;
    isVertical: import("vue").ComputedRef<boolean>;
    throttledArrowClick: import("lodash").DebouncedFunc<(index: number) => void>;
    throttledIndicatorHover: import("lodash").DebouncedFunc<(index: number) => void>;
    pauseTimer: () => void;
    startTimer: () => void;
    playSlides: () => void;
    setActiveItem: (index: string | number) => void;
    resetItemPosition: (oldIndex?: number | undefined) => void;
    addItem: (item: import("../..").CarouselItemContext) => void;
    removeItem: (uid?: number | undefined) => void;
    itemInStage: (item: import("../..").CarouselItemContext, index: number) => false | "right" | "left";
    handleMouseEnter: () => void;
    handleMouseLeave: () => void;
    handleButtonEnter: (arrow: "right" | "left") => void;
    handleButtonLeave: () => void;
    handleIndicatorClick: (index: number) => void;
    handleIndicatorHover: (index: number) => void;
    prev: () => void;
    next: () => void;
    resizeObserver: import("vue").ShallowRef<{
        isSupported: boolean | undefined;
        stop: () => void;
    } | undefined>;
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
    ArrowLeft: import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>;
    ArrowRight: import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    change: (current: number, prev: number) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly initialIndex: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 0, unknown, unknown, unknown>;
    readonly height: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly trigger: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
    readonly autoplay: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly interval: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 3000, unknown, unknown, unknown>;
    readonly indicatorPosition: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly indicator: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly arrow: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "hover", unknown, unknown, unknown>;
    readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly loop: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly direction: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "horizontal", unknown, unknown, unknown>;
    readonly pauseOnHover: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
}>> & {
    onChange?: ((current: number, prev: number) => any) | undefined;
}, {
    type: string;
    trigger: string;
    height: string;
    direction: string;
    indicator: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    initialIndex: number;
    autoplay: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    interval: number;
    indicatorPosition: string;
    arrow: string;
    loop: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    pauseOnHover: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
}>> & {
    CarouselItem: import("vue").DefineComponent<{
        readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
            readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
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
        COMPONENT_NAME: string;
        carouselContext: import("../..").CarouselContext;
        instance: import("vue").ComponentInternalInstance;
        CARD_SCALE: number;
        hover: import("vue").Ref<boolean>;
        translate: import("vue").Ref<number>;
        scale: import("vue").Ref<number>;
        active: import("vue").Ref<boolean>;
        ready: import("vue").Ref<boolean>;
        inStage: import("vue").Ref<boolean>;
        animating: import("vue").Ref<boolean>;
        isCardType: import("vue").Ref<boolean>;
        isVertical: import("vue").Ref<boolean>;
        itemStyle: import("vue").ComputedRef<import("vue").CSSProperties>;
        processIndex: (index: number, activeIndex: number, length: number) => number;
        calcCardTranslate: (index: number, activeIndex: number) => number;
        calcTranslate: (index: number, activeIndex: number, isVertical: boolean) => number;
        translateItem: (index: number, activeIndex: number, oldIndex?: number | undefined) => void;
        handleItemClick: () => void;
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
    }>>, {
        name: string;
        label: import("element-plus/es/utils").BuildPropType<readonly [StringConstructor, NumberConstructor], unknown, unknown>;
    }>;
};
export default ElCarousel;
export declare const ElCarouselItem: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
    readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
        readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
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
    COMPONENT_NAME: string;
    carouselContext: import("../..").CarouselContext;
    instance: import("vue").ComponentInternalInstance;
    CARD_SCALE: number;
    hover: import("vue").Ref<boolean>;
    translate: import("vue").Ref<number>;
    scale: import("vue").Ref<number>;
    active: import("vue").Ref<boolean>;
    ready: import("vue").Ref<boolean>;
    inStage: import("vue").Ref<boolean>;
    animating: import("vue").Ref<boolean>;
    isCardType: import("vue").Ref<boolean>;
    isVertical: import("vue").Ref<boolean>;
    itemStyle: import("vue").ComputedRef<import("vue").CSSProperties>;
    processIndex: (index: number, activeIndex: number, length: number) => number;
    calcCardTranslate: (index: number, activeIndex: number) => number;
    calcTranslate: (index: number, activeIndex: number, isVertical: boolean) => number;
    translateItem: (index: number, activeIndex: number, oldIndex?: number | undefined) => void;
    handleItemClick: () => void;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
}>>, {
    name: string;
    label: import("element-plus/es/utils").BuildPropType<readonly [StringConstructor, NumberConstructor], unknown, unknown>;
}>>;
export * from './src/carousel';
export * from './src/carousel-item';
