import {CodeMapping, VirtualCode} from "@volar/language-core"
import * as ts from "typescript";
import {IScriptSnapshot} from "typescript";
import {CodegenContext} from "@volar/language-core/lib/types"

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

  private transpiledTemplate: {
    sourceCode: { [fileName: string]: string },
    snapshot: IScriptSnapshot
    mappings: ({ source: string } & CodeMapping)[]
  } | undefined

  constructor(private fileName: string, private ctx: CodegenContext<string>, private useCaseSensitiveFileNames: boolean) {
  }

  sourceFileUpdated(snapshot: ts.IScriptSnapshot, _languageId?: string): AngularVirtualCode {
    this.associatedScriptMappings.clear()
    if (this.transpiledTemplate) {
      // Check if the template is still valid
      if (snapshot.getChangeRange(this.transpiledTemplate.snapshot) !== undefined
          || !sameContents(snapshot, this.transpiledTemplate.sourceCode[this.normalizeId(this.fileName)])) {
        this.transpiledTemplate = undefined
      } else if (this.transpiledTemplate.mappings.find(mapping => {
          return !sameContents(this.ctx.getAssociatedScript(mapping.source)?.snapshot,
                               this.transpiledTemplate?.sourceCode?.[this.normalizeId(mapping.source)])
        })) {
        this.transpiledTemplate = undefined
      }
    }
    if (this.transpiledTemplate) {
      this.snapshot = this.transpiledTemplate.snapshot
      this.mappings = []
      this.transpiledTemplate.mappings.forEach(mappingSet => {
        let mappingsWithData: CodeMapping[]
        if (this.normalizeId(mappingSet.source) === this.normalizeId(this.fileName)) {
          mappingsWithData = this.mappings
        }
        else {
          const associatedScript = this.ctx.getAssociatedScript(mappingSet.source)
          const scriptId = associatedScript?.id
          if (scriptId) {
            if (!this.associatedScriptMappings.has(scriptId)) {
              this.associatedScriptMappings.set(scriptId, [])
            }
            mappingsWithData = this.associatedScriptMappings.get(scriptId)!!
          } else {
            return
          }
        }
        // Split the mapping set for Volar
        for (let i = 0 ; i < mappingSet.sourceOffsets.length; i++) {
          const generatedLength = mappingSet.generatedLengths?.[i];
          mappingsWithData.push({
            sourceOffsets: [mappingSet.sourceOffsets[i]],
            lengths: [mappingSet.lengths[i]],
            generatedOffsets: [mappingSet.generatedOffsets[i]],
            generatedLengths: generatedLength ? [generatedLength] : undefined,
            data: {
              format: mappingSet.source === this.fileName,
              completion: true,
              navigation: true,
              semantic: true,
              structure: true,
              verification: true,
            }
          })
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
        }
      }]
    }
    return this
  }

  transpiledTemplateUpdated(
    transpiledCode: string | undefined,
    sourceCode: { [fileName: string]: string },
    mappings: ({ source: string } & CodeMapping)[]
  ) {
    if (transpiledCode) {
      this.transpiledTemplate = {
        mappings,
        sourceCode: Object.fromEntries(Object.entries(sourceCode).map(([key, value]) => {
          return [this.normalizeId(key), value]
        })),
        snapshot: createScriptSnapshot(transpiledCode)
      }
    }
    else {
      this.transpiledTemplate = undefined
    }
  }

  normalizeId(id: string): string {
    return this.useCaseSensitiveFileNames ? id : id.toLowerCase()
  }

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

function sameContents(snapshot: IScriptSnapshot | undefined, code: string | undefined): boolean {
  return !!snapshot && !!code && snapshot.getText(0, snapshot.getLength()) === code
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