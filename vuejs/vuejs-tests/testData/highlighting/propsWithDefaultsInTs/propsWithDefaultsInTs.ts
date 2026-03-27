type DefineProps<T, BKeys extends keyof T> = Readonly<T> & {
  readonly [K in BKeys]-?: boolean;
};

export declare function defineProps<TypeProps>(): DefineProps<TypeProps, never>;

let props = defineProps<{
  title?: string;
}>();

export declare function withDefaults<T, BKeys extends keyof T>(props: DefineProps<T, BKeys>): void;

withDefaults(props);
