# DataTip

JSON-driven custom item tooltips for Minecraft. Put content under `assets/<namespace>/datatip/` in a
resource pack. Every `.json` file in that directory and its subdirectories is loaded, so filenames are
arbitrary and content can be split across multiple files by item or purpose.

## Quick Start

```json
{
  "minecraft:diamond": {
    "type": "text",
    "text": "A shiny diamond",
    "color": "#55FFFF"
  }
}
```

Save this as any JSON file in the resource pack, for example
`assets/minecraft/datatip/getting_started.json`, then hover a diamond in-game. `datatip.json` is only an
example filename, not a required name.

## Content Types

| Type         | Description        | Example                                                       |
|--------------|--------------------|---------------------------------------------------------------|
| `text`       | Text content       | `{"type": "text", "text": "Hello", "color": "white"}`         |
| `item`       | Item icon          | `{"type": "item", "item": "minecraft:diamond", "size": 32}`   |
| `block`      | 3D block           | `{"type": "block", "block": "minecraft:stone", "size": 48}`   |
| `entity`     | 3D entity          | `{"type": "entity", "entity": "minecraft:wolf", "size": 48}`  |
| `progress`   | Progress bar       | `{"type": "progress", "progress": 0.75, "width": 100}`        |
| `carousel`   | Carousel container | `{"type": "carousel", "intervalSeconds": 3, "frames": [...]}` |
| `typewriter` | Typewriter effect  | `{"type": "typewriter", "lines": ["Typing..."]}`              |
| `atlas`      | Texture rendering  | `{"type": "atlas", "item": "minecraft:apple", "size": 32}`    |
| `image`      | Image              | `{"type": "image", "texture": "mymod:gui/icon.png"}`          |
| `chart`      | Chart              | `{"type": "chart", "chartType": "bar", "entries": [...]}`     |
| `vbox`       | Vertical layout    | `{"type": "vbox", "gap": 4, "children": [...]}`               |
| `hbox`       | Horizontal layout  | `{"type": "hbox", "gap": 8, "children": [...]}`               |
| `divider`    | Divider line       | `{"type": "divider", "color": "#555555"}`                     |
| `spacer`     | Spacing            | `{"type": "spacer", "height": 8}`                             |

### Content Type Details

#### text
Text content with rich styling options.
```json
{
  "type": "text",
  "text": "Hello World",
  "color": "#55FFFF",
  "bold": true,
  "align": "center"
}
```

#### item
Render item icon in tooltip.
```json
{
  "type": "item",
  "item": "minecraft:diamond",
  "size": 32,
  "offsetY": 4
}
```

#### block
Render 3D block model with auto-rotation.
```json
{
  "type": "block",
  "block": "minecraft:chest",
  "size": 48,
  "autoRotate": true
}
```

#### entity
Render 3D entity model with auto-rotation.
```json
{
  "type": "entity",
  "entity": "minecraft:wolf",
  "size": 48,
  "autoRotate": true,
  "offsetY": 8
}
```

#### progress
Progress bar with multiple styles.
```json
{
  "type": "progress",
  "progress": 0.75,
  "width": 100,
  "height": 8,
  "style": "gradient",
  "showLabel": true,
  "label": "75%",
  "labelAlign": "right"
}
```

#### carousel
Auto-switching multi-frame display.
```json
{
  "type": "carousel",
  "intervalSeconds": 3,
  "frames": [
    {"type": "text", "text": "Frame 1"},
    {"type": "text", "text": "Frame 2"}
  ]
}
```

#### typewriter
Animated text reveal effect.
```json
{
  "type": "typewriter",
  "lines": ["Hello", "World"],
  "charsPerSecond": 10,
  "pauseSeconds": 1,
  "loop": false
}
```

Multi-language typewriter:
```json
{
  "type": "typewriter",
  "lines": {
    "zh_cn": ["你好", "世界"],
    "en_us": ["Hello", "World"]
  },
  "bold": true,
  "color": "gold"
}
```

#### atlas
Render texture from atlas (item/block ID auto-converts).
```json
{
  "type": "atlas",
  "item": "minecraft:apple",
  "size": 32
}
```

#### image
Render custom texture image.
```json
{
  "type": "image",
  "texture": "mymod:textures/gui/icon.png",
  "width": 32,
  "height": 32
}
```

#### chart
Bar, pie, or line chart with variable support.
```json
{
  "type": "chart",
  "chartType": "bar",
  "width": 100,
  "height": 60,
  "entries": [
    {"label": "X", "value": "{player_x}", "color": "#FF5555"},
    {"label": "Y", "value": "{player_y}", "color": "#55FF55"}
  ]
}
```

#### vbox / hbox
Vertical/horizontal layout containers.
```json
{
  "type": "vbox",
  "gap": 4,
  "children": [...]
}
```

#### divider
Divider line with multiple styles.
```json
{
  "type": "divider",
  "color": "#555555",
  "style": "dashed"
}
```

#### spacer
Empty spacing between content.
```json
{
  "type": "spacer",
  "height": 8
}
```

## Text Properties

| Property        | Type          | Default | Description                                             |
|-----------------|---------------|---------|---------------------------------------------------------|
| `text`          | String/Object | -       | Text content (supports multi-language object)           |
| `color`         | String        | "white" | Color (named or hex, supports expressions)              |
| `font`          | String        | -       | Custom font (e.g. `minecraft:alt`, `minecraft:uniform`) |
| `align`         | String        | "left"  | Alignment: `left`, `center`, `right`                    |
| `bold`          | boolean       | false   | Bold text                                               |
| `italic`        | boolean       | false   | Italic text                                             |
| `underlined`    | boolean       | false   | Underlined text                                         |
| `strikethrough` | boolean       | false   | Strikethrough text                                      |
| `shift`         | boolean       | false   | Only show when holding Shift                            |
| `maxWidth`      | int           | 0       | Maximum width (0=no wrap)                               |

By default, DataTip does not impose a global line width. Text is measured at its natural resolved width,
while Minecraft's tooltip renderer controls final positioning and background bounds. DataTip only wraps
when a text entry sets `maxWidth` or `max_width` is greater than zero. Variables are resolved before
measurement, so long item names and NBT values use their real rendered size.

## Progress Bar Properties

| Property     | Type          | Default    | Description                                        |
|--------------|---------------|------------|----------------------------------------------------|
| `progress`   | float         | 0.0        | Progress value (0.0-1.0)                           |
| `width`      | int           | 100        | Width                                              |
| `height`     | int           | 8          | Height                                             |
| `colorFg`    | String        | "#55FF55"  | Foreground color                                   |
| `colorBg`    | String        | "#333333"  | Background color                                   |
| `style`      | String        | "gradient" | Style: `flat`, `gradient`, `segmented`, `animated` |
| `showLabel`  | boolean       | false      | Show label                                         |
| `label`      | String/Object | -          | Custom label text (supports localization)          |
| `labelAlign` | String        | "left"     | Label alignment: `left`, `center`, `right`         |

## Divider Properties

| Property    | Type   | Default   | Description                        |
|-------------|--------|-----------|------------------------------------|
| `color`     | String | "#555555" | Color                              |
| `style`     | String | "solid"   | Style: `solid`, `dashed`, `dotted` |
| `width`     | int    | 0         | Width (0=fill)                     |
| `widthMode` | String | "fill"    | Mode: `fill`, `fixed`, `centered`  |

## Carousel Properties

| Property          | Type   | Default | Description                         |
|-------------------|--------|---------|-------------------------------------|
| `frames`          | Array  | -       | Content frames array                |
| `intervalSeconds` | int    | 3       | Frame switch interval (max 86400s)  |
| `transition`      | String | "fade"  | Transition: `none`, `fade`, `slide` |

## Typewriter Properties

| Property         | Type         | Default | Description                                                                           |
|------------------|--------------|---------|---------------------------------------------------------------------------------------|
| `lines`          | Array/Object | -       | Text lines array (supports multi-language object)                                     |
| `charsPerSecond` | int          | 2       | Characters per second                                                                 |
| `pauseSeconds`   | int          | 1       | Pause between lines (seconds)                                                         |
| `loop`           | boolean      | false   | Replay on re-hover/expand (`true` = replay from start each time, `false` = play once) |
| `color`          | String       | "white" | Text color                                                                            |
| `font`           | String       | -       | Custom font                                                                           |
| `bold`           | boolean      | false   | Bold text                                                                             |
| `italic`         | boolean      | false   | Italic text                                                                           |
| `underlined`     | boolean      | false   | Underlined text                                                                       |
| `strikethrough`  | boolean      | false   | Strikethrough text                                                                    |
| `align`          | String       | "left"  | Alignment: `left`, `center`, `right`                                                  |
| `shadow`         | boolean      | true    | Text shadow                                                                           |
| `lineHeight`     | int          | 9       | Line height in pixels, defaulting to the vanilla font line height                     |
| `shift`          | boolean      | false   | Only show when holding Shift                                                          |

## Chart Properties

| Property        | Type          | Default   | Description                             |
|-----------------|---------------|-----------|-----------------------------------------|
| `chartType`     | String        | "bar"     | Chart type: `bar`, `pie`, `line`        |
| `width`         | int           | 100       | Width (pie charts: max 128)             |
| `height`        | int           | 60        | Height (pie charts: max 128)            |
| `title`         | String/Object | -         | Title (supports localization)           |
| `entries`       | Array         | -         | Data entries array                      |
| `titleColor`    | String        | "#FFFFFF" | Title color                             |
| `labelColor`    | String        | "#AAAAAA" | Label color                             |
| `valueColor`    | String        | "#FFFFFF" | Value color                             |
| `zeroLineColor` | String        | "#888888" | Zero line color (for positive/negative) |
| `showLabels`    | boolean       | true      | Show data labels                        |
| `showValues`    | boolean       | true      | Show chart values                       |

Chart entry `value` accepts a number, numeric string, or variable expression. `valueExpr` is an equivalent
explicit field. Entry `label` accepts the same localized value as `title`. Labels that cannot fit their slot
are hidden, while chart geometry remains inside its width.

## Entity/Item/Block/Atlas/Image Common Properties

| Property  | Type          | Default | Description                                   |
|-----------|---------------|---------|-----------------------------------------------|
| `label`   | String/Object | -       | Optional localized label (except `image`)     |
| `offsetX` | int           | 0       | X-axis offset (positive=right, negative=left) |
| `offsetY` | int           | 0       | Y-axis offset (positive=down, negative=up)    |

## Variables

| Variable               | Description                                     |
|------------------------|-------------------------------------------------|
| `{durability}`         | Current durability                              |
| `{max_durability}`     | Maximum durability                              |
| `{damage}`             | Damage value                                    |
| `{durability_percent}` | Durability percentage                           |
| `{durability_bar}`     | Durability bar (visual)                         |
| `{count}`              | Item count                                      |
| `{item_name}`          | Item name                                       |
| `{item_id}`            | Item ID                                         |
| `{enchantment_count}`  | Enchantment count                               |
| `{is_enchanted}`       | Is enchanted                                    |
| `{rarity}`             | Rarity                                          |
| `{max_stack_size}`     | Max stack size                                  |
| `{is_stackable}`       | Is stackable                                    |
| `{is_damageable}`      | Is damageable                                   |
| `{player_health}`      | Player health                                   |
| `{player_max_health}`  | Player max health                               |
| `{player_hunger}`      | Player hunger                                   |
| `{player_experience}`  | Player experience level                         |
| `{player_x}`           | Player X coordinate                             |
| `{player_y}`           | Player Y coordinate                             |
| `{player_z}`           | Player Z coordinate                             |
| `{game_time}`          | Game time                                       |
| `{is_day}`             | Is daytime                                      |
| `{is_raining}`         | Is raining                                      |
| `{is_thundering}`      | Is thundering                                   |
| `{health_bar}`         | Health bar (visual)                             |
| `{nbt:path}`           | Item NBT value (see NBT Variable section below) |

### NBT Variable

Forge 1.20.1 stores item custom data in NBT. Use `{nbt:path}` to read a dot-separated NBT path from the
current item stack.

```json
{
  "type": "text",
  "text": "Name: {nbt:display.Name}",
  "color": "white"
}
```

**Supported paths:**
- `display.Name` - Custom item name
- `display.Lore[0]` - First lore line
- `Damage` - Damage value
- `RepairCost` - Repair cost
- `Enchantments[0].id` - First enchantment id
- `Count` - Item stack count (synthetic value)

## Conditions

| Condition    | Description                     | Example                                           |
|--------------|---------------------------------|---------------------------------------------------|
| `dimension`  | Dimension                       | `"dimension": "minecraft:the_nether"`             |
| `biome`      | Biome                           | `"biome": "minecraft:desert"`                     |
| `holding`    | Held item                       | `"holding": "minecraft:diamond_sword"`            |
| `sneaking`   | Is sneaking                     | `"sneaking": true`                                |
| `creative`   | Creative mode                   | `"creative": true`                                |
| `survival`   | Survival mode                   | `"survival": true`                                |
| `health`     | Health                          | `"health": "50%"` or `"health": 10`               |
| `hunger`     | Hunger                          | `"hunger": 15`                                    |
| `experience` | Experience level                | `"experience": 30`                                |
| `time`       | Time                            | `"time": "day"` or `"time": 6000`                 |
| `weather`    | Weather                         | `"weather": "rain"`                               |
| `light`      | Light level                     | `"light": "dark"` or `"light": 8`                 |
| `altitude`   | Altitude                        | `"altitude": ">=64"`                              |
| `enchanted`  | Is enchanted                    | `"enchanted": true`                               |
| `damage`     | Damage value                    | `"damage": 100`                                   |
| `count`      | Item count                      | `"count": 16`                                     |
| `nbt`        | NBT path exists or values match | `"nbt": "display.Name"` or `"nbt": {"Damage": 0}` |
| `item_tag`   | Item tag                        | `"item_tag": "minecraft:swords"`                  |

Boolean conditions support both `true` and `false`. Unknown `time`, `weather`, and `light` values fail
closed so a typo cannot accidentally enable content.

### NBT Condition

Check if an item has an NBT path, or if a set of NBT paths matches expected values.

```json
{
  "conditions": {
    "nbt": "display.Name"
  }
}
```

```json
{
  "conditions": {
    "nbt": {
      "Damage": 0,
      "RepairCost": 0
    }
  }
}
```

### Item Tag Condition

Check if item belongs to a specific tag.
```json
{
  "conditions": {
    "item_tag": "minecraft:swords"
  }
}
```

## Expressions

Supports expressions in text and color:

```json
{
  "type": "text",
  "text": "Status: {durability > 100 ? 'Good' : 'Needs repair'}",
  "color": "{durability > 100 ? 'green' : 'red'}"
}
```

Supported operators:
- Comparison: `>`, `<`, `==`, `!=`, `>=`, `<=`
- Logic: `&&`, `||`, `!`
- Arithmetic: `+`, `-`, `*`, `/`
- Ternary: `condition ? true_value : false_value`

## Multi-language

All player-visible JSON text supports localization: `text.text`, `typewriter.lines`, `progress.label`,
`chart.title`, `chart.entries[].label`, and the `label` field of `item`, `block`, `entity`, and `atlas`.
Containers and carousel frames inherit this behavior from their child content.

Use a language-code map when the wording is owned by the resource pack:

```json
{
    "type": "progress",
    "progress": 0.75,
    "showLabel": true,
    "label": {
        "zh_cn": "资源完整度 75%",
        "en_us": "Resource integrity 75%"
    }
}
```

Single-value `label`/`title` fields may also use styled language entries, or reference a translation key supplied
by Minecraft or a mod:

```json
{
    "type": "chart",
    "title": {
        "zh_cn": {
            "text": "村民交易指数",
            "color": "gold",
            "bold": true
        },
        "en_us": {
            "text": "Villager Trade Index",
            "color": "gold",
            "bold": true
        }
  },
    "entries": [
        {
            "label": {
                "trans": "entity.minecraft.villager"
            },
            "value": 12
        }
    ]
}
```

Language selection follows the active Minecraft language. If an exact entry is absent, DataTip falls back to
`en_us`, then to the first language entry, so localized content does not disappear after a language switch.

## Special Properties

| Property     | Description                                                                                    |
|--------------|------------------------------------------------------------------------------------------------|
| `align`      | Alignment: `left`, `center`, `right` (works on all content types)                              |
| `shift`      | Only show when holding Shift. Multiple `shift: true` items in a container merge into one hint. |
| `prepend`    | Show after item name (before original content)                                                 |
| `conditions` | Conditions configuration                                                                       |

## Colors

### Named Colors

| Color               | Hex       |
|---------------------|-----------|
| black               | #000000   |
| dark_blue           | #0000AA   |
| dark_green          | #00AA00   |
| dark_aqua           | #00AAAA   |
| dark_red            | #AA0000   |
| dark_purple         | #AA00AA   |
| gold                | #FFAA00   |
| gray/grey           | #AAAAAA   |
| dark_gray/dark_grey | #555555   |
| blue                | #5555FF   |
| green               | #55FF55   |
| aqua                | #55FFFF   |
| red                 | #FF5555   |
| light_purple        | #FF55FF   |
| yellow              | #FFFF55   |
| white               | #FFFFFF   |

### Hex Colors

Any 6-digit hex with `#` prefix: `"#FF6600"`, `"#AABBCC"`, `"#00FF00"`

## JSON Schema

`datatip.schema.json` is an editor helper for DataTip resource-pack JSON files. It provides completion,
type hints, and basic validation in editors such as IntelliJ IDEA or VS Code. It is not required at
runtime, and changing it does not change how DataTip loads tooltips in-game.

DataTip automatically exports the schema on client startup. The schema is synchronized with the runtime
parser and covers modern entries, legacy-compatible forms, wildcards, styled localized text, conditions,
and every built-in content field:

```text
.minecraft/datatip.schema.json
```

Recommended IntelliJ IDEA setup:

1. Start the game once so DataTip exports `.minecraft/datatip.schema.json`.
2. Open `Settings | Languages & Frameworks | Schemas and DTDs | JSON Schema Mappings`.
3. Add a new mapping and select `.minecraft/datatip.schema.json`.
4. Map it to the whole DataTip directory, for example `assets/*/datatip/`, including JSON files below it.

With this setup, resource-pack authors do not need to edit every JSON file or open the mod jar.

Optional per-resource-pack setup:

```text
MyResourcePack/
  datatip.schema.json
  assets/
    minecraft/
      datatip/
        showcase.json
        equipment/
          weapons.json
```

If you copy `.minecraft/datatip.schema.json` to the resource-pack root, you can add this to
any DataTip JSON file, for example `assets/<namespace>/datatip/showcase.json`:

```json
{
  "$schema": "../../../datatip.schema.json",
  "minecraft:diamond": {
    "type": "text",
      "text": "Schema enabled"
  }
}
```

For mod development files under `src/main/resources/assets/<namespace>/datatip/`, either use the IDEA
mapping above or adjust the `$schema` path to your project layout.

## Configuration

File: `config/datatip-common.toml`

| Option                | Type    | Default    | Description                                      |
|-----------------------|---------|------------|--------------------------------------------------|
| `enabled`             | boolean | true       | Enable/disable DataTip                           |
| `default_color`       | int     | 0xFFAAAAAA | Default text color                               |
| `default_line_height` | int     | 0          | Default line height (0=vanilla font line height) |
| `max_width`           | int     | 0          | DataTip width override (0=natural content width) |
| `shift_hint_color`    | int     | 0xFF888888 | Shift hint text color (ARGB format)              |

## Legacy Format Support

Old format is automatically converted to new format. The converted JSON is saved to `.minecraft/datatip_converted/` directory.

**Supported old format:**

```json
{
  "minecraft:diamond": ["Line 1", "Line 2"],
  "minecraft:diamond_sword": {
    "text": {"zh_cn": ["Sharp"], "en_us": ["Sharp"]},
    "color": "gold",
    "shift": true
  }
}
```

**Converted output:**
```
.minecraft/datatip_converted/
  └── confluence/
      └── datatip.json
```

## Complete Example

```json
{
  "minecraft:diamond": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Diamond", "color": "#55FFFF", "align": "center"},
      {"type": "divider", "color": "#555555", "widthMode": "centered", "width": 80},
      {"type": "text", "text": "A precious gem", "color": "gray", "align": "center"},
      {"type": "spacer", "height": 4},
      {"type": "text", "text": "Left aligned", "color": "white", "align": "left"},
      {"type": "text", "text": "Centered", "color": "gold", "align": "center"},
      {"type": "text", "text": "Right aligned", "color": "aqua", "align": "right"}
    ]
  },

  "minecraft:diamond_sword": {
    "type": "hbox",
    "gap": 8,
    "children": [
      {"type": "item", "item": "minecraft:diamond_sword", "size": 32},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "Diamond Sword", "color": "aqua", "align": "center"},
        {"type": "text", "text": "Durability: {durability}/{max_durability}", "color": "gray"},
        {"type": "text", "text": "Percent: {durability_percent}%", "color": "gold"}
      ]}
    ]
  },

  "minecraft:diamond_pickaxe": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Diamond Pickaxe", "color": "aqua", "align": "center"},
      {"type": "progress", "progress": 0.75, "width": 100, "height": 6, "colorFg": "#55FF55", "showLabel": true, "label": "75%", "labelAlign": "right"},
      {"type": "progress", "progress": 0.5, "width": 100, "height": 8, "style": "segmented"},
      {"type": "progress", "progress": 0.9, "width": 100, "height": 6, "colorFg": "#FFD700", "animated": true, "animSpeed": 3}
    ]
  },

  "minecraft:golden_apple": {
    "type": "carousel",
    "intervalSeconds": 10,
    "frames": [
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "Golden Apple", "color": "gold", "align": "center"},
        {"type": "text", "text": "Restores health", "color": "red"}
      ]},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "Golden Apple", "color": "gold", "align": "center"},
        {"type": "text", "text": "Restores health", "color": "red"}
      ]},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "Golden Apple", "color": "gold", "align": "center"},
        {"type": "text", "text": "Restores health", "color": "red"}
      ]}
    ]
  },

  "minecraft:nether_star": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Nether Star", "color": "light_purple", "align": "center"},
      {"type": "typewriter", "lines": ["Boss drop", "Used for beacon", "Rare item"], "charsPerSecond": 10, "pauseSeconds": 1, "color": "gray"}
    ]
  },

  "minecraft:stone": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Stone", "color": "gray"},
      {"type": "divider", "color": "#555555", "style": "solid"},
      {"type": "text", "text": "Above solid line", "color": "white"},
      {"type": "divider", "color": "#555555", "style": "dashed"},
      {"type": "text", "text": "Above dashed line", "color": "white"},
      {"type": "divider", "color": "#555555", "style": "dotted"},
      {"type": "text", "text": "Above dotted line", "color": "white"}
    ]
  },

  "minecraft:bow": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Durability: {durability}/{max_durability}", "color": "gray"},
      {"type": "text", "text": "Percent: {durability_percent}%", "color": "gold"},
      {"type": "text", "text": "Durability bar: {durability_bar}", "color": "green"},
      {"type": "text", "text": "Health bar: {health_bar}", "color": "red"}
    ]
  },

  "minecraft:iron_ingot": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Iron Ingot", "color": "white", "align": "center"},
      {"type": "divider", "color": "#555555", "widthMode": "centered", "width": 60},
      {"type": "text", "text": "Health: {player_health}/{player_max_health}", "color": "red"},
      {"type": "text", "text": "Hunger: {player_hunger}", "color": "gold"},
      {"type": "text", "text": "Experience: {player_experience}", "color": "green"}
    ]
  },

  "minecraft:clock": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Time: {game_time}", "color": "gold"},
      {"type": "text", "text": "Daytime: {is_day}", "color": "yellow"},
      {"type": "text", "text": "Raining: {is_raining}", "color": "aqua"},
      {"type": "text", "text": "Thundering: {is_thundering}", "color": "red"}
    ]
  },

  "#minecraft:swords": {
    "type": "text",
    "text": "All swords", "color": "yellow"
  },

  "minecraft:diamond_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Diamond Block", "color": "aqua", "align": "center"},
      {"type": "text", "text": "Only in Nether", "color": "dark_red"}
    ],
    "conditions": {
      "dimension": "minecraft:the_nether"
    }
  },

  "minecraft:emerald_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Emerald Block", "color": "green"},
      {"type": "text", "text": "Hold Shift to see this", "color": "gray", "shift": true}
    ]
  },

  "minecraft:gold_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Gold Block", "color": "gold"},
      {"type": "text", "text": "Hold Shift for full tooltip", "color": "gray"},
      {"type": "text", "text": "All content will be folded", "color": "yellow"}
    ],
    "shift": true
  },

  "minecraft:iron_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Iron Block", "color": "white"},
      {"type": "text", "text": "This shows after item name", "color": "gray"}
    ],
    "prepend": true
  },

  "minecraft:enchanted_golden_apple": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Enchanted Golden Apple", "color": "gold", "bold": true, "align": "center"},
      {"type": "divider", "color": "#FFD700", "widthMode": "centered", "width": 80},
      {"type": "hbox", "gap": 8, "children": [
        {"type": "item", "item": "minecraft:enchanted_golden_apple", "size": 32},
        {"type": "vbox", "gap": 2, "children": [
          {"type": "text", "text": "Rare food", "color": "light_purple"},
          {"type": "progress", "progress": 1.0, "width": 80, "height": 6, "colorFg": "#FFD700", "showLabel": true, "label": "Full", "labelAlign": "left"}
        ]}
      ]},
      {"type": "text", "text": "Durability bar: {durability_bar}", "color": "gray"},
      {"type": "text", "text": "Health bar: {health_bar}", "color": "red"}
    ]
  },

  "minecraft:wolf_spawn_egg": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Wolf Spawn Egg", "color": "white", "align": "center"},
      {"type": "entity", "entity": "minecraft:wolf", "size": 48, "rotationSpeed": 1.0, "autoRotate": true},
      {"type": "text", "text": "Can be tamed", "color": "gray"}
    ]
  },

  "minecraft:crafting_table": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Crafting Table", "color": "white", "align": "center"},
      {"type": "block", "block": "minecraft:crafting_table", "size": 48, "rotationSpeed": 0.5, "autoRotate": true},
      {"type": "text", "text": "Used for crafting", "color": "gray"}
    ]
  },

  "minecraft:apple": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Apple", "color": "red", "align": "center"},
      {"type": "atlas", "item": "minecraft:apple", "size": 32},
      {"type": "text", "text": "Restores hunger", "color": "gray"}
    ]
  },

  "minecraft:red_concrete": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Red Concrete", "color": "red", "align": "center"},
      {"type": "atlas", "block": "minecraft:red_concrete", "size": 32},
      {"type": "text", "text": "Decorative block", "color": "gray"}
    ]
  },

  "minecraft:iron_sword": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Iron Sword", "color": "white", "align": "center"},
      {"type": "text", "text": "Status: {durability > 100 ? 'Good' : 'Needs repair'}", "color": "{durability > 100 ? 'green' : 'red'}"},
      {"type": "text", "text": "Durability: {durability}/{max_durability} ({durability_percent}%)", "color": "gray"}
    ]
  },

  "minecraft:ender_pearl": {
    "type": "text",
    "text": {
      "zh_cn": "末影珍珠 - 可用于传送",
      "en_us": "Ender Pearl - Can be used for teleportation"
    },
    "color": "#00AAAA"
  },

  "minecraft:coal": {
    "type": "vbox",
    "gap": 0,
    "children": [
      {"type": "text", "text": "Coal", "color": "gray"},
      {"type": "spacer", "height": 8},
      {"type": "text", "text": "8px spacer above", "color": "white"}
    ]
  },

  "minecraft:emerald": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Bold text", "color": "green", "bold": true},
      {"type": "text", "text": "Italic text", "color": "green", "italic": true},
      {"type": "text", "text": "Underlined text", "color": "green", "underlined": true},
      {"type": "text", "text": "Strikethrough text", "color": "green", "strikethrough": true}
    ]
  },

  "minecraft:compass": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Compass", "color": "#FF5555", "bold": true},
      {"type": "text", "text": "Position: {player_x}, {player_y}, {player_z}", "color": "white"},
      {"type": "chart", "chartType": "bar", "width": 100, "height": 60,
       "entries": [
         {"label": "X", "value": "{player_x}", "color": "#FF5555"},
         {"label": "Y", "value": "{player_y}", "color": "#55FF55"},
         {"label": "Z", "value": "{player_z}", "color": "#5555FF"}
       ]
      }
    ]
  },

  "minecraft:redstone": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Redstone", "color": "#FF0000", "bold": true},
      {"type": "chart", "chartType": "pie", "width": 80,
       "entries": [
         {"label": "Redstone Dust", "value": 60, "color": "#FF0000"},
         {"label": "Redstone Torch", "value": 25, "color": "#FF5555"},
         {"label": "Redstone Repeater", "value": 15, "color": "#FFAAAA"}
       ]
      }
    ]
  },

  "minecraft:wheat_seeds": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Wheat Seeds", "color": "#55AA55", "bold": true},
      {"type": "chart", "chartType": "line", "width": 100, "height": 60,
       "entries": [
         {"label": "1", "value": 10, "color": "#55AA55"},
         {"label": "2", "value": 25, "color": "#55AA55"},
         {"label": "3", "value": 45, "color": "#55AA55"},
         {"label": "4", "value": 40, "color": "#55AA55"},
         {"label": "5", "value": 60, "color": "#55AA55"}
       ]
      }
    ]
  },

  "minecraft:painting": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "Painting", "color": "#AAAAAA", "bold": true},
      {"type": "entity", "entity": "minecraft:painting", "size": 48, "autoRotate": false, "offsetY": 8},
      {"type": "text", "text": "Decorative item", "color": "gray"}
    ]
  },
    "minecraft:emerald_ore": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Multiple shift items merge into one hint", "color": "green"},
      {"type": "text", "text": "Hold Shift to reveal", "color": "gray", "shift": true},
      {"type": "text", "text": "All folded into one line", "color": "gray", "shift": true},
      {"type": "text", "text": "No more duplicate hints", "color": "gray", "shift": true}
    ]
  },

  "minecraft:enchanted_book": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Typewriter + loop replay on hover", "color": "light_purple", "bold": true},
      {"type": "typewriter", "lines": [
        "Plays from start on each hover",
        "loop:true with gap detection",
        "Try unhovering and re-hovering"
      ], "charsPerSecond": 6, "pauseSeconds": 1, "loop": true, "color": "gray"}
    ]
  },
    "minecraft:netherite_upgrade_smithing_template": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "Typewriter + shift replay on expand", "color": "white", "bold": true},
      {"type": "typewriter", "lines": [
        "Hold Shift to reveal",
        "Replays from start each time",
        "Try releasing and re-pressing Shift"
      ], "charsPerSecond": 8, "pauseSeconds": 1, "loop": true, "color": "gray", "shift": true}
    ]
    },
    "minecraft:amethyst_shard": {
        "type": "vbox",
        "gap": 3,
        "children": [
            {
                "type": "progress",
                "progress": 0.25,
                "width": 150,
                "height": 6,
                "style": "flat",
                "label": "Flat · Left",
                "showLabel": true,
                "labelAlign": "left"
            },
            {
                "type": "progress",
                "progress": 0.5,
                "width": 150,
                "height": 6,
                "style": "gradient",
                "label": {
                    "zh_cn": "渐变 · 居中",
                    "en_us": "Gradient · Center"
                },
                "showLabel": true,
                "labelAlign": "center"
            },
            {
                "type": "progress",
                "progress": 0.75,
                "width": 150,
                "height": 6,
                "style": "segmented",
                "label": "Segmented · Right",
                "showLabel": true,
                "labelAlign": "right"
            },
            {
                "type": "progress",
                "progress": 0.9,
                "width": 150,
                "height": 6,
                "style": "animated",
                "animated": true,
                "animSpeed": 2,
                "label": "Animated",
                "showLabel": true,
                "labelAlign": "center"
            }
        ]
    },
    "minecraft:copper_ingot": {
        "type": "carousel",
        "intervalSeconds": 4,
        "transition": "fade",
        "frames": [
            {
                "type": "text",
                "text": {
                    "zh_cn": "淡入淡出",
                    "en_us": "Fade"
                }
            },
            {
                "type": "item",
                "item": "minecraft:copper_ingot",
                "size": 28
            },
            {
                "type": "block",
                "block": "minecraft:copper_block",
                "size": 34,
                "autoRotate": true
            }
        ]
    },
    "minecraft:echo_shard": {
        "type": "carousel",
        "intervalSeconds": 4,
        "transition": "slide",
        "frames": [
            {
                "type": "entity",
                "entity": "minecraft:pig",
                "size": 38,
                "autoRotate": true
            },
            {
                "type": "atlas",
                "item": "minecraft:echo_shard",
                "size": 32
            }
        ]
    },
    "minecraft:recovery_compass": {
        "type": "carousel",
        "intervalSeconds": 4,
        "transition": "none",
        "frames": [
            {
                "type": "text",
                "text": "Frame A"
            },
            {
                "type": "text",
                "text": "Frame B"
            }
        ]
    },
    "minecraft:filled_map": {
        "type": "image",
        "texture": "minecraft:textures/map/map_background.png",
        "width": 64,
        "height": 64,
        "u": 0,
        "v": 0,
        "textureWidth": 128,
        "textureHeight": 128,
        "scale": 0.75,
        "offsetX": 2,
        "offsetY": 2
    },
    "minecraft:name_tag": {
        "type": "vbox",
        "gap": 2,
        "prepend": true,
        "children": [
            {
                "type": "text",
                "text": "Name: {nbt:display.Name}",
                "color": "white"
            },
            {
                "type": "text",
                "text": "Damage: {nbt:Damage}/{max_durability}",
                "color": "gray"
            },
            {
                "type": "text",
                "text": "RepairCost: {nbt:RepairCost}",
                "color": "gold"
            }
        ]
  }
}
```

Datagen also writes `10_all_conditions.json`, covering every 1.20.1 condition and its common value forms:

```json
{
    "minecraft:grass_block": {
        "type": "text",
        "text": "dimension",
        "conditions": {
            "dimension": "minecraft:overworld"
        }
    },
    "minecraft:oak_sapling": {
        "type": "text",
        "text": "biome array",
        "conditions": {
            "biome": [
                "minecraft:plains",
                "minecraft:forest"
            ]
        }
    },
    "minecraft:map": {
        "type": "text",
        "text": "holding array",
        "conditions": {
            "holding": [
                "minecraft:compass",
                "minecraft:recovery_compass"
            ]
        }
    },
    "minecraft:feather": {
        "type": "text",
        "text": "sneaking true",
        "conditions": {
            "sneaking": true
        }
    },
    "minecraft:flint": {
        "type": "text",
        "text": "sneaking false",
        "conditions": {
            "sneaking": false
        }
    },
    "minecraft:command_block": {
        "type": "text",
        "text": "creative",
        "conditions": {
            "creative": true
        }
    },
    "minecraft:wooden_sword": {
        "type": "text",
        "text": "survival",
        "conditions": {
            "survival": true
        }
    },
    "minecraft:golden_apple": {
        "type": "text",
        "text": "health",
        "conditions": {
            "health": "50%"
        }
    },
    "minecraft:bread": {
        "type": "text",
        "text": "hunger",
        "conditions": {
            "hunger": 15
        }
    },
    "minecraft:experience_bottle": {
        "type": "text",
        "text": "experience",
        "conditions": {
            "experience": 5
        }
    },
    "minecraft:bookshelf": {
        "type": "text",
        "text": "level",
        "conditions": {
            "level": 5
        }
    },
    "minecraft:sunflower": {
        "type": "text",
        "text": "time",
        "conditions": {
            "time": "day"
        }
    },
    "minecraft:clock": {
        "type": "text",
        "text": "time number",
        "conditions": {
            "time": 6000
        }
    },
    "minecraft:lightning_rod": {
        "type": "text",
        "text": "weather",
        "conditions": {
            "weather": "clear"
        }
    },
    "minecraft:torch": {
        "type": "text",
        "text": "light",
        "conditions": {
            "light": "bright"
        }
    },
    "minecraft:glowstone": {
        "type": "text",
        "text": "light number",
        "conditions": {
            "light": 8
        }
    },
    "minecraft:scaffolding": {
        "type": "text",
        "text": "altitude",
        "conditions": {
            "altitude": ">=64"
        }
    },
    "minecraft:stick": {
        "type": "text",
        "text": "enchanted",
        "conditions": {
            "enchanted": false
        }
    },
    "minecraft:diamond_pickaxe": {
        "type": "text",
        "text": "damage",
        "conditions": {
            "damage": 100
        }
    },
    "minecraft:cobblestone": {
        "type": "text",
        "text": "count",
        "conditions": {
            "count": 16
        }
    },
    "minecraft:name_tag": {
        "type": "text",
        "text": "nbt path",
        "conditions": {
            "nbt": "display.Name"
        }
    },
    "minecraft:paper": {
        "type": "text",
        "text": "nbt values",
        "conditions": {
            "nbt": {
                "Damage": 0,
                "RepairCost": 0
            }
        }
    },
    "minecraft:iron_sword": {
        "type": "text",
        "text": "item_tag",
        "conditions": {
            "item_tag": "minecraft:swords"
        }
    },
    "minecraft:diamond_block": {
        "type": "text",
        "text": "combined",
        "conditions": {
            "dimension": "minecraft:overworld",
            "creative": false,
            "health": 1,
            "count": 1
        }
    }
}
```

## For Mod Developers

### Datagen and TipContentBuilder

Running client datagen makes `DatatipDataGen` register `ExampleTooltipProvider`, which generates `00_showcase.json` for
every content,
layout, localization, and animation combination, plus `10_all_conditions.json` for every 1.20.1 condition.
All example content is constructed through `TipContentBuilder`:

```java
import static com.cooobird.datatip.datagen.TipContentBuilder.*;

root.add("minecraft:clock",toJson(carousel(4, "fade",
         langText(languages("淡入淡出文本", "Fade text"), "gold"),

item("minecraft:clock",1,28,false,false,true,
     languages("物品帧", "Item frame"), "#FFAA00",0,0),

image("minecraft:textures/item/clock_00.png",16,16,
          0,0,16,16,1.5f,0,0)
)));

    root.

add("minecraft:name_tag",entry(
    text("Requires custom-name NBT", "yellow"),
    Map.

of("nbt","display.Name"), false,true
    ));
```

See `ExampleShowcaseTooltips` and `ExampleConditionTooltips` for complete Builder coverage.

### Register Runtime Tooltip Content

Runtime content is not written to resource packs, but it is displayed together with JSON-loaded DataTip content.

```java
TipRuntimeContentRegistry.register(
    "minecraft:diamond",
    TextContent.of("DataTip registered from code")
);

    TipRuntimeContentRegistry.

register(
    "#minecraft:swords",
        new TipContentEntry(TextContent.of("Extra sword tooltip"),List.

of(), false,true)
    );
```

Runtime content can also be removed by namespace:

```java
TipRuntimeContentRegistry.clearNamespace("mymod");
```

### Register Custom Variables
```java
VariableResolver.registerVariable("my_var", stack -> "custom_value");
```

### Register Custom Conditions
```java
ConditionChecker.registerCondition("my_condition", (value, stack, player, level) -> {
    return player.getAbsorptionAmount() > 0;
});
```

Then use in JSON:
```json
{
  "conditions": {
    "my_condition": true
  }
}
```

### Event Hooks

`onAppendLines` runs during tooltip collection, even when the item has no matching DataTip content.

```java
// Pre-render event (modify or cancel)
TipEventManager.onPreRender(event -> {
    event.setItemStack(customStack);
    // or event.cancel() to cancel rendering
});

// Append extra tooltip lines
    TipEventManager.

onAppendLines(event ->{
    event.

addLine("Extra info from other mod");
});

// Variable resolve event (inject custom variables)
TipEventManager.onResolveVariable(event -> {
    if (event.getVariableName().equals("custom_var")) {
        event.setValue("custom_value");
    }
});
```

## Hot Reload

Press F3+T to reload client resource packs and tooltips without restarting. `/reload` only reloads
server data packs and does not replace a client resource reload.

## License

GNU LGPL 3.0
