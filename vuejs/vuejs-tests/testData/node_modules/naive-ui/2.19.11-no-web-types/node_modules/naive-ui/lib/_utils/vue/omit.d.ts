export declare function omit<T, K extends keyof T, R extends Record<string, any>>(object: T, keys?: K[], rest?: R): Omit<T, K> & (R extends undefined ? {} : R);
