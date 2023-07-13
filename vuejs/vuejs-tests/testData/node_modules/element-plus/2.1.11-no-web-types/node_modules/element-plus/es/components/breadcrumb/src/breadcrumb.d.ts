import type { ExtractPropTypes } from 'vue';
import type Breadcrumb from './breadcrumb.vue';
export declare const breadcrumbProps: {
    readonly separator: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "/", unknown, unknown, unknown>;
    readonly separatorIcon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
};
export declare type BreadcrumbProps = ExtractPropTypes<typeof breadcrumbProps>;
export declare type BreadcrumbInstance = InstanceType<typeof Breadcrumb>;
