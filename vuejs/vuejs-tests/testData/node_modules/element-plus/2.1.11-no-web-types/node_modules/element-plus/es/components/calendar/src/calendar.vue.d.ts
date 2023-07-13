import dayjs from 'dayjs';
import type { CalendarDateType } from './calendar';
import type { ComputedRef } from 'vue';
import type { Dayjs } from 'dayjs';
declare const _default: import("vue").DefineComponent<{
    readonly modelValue: import("element-plus/es/utils").BuildPropReturn<DateConstructor, unknown, unknown, unknown, unknown>;
    readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[Date, Date]>, unknown, unknown, unknown, [Date, Date]>;
}, {
    COMPONENT_NAME: string;
    props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
        readonly modelValue: import("element-plus/es/utils").BuildPropReturn<DateConstructor, unknown, unknown, unknown, unknown>;
        readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[Date, Date]>, unknown, unknown, unknown, [Date, Date]>;
    }>> & {
        "onUpdate:modelValue"?: ((value: Date) => any) | undefined;
        onInput?: ((value: Date) => any) | undefined;
    }>>;
    emit: ((event: "update:modelValue", value: Date) => void) & ((event: "input", value: Date) => void);
    ns: {
        namespace: ComputedRef<string>;
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
    t: import("element-plus/es/hooks").Translator;
    lang: import("vue").Ref<string>;
    selectedDay: import("vue").Ref<dayjs.Dayjs | undefined>;
    now: dayjs.Dayjs;
    prevMonthDayjs: ComputedRef<dayjs.Dayjs>;
    nextMonthDayjs: ComputedRef<dayjs.Dayjs>;
    prevYearDayjs: ComputedRef<dayjs.Dayjs>;
    nextYearDayjs: ComputedRef<dayjs.Dayjs>;
    i18nDate: ComputedRef<string>;
    realSelectedDay: import("vue").WritableComputedRef<dayjs.Dayjs | undefined>;
    date: ComputedRef<dayjs.Dayjs>;
    calculateValidatedDateRange: (startDayjs: Dayjs, endDayjs: Dayjs) => [Dayjs, Dayjs][];
    validatedRange: ComputedRef<[dayjs.Dayjs, dayjs.Dayjs][]>;
    pickDay: (day: Dayjs) => void;
    selectDate: (type: CalendarDateType) => void;
    ElButton: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
        readonly disabled: BooleanConstructor;
        readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
        readonly nativeType: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
        readonly loading: BooleanConstructor;
        readonly loadingIcon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
        readonly plain: BooleanConstructor;
        readonly autofocus: BooleanConstructor;
        readonly round: BooleanConstructor;
        readonly circle: BooleanConstructor;
        readonly color: StringConstructor;
        readonly dark: BooleanConstructor;
        readonly autoInsertSpace: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
            readonly disabled: BooleanConstructor;
            readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
            readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
            readonly nativeType: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
            readonly loading: BooleanConstructor;
            readonly loadingIcon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
            readonly plain: BooleanConstructor;
            readonly autofocus: BooleanConstructor;
            readonly round: BooleanConstructor;
            readonly circle: BooleanConstructor;
            readonly color: StringConstructor;
            readonly dark: BooleanConstructor;
            readonly autoInsertSpace: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
        }>> & {
            onClick?: ((evt: MouseEvent) => any) | undefined;
        }>>;
        emit: (event: "click", evt: MouseEvent) => void;
        slots: Readonly<{
            [name: string]: import("vue").Slot | undefined;
        }>;
        buttonGroupContext: import("../../..").ButtonGroupContext | undefined;
        globalConfig: import("vue").Ref<import("element-plus/es/components/button").ButtonConfigContext | undefined>;
        ns: {
            namespace: ComputedRef<string>;
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
        form: import("../../..").FormContext | undefined;
        _size: ComputedRef<"" | "default" | "small" | "large">;
        _disabled: ComputedRef<boolean>;
        _ref: import("vue").Ref<HTMLButtonElement | undefined>;
        _type: ComputedRef<"" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text">;
        autoInsertSpace: ComputedRef<import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>>;
        shouldAddSpace: ComputedRef<boolean>;
        buttonStyle: ComputedRef<Record<string, string>>;
        handleClick: (evt: MouseEvent) => void;
        ElIcon: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
            readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
        }, {
            props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
                readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
                readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
            }>> & {
                [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
            }>>;
            ns: {
                namespace: ComputedRef<string>;
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
            style: ComputedRef<import("vue").CSSProperties>;
        }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown, unknown, unknown>;
            readonly color: import("element-plus/es/utils").BuildPropReturn<StringConstructor, unknown, unknown, unknown, unknown>;
        }>>, {
            size: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<string | number>, unknown, unknown>;
            color: string;
        }>> & Record<string, any>;
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
        click: (evt: MouseEvent) => boolean;
    }, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
        readonly disabled: BooleanConstructor;
        readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        readonly icon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, "", unknown, unknown, unknown>;
        readonly nativeType: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "button", unknown, "button" | "submit" | "reset", unknown>;
        readonly loading: BooleanConstructor;
        readonly loadingIcon: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, () => import("vue").DefineComponent<{}, {}, {}, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, import("vue").EmitsOptions, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{}>>, {}>, unknown, unknown, unknown>;
        readonly plain: BooleanConstructor;
        readonly autofocus: BooleanConstructor;
        readonly round: BooleanConstructor;
        readonly circle: BooleanConstructor;
        readonly color: StringConstructor;
        readonly dark: BooleanConstructor;
        readonly autoInsertSpace: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, undefined, unknown, unknown, unknown>;
    }>> & {
        onClick?: ((evt: MouseEvent) => any) | undefined;
    }, {
        type: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        icon: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
        nativeType: import("element-plus/es/utils").BuildPropType<StringConstructor, "button" | "submit" | "reset", unknown>;
        loadingIcon: import("element-plus/es/utils").BuildPropType<import("element-plus/es/utils").PropWrapper<string | import("vue").Component<any, any, any, import("vue").ComputedOptions, import("vue").MethodOptions>>, unknown, unknown>;
        autoInsertSpace: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
        size: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "small" | "large", never>;
        disabled: boolean;
        loading: boolean;
        plain: boolean;
        autofocus: boolean;
        round: boolean;
        circle: boolean;
        dark: boolean;
    }>> & {
        ButtonGroup: import("vue").DefineComponent<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
            readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        }, {
            props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
                readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
                readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
            }>> & {
                [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
            }>>;
            ns: {
                namespace: ComputedRef<string>;
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
        }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
            readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        }>>, {
            type: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
            size: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "small" | "large", never>;
        }>;
    };
    ElButtonGroup: import("element-plus/es/utils").SFCWithInstall<import("vue").DefineComponent<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
        readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
            readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        }>> & {
            [x: string & `on${string}`]: ((...args: any[]) => any) | ((...args: unknown[]) => any) | undefined;
        }>>;
        ns: {
            namespace: ComputedRef<string>;
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
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, Record<string, any>, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly size: import("element-plus/es/utils").BuildPropReturn<StringConstructor, never, false, "" | "default" | "small" | "large", never>;
        readonly type: import("element-plus/es/utils").BuildPropReturn<StringConstructor, "", unknown, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
    }>>, {
        type: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "primary" | "success" | "warning" | "info" | "danger" | "text", unknown>;
        size: import("element-plus/es/utils").BuildPropType<StringConstructor, "" | "default" | "small" | "large", never>;
    }>>;
    DateTable: import("vue").DefineComponent<{
        readonly selectedDay: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
        readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
        readonly date: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
        readonly hideHeader: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
    }, {
        props: Readonly<import("@vue/shared").LooseRequired<Readonly<import("vue").ExtractPropTypes<{
            readonly selectedDay: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
            readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
            readonly date: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
            readonly hideHeader: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
        }>> & {
            onPick?: ((value: dayjs.Dayjs) => any) | undefined;
        }>>;
        emit: (event: "pick", value: dayjs.Dayjs) => void;
        t: import("element-plus/es/hooks").Translator;
        lang: import("vue").Ref<string>;
        nsTable: {
            namespace: ComputedRef<string>;
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
        nsDay: {
            namespace: ComputedRef<string>;
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
        now: dayjs.Dayjs;
        firstDayOfWeek: number;
        isInRange: ComputedRef<boolean>;
        rows: ComputedRef<import("./date-table").CalendarDateCell[][]>;
        weekDays: ComputedRef<string[]>;
        getFormattedDate: (day: number, type: import("./date-table").CalendarDateCellType) => dayjs.Dayjs;
        getCellClass: ({ text, type }: import("./date-table").CalendarDateCell) => string[];
        handlePickDay: ({ text, type }: import("./date-table").CalendarDateCell) => void;
        getSlotData: ({ text, type }: import("./date-table").CalendarDateCell) => {
            isSelected: boolean;
            type: string;
            day: string;
            date: Date;
        };
    }, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
        pick: (value: dayjs.Dayjs) => boolean;
    }, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
        readonly selectedDay: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, unknown, unknown, unknown>;
        readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[dayjs.Dayjs, dayjs.Dayjs]>, unknown, unknown, unknown, unknown>;
        readonly date: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<dayjs.Dayjs>, unknown, true, unknown, unknown>;
        readonly hideHeader: import("element-plus/es/utils").BuildPropReturn<BooleanConstructor, unknown, unknown, unknown, unknown>;
    }>> & {
        onPick?: ((value: dayjs.Dayjs) => any) | undefined;
    }, {
        range: [dayjs.Dayjs, dayjs.Dayjs];
        selectedDay: dayjs.Dayjs;
        hideHeader: import("element-plus/es/utils").BuildPropType<BooleanConstructor, unknown, unknown>;
    }>;
}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {
    "update:modelValue": (value: Date) => boolean;
    input: (value: Date) => boolean;
}, string, import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, Readonly<import("vue").ExtractPropTypes<{
    readonly modelValue: import("element-plus/es/utils").BuildPropReturn<DateConstructor, unknown, unknown, unknown, unknown>;
    readonly range: import("element-plus/es/utils").BuildPropReturn<import("element-plus/es/utils").PropWrapper<[Date, Date]>, unknown, unknown, unknown, [Date, Date]>;
}>> & {
    "onUpdate:modelValue"?: ((value: Date) => any) | undefined;
    onInput?: ((value: Date) => any) | undefined;
}, {
    modelValue: Date;
    range: [Date, Date];
}>;
export default _default;
