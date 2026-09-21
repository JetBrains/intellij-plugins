class App {
  duplicatedMethod1() {
    const strings = [];
    strings[0] = "mmm";
    strings[1] = "mmm";
    strings[2] = "mmm";
    strings[3] = "mmm";
    for (let i = 0; i < strings.length; i++) {
      alert(strings[i]);
    }
  }

  duplicatedMethod2() {
    const strings = [];
    strings[0] = "mmm";
    strings[1] = "mmm";
    strings[2] = "mmm";
    strings[3] = "mmm";
    for (let i = 0; i < strings.length; i++) {
      alert(strings[i]);
    }
  }

  static main(args) {
    console.log("Hello World!");
    if (true) {
    }
  }
}
