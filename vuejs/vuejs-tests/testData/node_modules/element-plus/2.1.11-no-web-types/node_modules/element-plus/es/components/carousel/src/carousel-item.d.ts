import type { ExtractPropTypes } from 'vue';
import type CarouselItem from './carousel-item.vue';
export declare const carouselItemProps: {
    readonly name: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly label: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
};
export declare type CarouselItemProps = ExtractPropTypes<typeof carouselItemProps>;
export declare type CarouselItemInstance = InstanceType<typeof CarouselItem>;
