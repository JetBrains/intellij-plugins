import type { ExtractPropTypes } from 'vue';
import type { RouteLocationRaw } from 'vue-router';
import type BreadcrumbItem from './breadcrumb-item.vue';
export declare const breadcrumbItemProps: {
    readonly to: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<RouteLocationRaw>, "", unknown, unknown, unknown>;
    readonly replace: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
};
export declare type BreadcrumbItemProps = ExtractPropTypes<typeof breadcrumbItemProps>;
export declare type BreadcrumbItemInstance = InstanceType<typeof BreadcrumbItem>;
