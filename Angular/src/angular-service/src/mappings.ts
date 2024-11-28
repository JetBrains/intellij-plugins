// Keep in sync with Angular2TemplateTranspiler.SourceMappingFlag
export const MAPPING_FLAG_FORMAT = 1
export const MAPPING_FLAG_COMPLETION = 2
export const MAPPING_FLAG_NAVIGATION = 4
export const MAPPING_FLAG_SEMANTIC = 8
export const MAPPING_FLAG_STRUCTURE = 16
export const MAPPING_FLAG_TYPES = 32
export const MAPPING_FLAG_REVERSE_TYPES = 64

export interface Angular2TcbMappingInfo {
  fileName: string,
  sourceOffsets: number[]
  sourceLengths: number[]
  generatedOffsets: number[]
  generatedLengths: number[]
  diagnosticsOffsets: number[]
  diagnosticsLengths: number[]
  flags: number[]
}