import type { App, Plugin } from 'vue';
export declare const makeInstaller: (components?: Plugin[]) => {
    version: string;
    install: (app: App, options?: Partial<import("vue").ExtractPropTypes<{
        readonly a11y: import("./utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
        readonly locale: import("./utils").BuildPropReturn<import("./utils").PropWrapper<import("./locale").Language>, unknown, unknown, unknown, unknown>;
        readonly size: import("./utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "small" | "large", unknown>;
        readonly button: import("./utils").BuildPropReturn<import("./utils").PropWrapper<import("./components").ButtonConfigContext>, unknown, unknown, unknown, unknown>;
        readonly experimentalFeatures: import("./utils").BuildPropReturn<import("./utils").PropWrapper<import("element-plus/es/tokens").ExperimentalFeatures>, unknown, unknown, unknown, unknown>;
        readonly keyboardNavigation: import("./utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
        readonly message: import("./utils").BuildPropReturn<import("./utils").PropWrapper<import("./components").MessageConfigContext>, unknown, unknown, unknown, unknown>;
        readonly zIndex: import("./utils").BuildPropReturn<NumberConstructor, unknown, unknown, unknown, unknown>;
        readonly namespace: import("./utils").BuildPropReturn<StringConstructor, "el", unknown, unknown, unknown>;
    }>> | undefined) => void;
};
