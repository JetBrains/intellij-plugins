import type { ExtractPropTypes } from 'vue';
import type Autocomplete from './autocomplete.vue';
import type { Placement } from 'element-plus/es/components/popper';
export declare const autocompleteProps: {
    readonly valueKey: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "value", unknown, unknown, unknown>;
    readonly modelValue: import("element-plus/es/utils").BuildPropReturn<readonly [StringConstructor, NumberConstructor], "", unknown, unknown, unknown>;
    readonly debounce: import("element-plus/es/utils").BuildPropReturn<NumberConstructor, 300, unknown, unknown, unknown>;
    readonly placement: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<Placement>, "bottom-start", unknown, "top" | "bottom" | "top-start" | "top-end" | "bottom-start" | "bottom-end", unknown>;
    readonly fetchSuggestions: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<((queryString: string, cb: (data: {
        value: string;
    }[]) => void) => {
        value: string;
    }[] | Promise<{
        value: string;
    }[]> | void) | {
        value: string;
    }[]>, () => void, unknown, unknown, unknown>;
    readonly popperClass: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, unknown, unknown>;
    readonly triggerOnFocus: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly selectWhenUnmatched: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
    readonly hideLoading: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
    readonly popperAppendToBody: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
    readonly teleported: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, true, unknown, unknown, unknown>;
    readonly highlightFirstItem: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
};
export declare type AutocompleteProps = ExtractPropTypes<typeof autocompleteProps>;
export declare const autocompleteEmits: {
    "update:modelValue": (value: string) => boolean;
    input: (value: string) => boolean;
    change: (value: string) => boolean;
    focus: (evt: FocusEvent) => boolean;
    blur: (evt: FocusEvent) => boolean;
    clear: () => boolean;
    select: (item: {
        value: any;
    }) => boolean;
};
export declare type AutocompleteEmits = typeof autocompleteEmits;
export declare type AutocompleteInstance = InstanceType<typeof Autocomplete>;
