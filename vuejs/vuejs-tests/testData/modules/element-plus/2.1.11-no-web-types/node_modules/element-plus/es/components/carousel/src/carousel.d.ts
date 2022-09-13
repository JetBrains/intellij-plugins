import type { ExtractPropTypes } from 'vue';
import type Carousel from './carousel.vue';
export declare const carouselProps: {
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
};
export declare const carouselEmits: {
    change: (current: number, prev: number) => boolean;
};
export declare type CarouselProps = ExtractPropTypes<typeof carouselProps>;
export declare type CarouselEmits = typeof carouselEmits;
export declare type CarouselInstance = InstanceType<typeof Carousel>;
