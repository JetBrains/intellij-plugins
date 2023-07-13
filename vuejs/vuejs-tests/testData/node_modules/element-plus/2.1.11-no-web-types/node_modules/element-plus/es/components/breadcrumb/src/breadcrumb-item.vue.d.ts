import type { Router } from 'vue-router';
import type { BreadcrumbProps } from './breadcrumb';
declare const _default: import("vue").DefineComponent<{
    readonly to: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<import("vue-router").RouteLocationRaw>, "", unknown, unknown, unknown>;
    readonly replace: import("../../../utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
}, {
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly to: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<import("vue-router").RouteLocationRaw>, "", unknown, unknown, unknown>;
        readonly replace: import("../../../utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
    }>> & {
        [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
    }>>;
    instance: import("vue").ComponentInternalInstance;
    router: Router;
    breadcrumbInjection: BreadcrumbProps;
    ns: {
        namespace: import("vue").ComputedRef<string>;
        b: (blockSuffix?: string) => string;
        e: (element?: string | undefined) => string;
        m: (modifier?: string | undefined) => string;
        be: (blockSuffix?: string | undefined, element?: string | undefined) => string;
        em: (element?: string | undefined, modifier?: string | undefined) => string;
        bm: (blockSuffix?: string | undefined, modifier?: string | undefined) => string;
        bem: (blockSuffix?: string | undefined, element?: string | undefined, modifier?: string | undefined) => string;
        is: {
            (name: string, state: boolean | undefined): string;
            (name: string): string;
        };
    };
    separator: string;
    separatorIcon: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
    link: import("vue").Ref<HTMLSpanElement | undefined>;
    onClick: () => void;
    ElIcon: import("../../../utils").SFCWithInstall<import("vue").DefineComponent<{
        readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
            readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
        }>> & {
            [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
        }>>;
        ns: {
            namespace: import("vue").ComputedRef<string>;
            b: (blockSuffix?: string) => string;
            e: (element?: string | undefined) => string;
            m: (modifier?: string | undefined) => string;
            be: (blockSuffix?: string | undefined, element?: string | undefined) => string;
            em: (element?: string | undefined, modifier?: string | undefined) => string;
            bm: (blockSuffix?: string | undefined, modifier?: string | undefined) => string;
            bem: (blockSuffix?: string | undefined, element?: string | undefined, modifier?: string | undefined) => string;
            is: {
                (name: string, state: boolean | undefined): string;
                (name: string): string;
            };
        };
        style: import("vue").ComputedRef<import("vue").CSSProperties>;
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
        readonly color: import("../../../utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
    }>>, {
        size: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<string | number>, unknown, unknown>;
        color: string;
    }>> & Record<string, any>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly to: import("../../../utils").BuildPropReturn<import("../../../utils").PropWrapper<import("vue-router").RouteLocationRaw>, "", unknown, unknown, unknown>;
    readonly replace: import("../../../utils").BuildPropReturn<BooleanConstructor, false, unknown, unknown, unknown>;
}>>, {
    replace: import("../../../utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    to: import("../../../utils").BuildPropType<import("../../../utils").PropWrapper<import("vue-router").RouteLocationRaw>, unknown, unknown>;
}>;
export default _default;
