import { ComputedRef, Ref } from 'vue';
export declare function useThemeClass(componentName: string, hashRef: Ref<string> | undefined, cssVarsRef: ComputedRef<Record<string, string>> | undefined, props: {
    themeOverrides?: unknown;
    builtinThemeOverrides?: unknown;
}): {
    themeClass: Ref<string>;
    onRender: () => void;
};
