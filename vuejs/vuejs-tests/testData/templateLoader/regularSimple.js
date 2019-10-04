import withRender from './regularSimple.html'

export default withRender(
    {
      data() {
        return {
          text: 'Example text',
          fooBar: 12
        }
      },

      methods: {
        log() {
          console.log('output log')
        }
      }
    })
