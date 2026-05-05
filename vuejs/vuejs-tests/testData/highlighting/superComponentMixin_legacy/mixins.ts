import { Component, Vue } from 'vue-property-decorator'

const negativeZeroPattern = /^-0(\.0*)?$/

@Component({
  filters: {
    formatDigitsAfterDecimal (value, decimalPlaces = 0): string {
      if (typeof value === 'number') {
        let result = value.toFixed(decimalPlaces)
        if (negativeZeroPattern.test(result)) {
          // Workaround to avoid returning negative zero
          result = result.substring(1)
        }
        return result
      } else {
        return value
      }
    }
  }
})
export class ConversionMixin extends Vue {

}
