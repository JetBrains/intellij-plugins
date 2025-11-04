export {}

declare module 'vue' {
  interface ComponentCustomProperties {
    $t1(a: string, b: string): string
    $t1(a: number, b: number): string
    $t1(a: bigint, b: bigint): string

    $t2: (a: string, b: string) => string
  }
}