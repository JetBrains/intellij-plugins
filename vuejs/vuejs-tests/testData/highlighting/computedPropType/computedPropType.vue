<script>
  export default {
    computed: {
      licenseKey() {
        return 'somethigng'
      },
      secondProp: function() {
        return 12
      },
      isLicenseActive() {
        this.licenseKey !== ''
        this.licenseKey <weak_warning descr="Condition is always true since types 'string' and 'number' have no overlap">!==</weak_warning> 12

        this.secondProp <weak_warning descr="Condition is always true since types 'number' and 'string' have no overlap">!==</weak_warning> ''
        this.secondProp !== 12
      }
    }
  }
</script>
