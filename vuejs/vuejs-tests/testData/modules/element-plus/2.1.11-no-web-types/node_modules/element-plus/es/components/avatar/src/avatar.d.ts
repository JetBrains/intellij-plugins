import type { ExtractPropTypes } from 'vue';
import type { ObjectFitProperty } from 'csstype';
import type Avatar from './avatar.vue';
export declare const avatarProps: {
    readonly size: import("element-plus/es/utils").BuildPropReturn<readonly [NumberConstructor, StringConstructor], "", unknown, "" | "default" | "small" | "large", number>;
    readonly shape: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "circle", unknown, "circle" | "square", unknown>;
    readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown, unknown, unknown>;
    readonly src: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly alt: StringConstructor;
    readonly srcSet: StringConstructor;
    readonly fit: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<ObjectFitProperty>, "cover", unknown, unknown, unknown>;
};
export declare type AvatarProps = ExtractPropTypes<typeof avatarProps>;
export declare const avatarEmits: {
    error: (evt: Event) => boolean;
};
export declare type AvatarEmits = typeof avatarEmits;
export declare type AvatarInstance = InstanceType<typeof Avatar>;
