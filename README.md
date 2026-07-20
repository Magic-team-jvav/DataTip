# DataTip

[简体中文](README_zh.md)

DataTip is a JSON-driven custom item tooltip system for Minecraft 1.20.1 and Forge 47.4.20. Resource packs can combine
text, item icons, textures, rotating blocks and entities, progress bars, charts, animation, and nested layouts without
writing Java code.

Current project version: `1.2.6-forge`.

## Documentation map

- **Getting started:** [resource-pack setup](#resource-pack-setup),
  [vanilla tooltip integration](#vanilla-tooltip-integration), and [controls](#controls)
- **JSON reference:** [entry properties](#entry-properties), [common modifiers](#common-modifiers),
  [built-in content types](#built-in-content-types), [variables](#variables-and-expressions),
  [conditions](#conditions), and [colors](#colors)
- **Tooling and extension:** [configuration](#configuration), [JSON Schema](#json-schema),
  [legacy conversion](#legacy-conversion), [generated examples](#generated-examples), [Java API](#java-api),
  and the [complete JSON example](#complete-json-example)

## Resource-pack setup

DataTip loads every JSON file below:

```text
assets/<namespace>/datatip/
```

The namespace belongs immediately below `assets`; `datatip` is the reload-listener directory. Filenames and
subdirectories are arbitrary. For example, both of these are valid:

```text
assets/example/datatip/items.json
assets/example/datatip/equipment/weapons.json
```

`datatip.json` is only an example filename. It is not required.

Each file is a JSON object whose keys select items:

| Selector      | Example             | Meaning                                                |
|---------------|---------------------|--------------------------------------------------------|
| Exact item ID | `minecraft:diamond` | Matches one item                                       |
| Item tag      | `#minecraft:swords` | Matches every item in the tag                          |
| Wildcard      | `minecraft:*_sword` | `*` matches any sequence and `?` matches one character |

Tags cannot contain wildcards. Top-level keys beginning with `_`, and `$schema`, are treated as metadata and ignored.

### Minimal example

Save this as `assets/example/datatip/getting_started.json`:

```json
{
  "minecraft:diamond": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {
        "type": "text",
        "translate": "example.tooltip.diamond",
        "color": "aqua",
        "bold": true
      },
      {
        "type": "progress",
        "progress": 0.75,
        "width": 100,
        "label": "75%",
        "labelAlign": "right"
      }
    ]
  }
}
```

Add the translation outside the DataTip JSON, for example in `assets/example/lang/en_us.json`:

```json
{
  "example.tooltip.diamond": "A shiny diamond"
}
```

Reload resource packs with `F3 + T` after editing files.

## Vanilla tooltip integration

DataTip participates in the Minecraft/Forge tooltip pipeline instead of replacing it:

- Minecraft collects tooltip components, wraps ordinary text, chooses the mouse-side position, and draws the background
  and border.
- DataTip measures its prepared content using the same available width that rendering uses.
- `max_width > 0` is forwarded to the tooltip gather event, so vanilla lines and DataTip content share the configured
  width.
- `max_width = 0` keeps the content's natural width until Minecraft's screen-fit rule constrains it. Text is then
  measured and wrapped again at the actual available width.
- Height is limited to the physical screen viewport, including the vanilla tooltip margins and the height used by
  ordinary tooltip components.
- Content taller than the viewport keeps its semantic layout and scrolls inside the visible DataTip region. It does not
  enlarge the background beyond the screen.
- The vanilla background and all four border edges are drawn for the final visible tooltip bounds.

Block and entity nodes render directly through the prepared DataTip draw-command path with scoped render state and
physical scissoring. Text and other 2D nodes use the same measured layout, so measuring, clipping, and drawing agree.

### Controls

Both controls are registered in Minecraft's Controls screen under the DataTip category and can be remapped:

| Action                 | Default    | Use                                                                   |
|------------------------|------------|-----------------------------------------------------------------------|
| Show Detailed Tooltip  | Left Shift | Reveals nodes with `"shift": true`                                    |
| Scroll Tooltip Content | Left Ctrl  | Hold it and use the mouse wheel when the tooltip exceeds the viewport |

The scroll hint substitutes `%s` with the current mapped key name; it is not hard-coded to Ctrl. DataTip shows one
merged Shift hint for a tooltip even when several nodes are folded. Changing Shift visibility preserves the current
scroll session instead of jumping back to the top.

## Entry properties

The object selected by an item key is both the root content node and the entry definition.

| Property     | Default                  | Description                                                                      |
|--------------|--------------------------|----------------------------------------------------------------------------------|
| `type`       | required for modern JSON | Root content type                                                                |
| `prepend`    | `false`                  | Inserts the entry after the item name and before the remaining vanilla lines     |
| `shift`      | `false`                  | Folds only this node and its subtree until the Show Detailed Tooltip key is held |
| `conditions` | `{}`                     | AND-combined conditions for this node                                            |
| `shiftHint`  | unset                    | Replaces the merged Shift hint with full DataTip content                         |
| `scrollHint` | unset                    | Replaces the overflow scroll hint with full DataTip content                      |

`shift` and `conditions` are common modifiers, so they also work on any nested node—not only the root.

Custom hints remain after ordinary DataTip content and may use `divider`, containers, `cycle_text`, or any other node.
Use `{"keybind": "key.datatip.show_tip"}` or `{"keybind": "key.datatip.scroll_tooltip"}` inside hint text to display
the actual mapped key instead of hard-coding Shift or Ctrl. The configured hint colors apply only to generated default
hints; a custom node's own `color`, formatting, and animation settings take precedence.

```json
{
  "minecraft:diamond": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {
        "type": "text",
        "text": "Ordinary content"
      },
      {
        "type": "text",
        "text": "Detailed content",
        "shift": true
      }
    ],
    "shiftHint": {
      "type": "vbox",
      "gap": 1,
      "children": [
        {
          "type": "divider",
          "color": "aqua"
        },
        {
          "type": "text",
          "translate": "tooltip.datatip.hold_shift",
          "with": [
            {
              "keybind": "key.datatip.show_tip"
            }
          ]
        }
      ]
    },
    "scrollHint": {
      "type": "vbox",
      "gap": 1,
      "children": [
        {
          "type": "divider",
          "style": "dashed",
          "color": "gray"
        },
        {
          "type": "text",
          "translate": "tooltip.datatip.scroll_hint",
          "with": [
            {
              "keybind": "key.datatip.scroll_tooltip"
            }
          ],
          "color": "yellow",
          "italic": true
        }
      ]
    }
  }
}
```

## Common modifiers

Every built-in content type supports the same modifier set.

| Property                                                 | Default   | Description                                                                                                    |
|----------------------------------------------------------|-----------|----------------------------------------------------------------------------------------------------------------|
| `offsetX`, `offsetY`                                     | `0`       | Signed 64-bit translation after layout                                                                         |
| `offsetZ`                                                | `0`       | Signed 64-bit sibling layer order                                                                              |
| `selfAlignX`                                             | `inherit` | `inherit`, `left`, `center`, `right`; `selfAlign` is an alias                                                  |
| `selfAlignY`                                             | `inherit` | `inherit`, `top`, `center`, `bottom`                                                                           |
| `margin`                                                 | `0`       | Sets all four margins                                                                                          |
| `marginTop`, `marginRight`, `marginBottom`, `marginLeft` | `margin`  | Per-side signed 64-bit margins                                                                                 |
| `constraints`                                            | none      | Object containing `width`, `height`, `minWidth`, `minHeight`, `maxWidth`, `maxHeight`; values are non-negative |
| `scale`                                                  | `1`       | Uniform scale shorthand                                                                                        |
| `scaleX`, `scaleY`                                       | `scale`   | Axis-specific positive scale                                                                                   |
| `rotation`                                               | `0`       | Finite rotation in degrees                                                                                     |
| `pivotX`, `pivotY`                                       | `0.5`     | Transform pivot from `0` to `1`                                                                                |
| `opacity`                                                | `1`       | Opacity from `0` to `1`                                                                                        |
| `visible`                                                | `true`    | Hides the current node when `false`                                                                            |
| `overflow`                                               | `none`    | `none`, `wrap`, `scale_down` (`scale-down` alias), or `clip`                                                   |
| `shift`                                                  | `false`   | Shows the node only while the configured detail key is held                                                    |
| `conditions`                                             | `{}`      | Conditions that must all pass                                                                                  |

Use the explicit `constraints` object when a content type already owns `width` or `height`:

```json
{
  "type": "text",
  "text": "Movable and transformable",
  "offsetX": 8,
  "offsetY": -3,
  "offsetZ": 20,
  "selfAlignX": "center",
  "margin": 2,
  "constraints": {"minWidth": 80, "maxWidth": 160},
  "scaleX": 1.1,
  "scaleY": 1.1,
  "rotation": -4,
  "pivotX": 0.5,
  "pivotY": 0.5,
  "opacity": 0.9,
  "overflow": "wrap"
}
```

The effective order is: natural measurement → constraints → parent alignment → margins → pivot/scale/rotation → XY
translation → local Z order → overflow and viewport clipping.

### Z order and real overlap

`offsetZ` is a local painter-order key for siblings inside their current parent, not a world-space Z coordinate. Smaller
values draw first; larger values draw later; equal values preserve JSON source order. Entity, block, and item commands
retain normal internal 3D depth while they draw, then isolate that depth at the command boundary. Later text and other
2D content with a higher `offsetZ` therefore stays visible above a lower model. A child still cannot escape an
ancestor's
command group.

Use `stack` when nodes must share the same XY area. Z order only changes content that actually overlaps:

```json
{
  "type": "stack",
  "padding": 2,
  "horizontalAlign": "center",
  "verticalAlign": "center",
  "children": [
    {"type": "entity", "entity": "minecraft:pig", "size": 68, "offsetZ": -10},
    {"type": "text", "text": "Text above the pig", "color": "gold", "bold": true, "offsetZ": 10}
  ]
}
```

## Built-in content types

| Type         | Purpose                                              |
|--------------|------------------------------------------------------|
| `text`       | Literal, language-map, or translation-key text       |
| `cycle_text` | Whole-text color cycling through a palette           |
| `spacer`     | Empty vertical space                                 |
| `divider`    | Solid, dashed, or dotted line                        |
| `item`       | Item stack icon                                      |
| `atlas`      | Flat texture selected from a texture, block, or item |
| `block`      | Rotating 3D block model                              |
| `entity`     | Rotating 3D entity model                             |
| `progress`   | Styled progress bar                                  |
| `vbox`       | Vertical child layout                                |
| `hbox`       | Horizontal child layout                              |
| `stack`      | Shared-XY overlay layout                             |
| `carousel`   | Timed frame container                                |
| `typewriter` | Animated text reveal                                 |
| `image`      | UV region from a texture                             |
| `chart`      | Bar, line, or pie chart                              |

### Text and localization

A `text` or `cycle_text` node must provide exactly one of `text`, `translate`, or `keybind`:

```json
{
    "type": "text",
    "text": "Literal text"
}
```

```json
{
  "type": "text",
  "text": {
    "zh_cn": "中文内容",
    "en_us": "English content"
  }
}
```

```json
{
    "type": "text",
    "translate": "example.tooltip.key"
}
```

```json
{
  "type": "text",
  "keybind": "key.datatip.show_tip"
}
```

Language maps resolve only the active client language. If it is absent, that node is omitted instead of mixing in
`en_us` or the first map entry. Translation keys use ordinary Minecraft language files and can be added without editing
the DataTip JSON. `translate` accepts `with` arguments containing strings, literal components, nested translations, or
keybind components:

```json
{
  "type": "text",
  "translate": "tooltip.datatip.hold_shift",
  "with": [
    {
      "keybind": "key.datatip.show_tip"
    }
  ]
}
```

Both DataTip JSON strings and translations in `assets/<namespace>/lang/*.json` support explicit `\n` line breaks. Write
`\\n` when the visible text must contain the literal characters `\n`. The old `trans` property is not accepted by the
modern parser; the legacy converter rewrites it to `translate`.

| Text property                                   | Default        | Description                                              |
|-------------------------------------------------|----------------|----------------------------------------------------------|
| `color`                                         | config default | Named color or `#RRGGBB`                                 |
| `font`                                          | vanilla font   | Resource location of a font                              |
| `shadow`                                        | `true`         | Font shadow                                              |
| `align`                                         | `left`         | `left`, `center`, `right` within the measured text width |
| `lineHeight`                                    | config/vanilla | Line height; config value `0` means vanilla font height  |
| `maxWidth`                                      | `0`            | Optional node-local wrap width                           |
| `bold`, `italic`, `underlined`, `strikethrough` | `false`        | Text styles                                              |

Visible `label` and `title` properties accept a literal string, a language map, styled language entries, or
`{"translate": "key"}`. Variables are resolved before measurement, so wrapped size and rendering use the same final
text.

`cycle_text` applies one current color to the whole text and cycles through the array in order; it is not a per-glyph
rainbow:

```json
{
  "type": "cycle_text",
  "text": "Whole-text color cycle",
  "colors": [
    "red",
    "#FFAA00",
    "yellow",
    "green",
    "aqua",
    "light_purple"
  ],
  "cycleSeconds": 3,
  "transition": "smooth",
  "phase": 0
}
```

`colors` must contain at least one color. `cycleSeconds` is the full-loop duration, `transition` is `smooth` or `step`,
and `phase` shifts the initial position. Color is calculated only while drawing and does not remeasure text every frame.

### Layout containers

| Type    | Properties                                                  | Defaults                                    |
|---------|-------------------------------------------------------------|---------------------------------------------|
| `vbox`  | `children`, `gap`, `padding`, `align` (`left/center/right`) | `gap: 0`, `padding: 0`, `align: left`       |
| `hbox`  | `children`, `gap`, `padding`, `align` (`top/center/bottom`) | `gap: 0`, `padding: 0`, `align: top`        |
| `stack` | `children`, `padding`, `horizontalAlign`, `verticalAlign`   | `padding: 0`, both alignments at `left/top` |

Container size is derived from the measured children, gaps, padding, margins, constraints, and transforms. Offsets move
the visual result but remain part of the prepared bounds used by the viewport.

### Visual nodes

| Type     | Type-specific properties and defaults                                                                                                                                                                  |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `item`   | `item: minecraft:air`, `count: 1`, `size: 16`, `showCount: true`, `showDurability: true`, `showLabel: false`, optional `label`, `labelColor: #FFFFFF`                                                  |
| `atlas`  | source priority `texture` → `block` → `item`; `width: 16`, `height: width`, optional `size` overriding both, optional `label`                                                                          |
| `block`  | `block: minecraft:stone`, `size: 32`, `rotationSpeed: 0.5`, `autoRotate: true`, optional `label`                                                                                                       |
| `entity` | `entity: minecraft:pig`, `size: 48`, `rotationSpeed: 1.0`, `autoRotate: true`, optional `label`                                                                                                        |
| `image`  | `texture: minecraft:textures/gui/icons.png`, `width: 64`, `height: 64`, `u: 0`, `v: 0`, `textureWidth: width`, `textureHeight: height`; use the common transforms for placement and final-node scaling |

`block` and `entity` reserve their declared square layout size, include the rotating model's visual bounds, and remain
clipped to the final physical tooltip viewport rather than to an unrelated intermediate box.

### Divider, spacer, and progress

| Type       | Properties and defaults                                                                                                                                                                                                                                                      |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spacer`   | `height: 4`                                                                                                                                                                                                                                                                  |
| `divider`  | `color: #555555`, `thickness: 1`, `width: 0`, `marginTop: 2`, `marginBottom: 2`, `style: solid` (`solid/dashed/dotted`), `widthMode: fill` (`fill/fixed/centered`)                                                                                                           |
| `progress` | `progress: 0`, `width: 100`, `height: 8`, `colorFg: #55FF55`, `colorBg: #333333`, optional `colorFgLight`/`colorBgDark`, `style: gradient` (`flat/gradient/segmented/animated`), `showLabel: false`, optional `label`, `animated: false`, `animSpeed: 2`, `labelAlign: left` |

### Carousel and typewriter

```json
{
  "type": "carousel",
  "intervalSeconds": 3,
  "transition": "slide",
  "frames": [
    {"type": "text", "text": "First"},
    {"type": "item", "item": "minecraft:diamond", "size": 24}
  ]
}
```

`carousel` defaults to `intervalSeconds: 3` and `transition: fade`; transitions are `none`, `fade`, and `slide`.

`typewriter` accepts `lines` as a plain array or as a language-code object whose values are arrays. Individual language
lines may be styled objects. Defaults are `charsPerSecond: 2`, `pauseSeconds: 1`, `loop: false`, `shadow: true`, and
`align: left`; it also supports the text color, font, line-height, and style properties.

### Charts

```json
{
  "type": "chart",
  "chartType": "bar",
  "width": 140,
  "height": 70,
  "title": {"translate": "example.chart.title"},
  "showLabels": true,
  "showValues": true,
  "entries": [
    {"label": "Damage", "value": 8, "color": "red"},
    {"label": "Health", "valueExpr": "{player_health}", "color": "green"}
  ]
}
```

`chartType` is `bar`, `line`, or `pie`. Defaults: `width: 100`, `height: 60`, `titleColor: #FFFFFF`,
`labelColor: #AAAAAA`, `valueColor: #FFFFFF`, `zeroLineColor: #888888`, `showLabels: true`, and `showValues: true`.
Entry values accept numbers, numeric strings, variables, and expressions.

## Variables and expressions

Use variables in visible text and numeric expressions with braces, for example
`"Durability: {durability}/{max_durability}"`.

| Group     | Variables                                                                                                                                                                                         |
|-----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Item      | `durability`, `max_durability`, `damage`, `count`, `item_name`, `item_id`, `durability_percent`, `enchantment_count`, `is_enchanted`, `rarity`, `max_stack_size`, `is_stackable`, `is_damageable` |
| Player    | `player_health`, `player_max_health`, `player_hunger`, `player_experience`, `player_x`, `player_y`, `player_z`                                                                                    |
| World     | `game_time`, `is_day`, `is_raining`, `is_thundering`                                                                                                                                              |
| Formatted | `durability_bar`, `health_bar`                                                                                                                                                                    |
| NBT       | `{nbt:path}` reads a dot-separated item NBT path; list indexes use forms such as `display.Lore[0]`                                                                                                |

Synthetic paths `Damage`, `Count`, and `RepairCost` are always available. Other paths read the current stack tag, for
example `{nbt:display.Name}` or `{nbt:display.Lore[0]}`.

Expressions support arithmetic, comparisons, boolean logic, parentheses, and the ternary operator. Examples:

```text
{durability_percent} < 20
{player_health} / {player_max_health} * 100
{is_raining} && {count} >= 16
{is_enchanted} ? 1 : 0
```

## Conditions

All properties inside `conditions` are AND-combined. Conditions can be placed on the root or any nested node.

| Condition                          | Accepted value                                                      |
|------------------------------------|---------------------------------------------------------------------|
| `dimension`                        | Dimension ID                                                        |
| `biome`                            | Biome ID or array of IDs                                            |
| `holding`                          | Item ID or array; checks main hand and offhand                      |
| `sneaking`, `creative`, `survival` | Boolean                                                             |
| `health`                           | Number, comparison string, or percent string                        |
| `hunger`, `experience`, `level`    | Number or comparison string                                         |
| `time`                             | Number, or `day`, `night`, `noon`, `midnight`                       |
| `weather`                          | `clear`, `rain`, `thunder`                                          |
| `light`                            | Number/comparison, or `dark`, `dim`, `bright`, `full`               |
| `altitude`                         | Number or comparison using `>`, `>=`, `<`, `<=`                     |
| `enchanted`                        | Boolean                                                             |
| `damage`                           | Maximum accepted current item damage                                |
| `count`                            | Minimum accepted stack count                                        |
| `nbt`                              | NBT path that must exist, or an object of paths and expected values |
| `item_tag`                         | Item tag ID                                                         |

```json
{
  "type": "text",
  "text": "Only while damaged, in survival, and below Y=64",
  "conditions": {
    "survival": true,
    "damage": 100,
    "altitude": "<64"
  }
}
```

Invalid or unknown condition values fail closed instead of exposing the node.

## Colors

Colors accept `#RRGGBB` or named values: `black`, `dark_blue`, `dark_green`, `dark_aqua`, `dark_red`, `dark_purple`,
`gold`, `gray`, `dark_gray`, `blue`, `green`, `aqua`, `red`, `light_purple`, `yellow`, `white`, `pink`, `cyan`,
`magenta`, `lime`, and `brown`. The aliases `orange`, `grey`, `dark_grey`, `light_blue`, `light_green`, and `light_red`
are also accepted.

Configuration colors can directly select any of the 16 vanilla chat colors: `BLACK`, `DARK_BLUE`, `DARK_GREEN`,
`DARK_AQUA`, `DARK_RED`, `DARK_PURPLE`, `GOLD`, `GRAY`, `DARK_GRAY`, `BLUE`, `GREEN`, `AQUA`, `RED`,
`LIGHT_PURPLE`, `YELLOW`, and `WHITE`. Select `CUSTOM` to use the associated custom value. Custom values accept
`#RRGGBB`, `#AARRGGBB`, `0xAARRGGBB`, or the legacy signed decimal ARGB representation.

## Configuration

The common configuration file is `config/datatip-common.toml`.

| Option                    | Default     | Description                                                                                  |
|---------------------------|-------------|----------------------------------------------------------------------------------------------|
| `enabled`                 | `true`      | Enables DataTip content                                                                      |
| `default_color`           | `GRAY`      | Default text color preset; accepts a vanilla color or `CUSTOM`                               |
| `default_color_value`     | `#FFAAAAAA` | Custom color used when `default_color = CUSTOM`                                              |
| `default_line_height`     | `0`         | Default line height; `0` uses vanilla font height                                            |
| `max_width`               | `0`         | Tooltip width override in pixels; `0` uses natural width plus vanilla screen-fit constraints |
| `shift_hint_color`        | `CUSTOM`    | Color preset for the merged Shift hint                                                       |
| `shift_hint_color_value`  | `#FF888888` | Custom color used when `shift_hint_color = CUSTOM`                                           |
| `scroll_hint_color`       | `CUSTOM`    | Color preset for the default scrolling hint                                                  |
| `scroll_hint_color_value` | `#FF888888` | Custom color used when `scroll_hint_color = CUSTOM`                                          |

`max_width` is a width override, not permission to exceed the screen. The final width, wrapping, height viewport,
position, background, and border still follow the vanilla tooltip pipeline.

`shift_hint_color*` and `scroll_hint_color*` style only the generated default hints. If an entry supplies `shiftHint` or
`scrollHint`, that node uses its own `color`, formatting, and `cycle_text` palette instead.

## JSON Schema

DataTip exports `.minecraft/datatip.schema.json` on startup. Map it in your editor to:

```text
assets/*/datatip/**/*.json
```

The schema provides completion and validation for built-in content types, common modifiers, conditions, localization
shapes, and item selectors. A top-level `$schema` property is allowed and ignored by the runtime. If the schema is
copied to the resource-pack root, a file under `assets/<namespace>/datatip/` can reference it with the appropriate
relative path.

The repository also keeps [datatip.schema.json](datatip.schema.json) for development.

## Legacy conversion

The loader detects legacy entries such as objects without `type`, primitive/array forms, and old nested `trans`
properties. It converts them first and then sends the converted output through the normal modern parser.

- `trans` is normalized to `translate` in converted output.
- `shift`, `prepend`, and `conditions` are preserved.
- Modern JSON should use `translate`; `trans` is not a supported modern alias.
- Converted files are written under `.minecraft/datatip_converted/<resource-namespace>/<resource-path>.json` for
  inspection.

For example, the resource `example:equipment/weapons` is exported as:

```text
.minecraft/datatip_converted/example/equipment/weapons.json
```

## Generated examples

The data generator is the authoritative full-coverage sample. Run:

```powershell
.\gradlew.bat runData
```

On Unix-like systems:

```bash
./gradlew runData
```

It generates:

```text
src/generated/resources/assets/minecraft/datatip/showcase.json
src/generated/resources/assets/minecraft/datatip/all_conditions.json
```

`showcase.json` covers every built-in content type plus layouts, localization, animation, stacking, translation keys,
Shift folding, common transforms, constraints, overflow, and viewport stress cases. `all_conditions.json` covers all
built-in conditions and their common value forms.

## Java API

### Register a content type

```java
TipContentRegistry.registerParser("my_type",(json, context) ->{
String value = context.getString(json, "value", "");
    return new

MyTipContent(value);
});
```

### Add or clear runtime content

```java
TipRuntimeContentRegistry.register(
    "example:my_item",
        new TipContentEntry(myContent, List.of(), false,false)
    );

    TipRuntimeContentRegistry.

clearNamespace("example");
```

### Register variables, conditions, and named compatibility readers

```java
VariableResolver.registerVariable("my_value",stack ->"42");

    ConditionChecker.

registerCondition(
    "my_condition",
        (value, stack, player, level) ->Boolean.TRUE.

equals(value)
);

    ComponentReaderRegistry.

register("my_component",reader);
```

On Forge 1.20.1, registered compatibility readers are referenced as `{component:my_component}`. Built-in item data
should use `{nbt:path}`.

`TipEventManager` also exposes reload, pre-render, and post-render hooks for mod integrations. Use the data-generation
builders under `com.cooobird.datatip.datagen` when generating JSON from Java.

## Complete JSON example

The following file is directly usable as `assets/example/datatip/readme_showcase.json`. It covers all 15 built-in
content types as well as translation keys, common modifiers, Stack/Z ordering, and independent Shift folding:

```json
{
  "minecraft:diamond": {
    "type": "vbox",
    "gap": 3,
    "padding": 2,
    "align": "center",
    "children": [
      {
        "type": "text",
        "translate": "example.tooltip.title",
        "color": "aqua",
        "bold": true,
        "selfAlignX": "center"
      },
      {
        "type": "divider",
        "style": "dashed",
        "widthMode": "fixed",
        "width": 150,
        "color": "dark_aqua"
      },
      {
        "type": "hbox",
        "gap": 6,
        "align": "center",
        "children": [
          {
            "type": "item",
            "item": "minecraft:diamond",
            "count": 8,
            "size": 24,
            "showCount": true
          },
          {
            "type": "progress",
            "progress": 0.75,
            "width": 110,
            "height": 8,
            "style": "gradient",
            "label": "75%",
            "labelAlign": "right"
          }
        ]
      },
      {
        "type": "stack",
        "padding": 3,
        "horizontalAlign": "center",
        "verticalAlign": "center",
        "children": [
          {
            "type": "block",
            "block": "minecraft:diamond_block",
            "size": 56,
            "rotationSpeed": 0.6,
            "offsetZ": -10
          },
          {
            "type": "text",
            "translate": "example.tooltip.foreground",
            "color": "yellow",
            "bold": true,
            "shadow": true,
            "offsetX": 4,
            "offsetY": -2,
            "offsetZ": 10
          }
        ]
      },
      {
        "type": "text",
        "translate": "example.tooltip.shift_detail",
        "color": "light_purple",
        "shift": true
      }
    ]
  },
  "minecraft:carrot_on_a_stick": {
    "type": "stack",
    "padding": 4,
    "horizontalAlign": "center",
    "verticalAlign": "bottom",
    "children": [
      {
        "type": "entity",
        "entity": "minecraft:pig",
        "size": 72,
        "rotationSpeed": 1.0,
        "autoRotate": true,
        "offsetZ": -5
      },
      {
        "type": "text",
        "text": {
          "zh_cn": "文字位于完整猪模型之上",
          "en_us": "Text above the complete pig model"
        },
        "color": "gold",
        "bold": true,
        "offsetY": -4,
        "offsetZ": 5
      }
    ]
  },
  "minecraft:clock": {
    "type": "carousel",
    "intervalSeconds": 3,
    "transition": "slide",
    "frames": [
      {
        "type": "atlas",
        "item": "minecraft:clock",
        "size": 32,
        "label": {"translate": "item.minecraft.clock"}
      },
      {
        "type": "image",
        "texture": "minecraft:textures/item/clock_00.png",
        "width": 32,
        "height": 32,
        "textureWidth": 32,
        "textureHeight": 32
      },
      {
        "type": "typewriter",
        "lines": {
          "zh_cn": ["轮播第三帧", "打字机文本"],
          "en_us": ["Carousel frame three", "Typewriter text"]
        },
        "charsPerSecond": 16,
        "pauseSeconds": 1,
        "loop": true,
        "color": "white",
        "align": "center"
      }
    ]
  },
  "minecraft:slime_ball": {
    "type": "vbox",
    "gap": 3,
    "children": [
      {"type": "spacer", "height": 4},
      {
        "type": "chart",
        "chartType": "bar",
        "width": 150,
        "height": 60,
        "title": {"translate": "example.tooltip.chart"},
        "showLabels": true,
        "showValues": true,
        "entries": [
          {"label": "A", "value": 3, "color": "red"},
          {"label": "B", "value": 7, "color": "green"},
          {"label": "C", "valueExpr": "{count}", "color": "blue"}
        ]
      }
    ]
  }
}
```

Translation keys used by the example can be placed in `assets/example/lang/en_us.json` and
`assets/example/lang/zh_cn.json`. The generated `showcase.json` remains the exhaustive stress-test reference.

## License

DataTip is licensed under the GNU Lesser General Public License 3.0. See [LICENSE](LICENSE).
