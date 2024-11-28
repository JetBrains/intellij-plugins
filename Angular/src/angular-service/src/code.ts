import {CodeMapping, VirtualCode} from "@volar/language-core"
import type * as ts from "typescript";
import {IScriptSnapshot} from "typescript";
import {CodegenContext} from "@volar/language-core/lib/types"
import {
  Angular2TcbMappingInfo,
  MAPPING_FLAG_COMPLETION,
  MAPPING_FLAG_FORMAT,
  MAPPING_FLAG_NAVIGATION,
  MAPPING_FLAG_SEMANTIC,
  MAPPING_FLAG_STRUCTURE,
  MAPPING_FLAG_TYPES,
  MAPPING_FLAG_REVERSE_TYPES,
} from "./mappings"

export class AngularVirtualCode implements VirtualCode {

  public snapshot: IScriptSnapshot = createEmptySnapshot();

  public mappings: CodeMapping[] = []

  public associatedScriptMappings: Map<string, CodeMapping[]> = new Map<string, CodeMapping[]>();

  get id(): string {
    return "main"
  }

  get languageId(): string {
    return "typescript"
  }

  constructor(private fileName: string) {
  }

  sourceFileUpdated(
    ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
    snapshot: ts.IScriptSnapshot,
    ctx: CodegenContext<string>,
    transpiledTemplate: AngularTranspiledTemplate | undefined,
  ): AngularVirtualCode {
    this.associatedScriptMappings.clear()
    if (transpiledTemplate && this.isTemplateInSync(ts, snapshot, ctx, transpiledTemplate)) {
      this.snapshot = transpiledTemplate.snapshot
      this.mappings = []
      transpiledTemplate.mappings.forEach(mappingSet => {
        let mappingsWithData: CodeMapping[]
        if (ts.server.toNormalizedPath(mappingSet.fileName) === ts.server.toNormalizedPath(this.fileName)) {
          mappingsWithData = this.mappings
        }
        else {
          const associatedScript = ctx.getAssociatedScript(mappingSet.fileName)
          const scriptId = associatedScript?.id
          if (scriptId) {
            if (!this.associatedScriptMappings.has(scriptId)) {
              this.associatedScriptMappings.set(scriptId, [])
            }
            mappingsWithData = this.associatedScriptMappings.get(scriptId)!!
          }
          else {
            return
          }
        }
        // Split the mapping set for Volar
        for (let i = 0; i < mappingSet.sourceOffsets.length; i++) {
          const sourceOffset = mappingSet.sourceOffsets[i]
          const sourceLength = mappingSet.sourceLengths[i]

          const diagnosticsOffset = mappingSet.diagnosticsOffsets[i]
          const diagnosticsLength = mappingSet.diagnosticsLengths[i]

          const generatedOffset = mappingSet.generatedOffsets[i];
          const generatedLength = mappingSet.generatedLengths[i];

          const flags = mappingSet.flags[i]
          mappingsWithData.push({
            sourceOffsets: [sourceOffset],
            lengths: [sourceLength],
            generatedOffsets: [generatedOffset],
            generatedLengths: [generatedLength],
            data: {
              format: (flags & MAPPING_FLAG_FORMAT) !== 0,
              completion: (flags & MAPPING_FLAG_COMPLETION) !== 0,
              navigation: (flags & MAPPING_FLAG_NAVIGATION) !== 0,
              semantic: (flags & MAPPING_FLAG_SEMANTIC) !== 0,
              structure: (flags & MAPPING_FLAG_STRUCTURE) !== 0,
              verification: diagnosticsOffset == sourceOffset && diagnosticsLength == sourceLength,
              types: (flags & MAPPING_FLAG_TYPES) !== 0,
              reverseTypes: (flags & MAPPING_FLAG_REVERSE_TYPES) !== 0,
            }
          })
          if (diagnosticsOffset >= 0 && (diagnosticsOffset != sourceOffset || diagnosticsLength != sourceLength)) {
            mappingsWithData.push({
              sourceOffsets: [diagnosticsOffset],
              lengths: [diagnosticsLength],
              generatedOffsets: [generatedOffset],
              generatedLengths: [generatedLength],
              data: {
                format: false,
                completion: false,
                navigation: false,
                semantic: false,
                structure: false,
                verification: true,
                types: false,
                reverseTypes: false,
              }
            })
          }
        }
      })
    }
    else {
      this.snapshot = snapshot
      this.mappings = [{
        generatedOffsets: [0],
        sourceOffsets: [0],
        lengths: [snapshot.getLength()],
        data: {
          format: true,
          completion: true,
          navigation: true,
          semantic: true,
          structure: true,
          verification: true,
          types: true,
          reverseTypes: true,
        }
      }]
    }
    return this
  }

  private isTemplateInSync(
    ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
    snapshot: ts.IScriptSnapshot,
    ctx: CodegenContext<string>,
    transpiledTemplate: AngularTranspiledTemplate,
  ) {
    const sourceText = getNormalizedSnapshotText(snapshot)
    if (!getNormalizedSnapshotText(transpiledTemplate.snapshot).startsWith(sourceText)
      || sourceText !== transpiledTemplate.sourceCode[ts.server.toNormalizedPath(this.fileName)]) {
      return false
    }
    else if (transpiledTemplate.mappings.find(mapping => {
      return getNormalizedSnapshotText(ctx.getAssociatedScript(mapping.fileName)?.snapshot)
        !== transpiledTemplate.sourceCode[ts.server.toNormalizedPath(mapping.fileName)]
    })) {
      return false
    }
    return true
  }
}

export function buildAngularTranspiledTemplate(
  ts: typeof import("tsc-ide-plugin/tsserverlibrary.shim"),
  transpiledCode: string | undefined,
  sourceCode: { [fileName: string]: string },
  mappings: Angular2TcbMappingInfo[]
): AngularTranspiledTemplate | undefined {
  if (transpiledCode) {
    return {
      mappings: mappings.map(it => ({
        ...it,
        fileName: ts.server.toNormalizedPath(it.fileName),
      })),
      sourceCode: Object.fromEntries(Object.entries(sourceCode).map(([key, value]) => {
        return [ts.server.toNormalizedPath(key), value]
      })),
      snapshot: createScriptSnapshot(transpiledCode)
    }
  }
  else {
    return undefined
  }
}


export interface AngularTranspiledTemplate {
  sourceCode: { [fileName: string]: string },
  snapshot: IScriptSnapshot
  mappings: Angular2TcbMappingInfo[]
}

function getNormalizedSnapshotText(snapshot: IScriptSnapshot | undefined): string {
  if (!snapshot) return ""
  if ((snapshot as any).__normalizedSnapshotText)
    return (snapshot as any).__normalizedSnapshotText
  const result = snapshot.getText(0, snapshot.getLength()).replaceAll(/\r\n|\n\r/g, "\n");
  (snapshot as any).__normalizedSnapshotText = result
  return result;
}

function fullDiffTextChangeRange(oldText: string, newText: string): ts.TextChangeRange | undefined {
  for (let start = 0; start < oldText.length && start < newText.length; start++) {
    if (oldText[start] !== newText[start]) {
      let end = oldText.length;
      for (let i = 0; i < oldText.length - start && i < newText.length - start; i++) {
        if (oldText[oldText.length - i - 1] !== newText[newText.length - i - 1]) {
          break;
        }
        end--;
      }
      let length = end - start;
      let newLength = length + (newText.length - oldText.length);
      if (newLength < 0) {
        length -= newLength;
        newLength = 0;
      }
      return {
        span: {start, length},
        newLength,
      };
    }
  }
  return undefined
}

function createScriptSnapshot(code: string): IScriptSnapshot {
  const changeRanges = new Map<ts.IScriptSnapshot, ts.TextChangeRange | undefined>()
  return {
    getText: (start, end) => (code ?? "").slice(start, end),
    getLength: () => (code ?? "").length,
    getChangeRange(oldSnapshot) {
      if (!changeRanges.has(oldSnapshot)) {
        changeRanges.set(oldSnapshot, undefined);
        const oldText = oldSnapshot.getText(0, oldSnapshot.getLength());
        const changeRange = fullDiffTextChangeRange(oldText, (code ?? ""));
        if (changeRange) {
          changeRanges.set(oldSnapshot, changeRange);
        }
      }
      return changeRanges.get(oldSnapshot);
    },
  }
}

function createEmptySnapshot(): IScriptSnapshot {
  return {
    getText: (start, end) => "",
    getLength: () => 0,
    getChangeRange(oldSnapshot) {
      return {
        span: {
          start: 0,
          length: oldSnapshot.getLength(),
        },
        newLength: 0
      };
    },
  }
}