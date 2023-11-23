# Devicetree plugin

Provides support for [Devicetree](https://www.devicetree.org). Devicetree is a
data structure used in many hardware platforms, particularly in the embedded
domain. It serves as a hierarchical description of the hardware components and
their configurations present in a system.

## Documentation

There exists a specification for the devicetree format. However, the
specification is not very specific in regard to the syntax of the language.
Therefore, the lexer and parser implementation is based on the devicetree
compiler in the linux kernel. Links to the documentation:

- [Devicetree specification](https://devicetree-specification.readthedocs.io/en/latest)
- [Devicetree compiler](https://git.kernel.org/pub/scm/utils/dtc/dtc.git/tree/Documentation/manual.txt)
- [Linux devicetree reference](https://elinux.org/Device_Tree_Reference)
- [Linux devicetree mysteries](https://elinux.org/Device_Tree_Mysteries)
- [Linux devicetree undocumented](https://elinux.org/Device_Tree_Source_Undocumented)

Devicetree compiler sources:

- [Lexer](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/tree/scripts/dtc/dtc-lexer.l?h=v6.3-rc5)
- [Parser](https://git.kernel.org/pub/scm/linux/kernel/git/torvalds/linux.git/tree/scripts/dtc/dtc-parser.y?h=v6.3-rc5)

Zephyr documentation:

- [Overlays](https://docs.zephyrproject.org/latest/build/dts/howtos.html#set-devicetree-overlays)
- [Bindings](https://docs.zephyrproject.org/latest/build/dts/bindings.html)