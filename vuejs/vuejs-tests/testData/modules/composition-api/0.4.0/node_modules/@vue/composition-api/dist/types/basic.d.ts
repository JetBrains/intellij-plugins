export declare type AnyObject = Record<string | number | symbol, any>;
declare type Equal<Left, Right> = (<U>() => U extends Left ? 1 : 0) extends (<U>() => U extends Right ? 1 : 0) ? true : false;
export declare type HasDefined<T> = Equal<T, unknown> extends true ? false : true;
export {};
