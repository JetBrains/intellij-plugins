declare module 'vue' {
  interface ComponentCustomProperties {
    $methodFromCustomProps(value?: string): string
    $methodFromCustomProps(value: number, radix?: number): string
  }
}

export {}
