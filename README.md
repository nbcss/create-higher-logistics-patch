# Create: Higher Logistics Patch

A small [Create](https://www.curseforge.com/minecraft/mc-mods/create) add-on for NeoForge 1.21.1 that applies bug-fix
patches to Create's higher-logistics and Stock Keeper systems.

## Fixed bugs

- **Gauge could rarely read 0 item in stock** ([Create#10486](https://github.com/Creators-of-Create/Create/issues/10486))
- **Gauge could sometime over-request items** ([Create#9987](https://github.com/Creators-of-Create/Create/issues/9987))
- **Stock Keeper freezing screen with JEI integration** ([Create#9937](https://github.com/Creators-of-Create/Create/issues/9937))
- **Ordered package cannot create correctly with multiple packagers** ([Create#10054](https://github.com/Creators-of-Create/Create/issues/10054)).
- **Promise over-reduction with shared inventories** ([Create#10634](https://github.com/Creators-of-Create/Create/issues/10634)).

## Requirements

- Minecraft 1.21.1
- NeoForge
- Create 6.0+

## Bundled with Create: Factory Controller

[Create: Factory Controller](https://github.com/nbcss/create-factory-controller) bundles this patch via Jar-in-Jar, so
installing Factory Controller installs it automatically. It also works as a standalone drop-in mod alongside Create.

### Developing alongside Create: Factory Controller

Factory Controller consumes this repository as a Gradle **composite build**, expecting it to be checked out next to it:

```
some-folder/
  create-factory-controller/
  create-higher-logistics-patch/   <- this repo
```

With that layout, Factory Controller's build picks up the live sources here and bundles the result. This repository
still builds and runs entirely on its own.

## License

[MIT](LICENSE) (c) nbcss
