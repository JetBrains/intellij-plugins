import type { ExtractPropTypes } from 'vue';
import type button from './button.vue';
export declare const buttonTypes: readonly ["default", "primary", "success", "warning", "info", "danger", "text", ""];
export declare const buttonNativeTypes: readonly ["button", "submit", "reset"];
export declare const buttonProps: {
    readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
    readonly disabled: BooleanConstructor;
    readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
    readonly nativeType: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
    readonly loading: BooleanConstructor;
    readonly loadingIcon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
    readonly plain: BooleanConstructor;
    readonly autofocus: BooleanConstructor;
    readonly round: BooleanConstructor;
    readonly circle: BooleanConstructor;
    readonly color: StringConstructor;
    readonly dark: BooleanConstructor;
    readonly autoInsertSpace: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
};
export declare const buttonEmits: {
    click: (evt: MouseEvent) => boolean;
};
export declare type ButtonProps = ExtractPropTypes<typeof buttonProps>;
export declare type ButtonEmits = typeof buttonEmits;
export declare type ButtonType = ButtonProps['type'];
export declare type ButtonNativeType = ButtonProps['nativeType'];
export declare type ButtonInstance = InstanceType<typeof button>;
export interface ButtonConfigContext {
    autoInsertSpace?: boolean;
}
