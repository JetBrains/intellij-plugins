declare const _default: {
  new (...args: any[]): {
    $: import("vue").ComponentInternalInstance;
    $data: {};
    $props: Partial<{
      block: boolean;
      icon: string;
    }> & Omit<Readonly<import("vue").ExtractPropTypes<{
      block: {
        type: BooleanConstructor;
        default: boolean;
      };
      icon: {
        type: StringConstructor;
        default: string;
        required: false;
      };
    }>> & import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps, "block" | "icon">;
    $attrs: {
      [x: string]: unknown;
    };
    $refs: {
      [x: string]: unknown;
    };
    $slots: Readonly<{
      [name: string]: import("vue").Slot | undefined;
    }>;
    $root: import("vue").ComponentPublicInstance<{}, {}, {}, {}, {}, {}, {}, {}, false, import("vue").ComponentOptionsBase<any, any, any, any, any, any, any, any, any, {}>> | null;
    $parent: import("vue").ComponentPublicInstance<{}, {}, {}, {}, {}, {}, {}, {}, false, import("vue").ComponentOptionsBase<any, any, any, any, any, any, any, any, any, {}>> | null;
    $emit: (event: string, ...args: any[]) => void;
    $el: any;
    $options: import("vue").ComponentOptionsBase<Readonly<import("vue").ExtractPropTypes<{
      block: {
        type: BooleanConstructor;
        default: boolean;
      };
      icon: {
        type: StringConstructor;
        default: string;
        required: false;
      };
    }>>, {}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {}, string, {
      block: boolean;
      icon: string;
    }> & {
      beforeCreate?: ((() => void) | (() => void)[]) | undefined;
      created?: ((() => void) | (() => void)[]) | undefined;
      beforeMount?: ((() => void) | (() => void)[]) | undefined;
      mounted?: ((() => void) | (() => void)[]) | undefined;
      beforeUpdate?: ((() => void) | (() => void)[]) | undefined;
      updated?: ((() => void) | (() => void)[]) | undefined;
      activated?: ((() => void) | (() => void)[]) | undefined;
      deactivated?: ((() => void) | (() => void)[]) | undefined;
      beforeDestroy?: ((() => void) | (() => void)[]) | undefined;
      beforeUnmount?: ((() => void) | (() => void)[]) | undefined;
      destroyed?: ((() => void) | (() => void)[]) | undefined;
      unmounted?: ((() => void) | (() => void)[]) | undefined;
      renderTracked?: (((e: import("vue").DebuggerEvent) => void) | ((e: import("vue").DebuggerEvent) => void)[]) | undefined;
      renderTriggered?: (((e: import("vue").DebuggerEvent) => void) | ((e: import("vue").DebuggerEvent) => void)[]) | undefined;
      errorCaptured?: (((err: unknown, instance: import("vue").ComponentPublicInstance<{}, {}, {}, {}, {}, {}, {}, {}, false, import("vue").ComponentOptionsBase<any, any, any, any, any, any, any, any, any, {}>> | null, info: string) => boolean | void) | ((err: unknown, instance: import("vue").ComponentPublicInstance<{}, {}, {}, {}, {}, {}, {}, {}, false, import("vue").ComponentOptionsBase<any, any, any, any, any, any, any, any, any, {}>> | null, info: string) => boolean | void)[]) | undefined;
    };
    $forceUpdate: () => void;
    $nextTick: typeof import("vue").nextTick;
    $watch(source: string | Function, cb: Function, options?: import("vue").WatchOptions<boolean> | undefined): import("vue").WatchStopHandle;
  } & Readonly<import("vue").ExtractPropTypes<{
    block: {
      type: BooleanConstructor;
      default: boolean;
    };
    icon: {
      type: StringConstructor;
      default: string;
      required: false;
    };
  }>> & import("vue").ShallowUnwrapRef<{}> & {} & import("vue").ComponentCustomProperties;
  __isFragment?: undefined;
  __isTeleport?: undefined;
  __isSuspense?: undefined;
} & import("vue").ComponentOptionsBase<Readonly<import("vue").ExtractPropTypes<{
  block: {
    type: BooleanConstructor;
    default: boolean;
  };
  icon: {
    type: StringConstructor;
    default: string;
    required: false;
  };
}>>, {}, unknown, {}, {}, import("vue").ComponentOptionsMixin, import("vue").ComponentOptionsMixin, {}, string, {
  block: boolean;
  icon: string;
}> & import("vue").VNodeProps & import("vue").AllowedComponentProps & import("vue").ComponentCustomProps;
export default _default;