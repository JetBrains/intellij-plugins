import { ComputedRef, Ref } from 'vue';
import type { RtlEnabledState, GlobalComponentConfig, Breakpoints } from '../config-provider/src/internal-interface';
declare type UseConfigProps = Readonly<{
    bordered?: boolean;
    [key: string]: unknown;
}>;
export declare const defaultClsPrefix = "n";
export default function useConfig(props?: UseConfigProps, options?: {
    defaultBordered?: boolean;
}): {
    inlineThemeDisabled: boolean | undefined;
    mergedRtlRef: Ref<RtlEnabledState | undefined> | undefined;
    mergedBorderedRef: ComputedRef<boolean>;
    mergedClsPrefixRef: ComputedRef<string>;
    mergedBreakpointsRef: Ref<Breakpoints> | undefined;
    mergedComponentPropsRef: Ref<GlobalComponentConfig | undefined> | undefined;
    namespaceRef: ComputedRef<string | undefined>;
};
export {};
