# DataTip

[English](README.md)

DataTip 是面向 Minecraft 1.21.1 与 NeoForge 21 的 JSON 驱动物品 Tooltip
系统。资源包可以直接组合文本、物品图标、纹理、旋转方块与实体、进度条、图表、动画和嵌套布局，无需编写 Java 代码。

当前项目版本：`1.2.5-neoforge`。

## 资源包结构

DataTip 会加载以下目录及其所有子目录中的 JSON 文件：

```text
assets/<namespace>/datatip/
```

命名空间应当直接位于 `assets` 下；`datatip` 是资源重载目录。文件名和子目录可以自由组织。例如以下两个路径都正确：

```text
assets/example/datatip/items.json
assets/example/datatip/equipment/weapons.json
```

`datatip.json` 只是示例文件名，并不是固定名称。

每个文件都是一个 JSON 对象，对象的键用于选择物品：

| 选择器     | 示例                  | 含义                      |
|---------|---------------------|-------------------------|
| 精确物品 ID | `minecraft:diamond` | 匹配一个物品                  |
| 物品标签    | `#minecraft:swords` | 匹配标签内的所有物品              |
| 通配符     | `minecraft:*_sword` | `*` 匹配任意字符序列，`?` 匹配一个字符 |

标签选择器不能包含通配符。以 `_` 开头的顶层键和 `$schema` 会被当作元数据忽略。

### 最小示例

将下面的内容保存为 `assets/example/datatip/getting_started.json`：

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

翻译内容放在 DataTip JSON 外部，例如写入 `assets/example/lang/zh_cn.json`：

```json
{
  "example.tooltip.diamond": "一颗闪亮的钻石"
}
```

修改资源后可以使用 `F3 + T` 重载资源包。

## 原版 Tooltip 集成

DataTip 接入 Minecraft/NeoForge 的 Tooltip 流程，而不是替换原版 Tooltip：

- Minecraft 负责收集 Tooltip 组件、为普通文本换行、选择鼠标旁的显示位置，并绘制背景和边框。
- DataTip 使用与实际渲染相同的可用宽度准备和测量内容。
- `max_width > 0` 会传入 Tooltip 收集事件，使原版文本与 DataTip 内容共用配置宽度。
- `max_width = 0` 时先保留内容自然宽度，再由原版屏幕适配规则限制；若实际可用宽度发生变化，文本会按最终宽度重新测量和换行。
- 高度受物理屏幕视口限制，计算时包含原版背景外扩、边距和普通 Tooltip 组件占用的高度。
- 内容高于视口时保留完整语义布局，只在可见的 DataTip 区域内滚动，不会让背景无限超出屏幕。
- 最终可见 Tooltip 的原版背景和四条边线会完整绘制。

方块和实体通过准备后的 DataTip 绘制命令路径直接渲染，并使用有作用域的渲染状态和物理剪裁。文本与其他 2D
节点使用同一份测量布局，因此测量、剪裁与绘制的边界一致。

### 键位

两个操作都注册在 Minecraft 控制设置的 DataTip 分类中，可以由玩家重新绑定：

| 操作            | 默认键位    | 用途                        |
|---------------|---------|---------------------------|
| 显示详细提示        | 左 Shift | 显示带有 `"shift": true` 的节点  |
| 滚动 Tooltip 内容 | 左 Ctrl  | Tooltip 超出视口时，按住该键并滚动鼠标滚轮 |

滚动提示中的 `%s` 会替换为当前真实绑定的键名，并非写死为 Ctrl。即使多个节点被折叠，一个 Tooltip 也只显示一条合并后的 Shift
提示。切换 Shift 内容时会保留当前滚动会话，不会直接跳回顶部。

## 条目属性

物品选择器对应的对象既是根内容节点，也是该条目的定义。

| 属性           | 默认值        | 说明                        |
|--------------|------------|---------------------------|
| `type`       | 现代 JSON 必填 | 根内容类型                     |
| `prepend`    | `false`    | 插入在物品名称之后、其余原版文本之前        |
| `shift`      | `false`    | 在“显示详细提示”键按下前，仅折叠当前节点及其子树 |
| `conditions` | `{}`       | 当前节点必须全部通过的条件             |

`shift` 和 `conditions` 都是通用修饰符，因此也可以用于任意嵌套节点，并非只能写在根节点。

## 通用修饰符

所有内置内容类型都支持同一组通用修饰符。

| 属性                                                    | 默认值       | 说明                                                                        |
|-------------------------------------------------------|-----------|---------------------------------------------------------------------------|
| `offsetX`、`offsetY`                                   | `0`       | 布局完成后的有符号 64 位 XY 位移                                                      |
| `offsetZ`                                             | `0`       | 同一父节点内的有符号 64 位图层顺序                                                       |
| `selfAlignX`                                          | `inherit` | `inherit`、`left`、`center`、`right`；`selfAlign` 是别名                         |
| `selfAlignY`                                          | `inherit` | `inherit`、`top`、`center`、`bottom`                                         |
| `margin`                                              | `0`       | 同时设置四个方向的外边距                                                              |
| `marginTop`、`marginRight`、`marginBottom`、`marginLeft` | `margin`  | 各方向的有符号 64 位外边距                                                           |
| `constraints`                                         | 无         | 可包含 `width`、`height`、`minWidth`、`minHeight`、`maxWidth`、`maxHeight`；值不得为负数 |
| `scale`                                               | `1`       | 等比缩放简写                                                                    |
| `scaleX`、`scaleY`                                     | `scale`   | 各轴正数缩放                                                                    |
| `rotation`                                            | `0`       | 有限的旋转角度，单位为度                                                              |
| `pivotX`、`pivotY`                                     | `0.5`     | `0` 到 `1` 的变换中心                                                           |
| `opacity`                                             | `1`       | `0` 到 `1` 的不透明度                                                           |
| `visible`                                             | `true`    | 为 `false` 时隐藏当前节点                                                         |
| `overflow`                                            | `none`    | `none`、`wrap`、`scale_down`（也接受 `scale-down`）或 `clip`                      |
| `shift`                                               | `false`   | 仅在配置的详情键按下时显示当前节点                                                         |
| `conditions`                                          | `{}`      | 必须全部通过的条件                                                                 |

当内容类型自身也使用 `width` 或 `height` 时，应显式使用 `constraints` 对象：

```json
{
  "type": "text",
  "text": "可以移动和变换的文本",
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

实际处理顺序是：自然尺寸测量 → 尺寸约束 → 父布局对齐 → 外边距 → 中心点/缩放/旋转 → XY 位移 → 局部 Z 顺序 → overflow
与视口剪裁。

### Z 顺序与真实重叠

`offsetZ` 只排序当前父节点内的兄弟节点。数值较小的先绘制，较大的后绘制；数值相同则保持 JSON 中的原始顺序。子节点不能越过祖先命令组，
`offsetZ` 也不会写入 Minecraft 世界的深度缓冲。

需要多个节点共用同一 XY 区域时使用 `stack`。Z 顺序只会改变真正发生重叠的内容：

```json
{
  "type": "stack",
  "padding": 2,
  "horizontalAlign": "center",
  "verticalAlign": "center",
  "children": [
    {"type": "entity", "entity": "minecraft:pig", "size": 68, "offsetZ": -10},
    {"type": "text", "text": "位于猪模型之上的文本", "color": "gold", "bold": true, "offsetZ": 10}
  ]
}
```

## 内置内容类型

| 类型           | 用途              |
|--------------|-----------------|
| `text`       | 字面文本、语言映射或翻译键文本 |
| `spacer`     | 空白垂直间距          |
| `divider`    | 实线、虚线或点线分隔符     |
| `item`       | 物品堆图标           |
| `atlas`      | 来自纹理、方块或物品的平面纹理 |
| `block`      | 旋转的 3D 方块模型     |
| `entity`     | 旋转的 3D 实体模型     |
| `progress`   | 带样式的进度条         |
| `vbox`       | 垂直子节点布局         |
| `hbox`       | 水平子节点布局         |
| `stack`      | 共用 XY 区域的叠放布局   |
| `carousel`   | 定时切换的帧容器        |
| `typewriter` | 逐字显示动画          |
| `image`      | 纹理中的 UV 区域      |
| `chart`      | 柱状图、折线图或饼图      |

### 文本与本地化

`text` 节点必须在 `text` 和 `translate` 中二选一：

```json
{"type": "text", "text": "字面文本"}
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
{"type": "text", "translate": "example.tooltip.key"}
```

语言映射会依次尝试当前语言、`en_us` 和第一项。翻译键使用普通 Minecraft 语言文件，因此可以在不修改 DataTip JSON
的情况下新增或替换翻译。现代解析器不接受旧 `trans` 属性；旧格式转换器会将其改写为 `translate`。

| 文本属性                                         | 默认值     | 说明                                      |
|----------------------------------------------|---------|-----------------------------------------|
| `color`                                      | 配置默认值   | 命名颜色或 `#RRGGBB`                         |
| `font`                                       | 原版字体    | 字体资源位置                                  |
| `shadow`                                     | `true`  | 字体阴影                                    |
| `align`                                      | `left`  | 在文本测量宽度内使用 `left`、`center` 或 `right` 对齐 |
| `lineHeight`                                 | 配置/原版   | 行高；配置值 `0` 表示原版字体高度                     |
| `maxWidth`                                   | `0`     | 可选的节点局部换行宽度                             |
| `bold`、`italic`、`underlined`、`strikethrough` | `false` | 文本样式                                    |

可见的 `label` 和 `title` 属性可以使用字面字符串、语言映射、带样式的语言项或 `{"translate": "key"}`
。变量会在测量前解析，所以换行尺寸与最终绘制文本保持一致。

### 布局容器

| 类型      | 属性                                                      | 默认值                                 |
|---------|---------------------------------------------------------|-------------------------------------|
| `vbox`  | `children`、`gap`、`padding`、`align`（`left/center/right`） | `gap: 0`、`padding: 0`、`align: left` |
| `hbox`  | `children`、`gap`、`padding`、`align`（`top/center/bottom`） | `gap: 0`、`padding: 0`、`align: top`  |
| `stack` | `children`、`padding`、`horizontalAlign`、`verticalAlign`  | `padding: 0`，两个方向分别默认 `left/top`    |

容器尺寸由测量后的子节点、间距、内边距、外边距、尺寸约束和变换共同决定。偏移会移动视觉结果，同时仍会进入准备后的视口边界计算。

### 视觉节点

| 类型       | 类型专属属性与默认值                                                                                                                                       |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| `item`   | `item: minecraft:air`、`count: 1`、`size: 16`、`showCount: true`、`showDurability: true`、`showLabel: false`、可选 `label`、`labelColor: #FFFFFF`         |
| `atlas`  | 来源优先级 `texture` → `block` → `item`；`width: 16`、`height: width`，可选 `size` 会同时覆盖宽高，可选 `label`                                                      |
| `block`  | `block: minecraft:stone`、`size: 32`、`rotationSpeed: 0.5`、`autoRotate: true`、可选 `label`                                                           |
| `entity` | `entity: minecraft:pig`、`size: 48`、`rotationSpeed: 1.0`、`autoRotate: true`、可选 `label`                                                            |
| `image`  | `texture: minecraft:textures/gui/icons.png`、`width: 64`、`height: 64`、`u: 0`、`v: 0`、`textureWidth: width`、`textureHeight: height`；位置和最终节点缩放使用通用变换 |

`block` 与 `entity` 会保留声明的正方形布局尺寸，并计入旋转模型的视觉边界；它们只受最终物理 Tooltip 视口剪裁，不会被无关的中间矩形截断。

### 分隔符、间距与进度条

| 类型         | 属性与默认值                                                                                                                                                                                                                                               |
|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `spacer`   | `height: 4`                                                                                                                                                                                                                                          |
| `divider`  | `color: #555555`、`thickness: 1`、`width: 0`、`marginTop: 2`、`marginBottom: 2`、`style: solid`（`solid/dashed/dotted`）、`widthMode: fill`（`fill/fixed/centered`）                                                                                           |
| `progress` | `progress: 0`、`width: 100`、`height: 8`、`colorFg: #55FF55`、`colorBg: #333333`、可选 `colorFgLight`/`colorBgDark`、`style: gradient`（`flat/gradient/segmented/animated`）、`showLabel: false`、可选 `label`、`animated: false`、`animSpeed: 2`、`labelAlign: left` |

### 轮播与打字机

```json
{
  "type": "carousel",
  "intervalSeconds": 3,
  "transition": "slide",
  "frames": [
    {"type": "text", "text": "第一帧"},
    {"type": "item", "item": "minecraft:diamond", "size": 24}
  ]
}
```

`carousel` 默认使用 `intervalSeconds: 3` 和 `transition: fade`；过渡类型为 `none`、`fade` 和 `slide`。

`typewriter` 的 `lines` 可以是普通数组，也可以是“语言代码到数组”的对象。不同语言的单行内容还可以写成带样式对象。默认值为
`charsPerSecond: 2`、`pauseSeconds: 1`、`loop: false`、`shadow: true`、`align: left`；同时支持文本颜色、字体、行高和样式属性。

### 图表

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
    {"label": "伤害", "value": 8, "color": "red"},
    {"label": "生命", "valueExpr": "{player_health}", "color": "green"}
  ]
}
```

`chartType` 可以是 `bar`、`line` 或 `pie`。默认值：`width: 100`、`height: 60`、`titleColor: #FFFFFF`、`labelColor: #AAAAAA`、
`valueColor: #FFFFFF`、`zeroLineColor: #888888`、`showLabels: true`、`showValues: true`。条目值可以是数字、数字字符串、变量或表达式。

## 变量与表达式

可见文本和数值表达式都可以使用花括号变量，例如 `"耐久：{durability}/{max_durability}"`。

| 分组  | 变量                                                                                                                                                                                    |
|-----|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 物品  | `durability`、`max_durability`、`damage`、`count`、`item_name`、`item_id`、`durability_percent`、`enchantment_count`、`is_enchanted`、`rarity`、`max_stack_size`、`is_stackable`、`is_damageable` |
| 玩家  | `player_health`、`player_max_health`、`player_hunger`、`player_experience`、`player_x`、`player_y`、`player_z`                                                                              |
| 世界  | `game_time`、`is_day`、`is_raining`、`is_thundering`                                                                                                                                     |
| 格式化 | `durability_bar`、`health_bar`                                                                                                                                                         |
| 组件  | `{component:path}` 与 `{custom_data:path}`                                                                                                                                             |

常见组件路径包括 `custom_name`、`item_name`、`lore`、`damage`、`max_damage`、`repair_cost`、`enchantments`、`unbreakable`、
`color`、`trim` 和 `custom_data`。

表达式支持算术、比较、布尔逻辑、括号和三元运算符。例如：

```text
{durability_percent} < 20
{player_health} / {player_max_health} * 100
{is_raining} && {count} >= 16
{is_enchanted} ? 1 : 0
```

## 条件

`conditions` 对象内的所有属性按 AND 组合。条件可以写在根节点或任意嵌套节点上。

| 条件                               | 可接受的值                                   |
|----------------------------------|-----------------------------------------|
| `dimension`                      | 维度 ID                                   |
| `biome`                          | 生物群系 ID 或 ID 数组                         |
| `holding`                        | 物品 ID 或数组；检查主手和副手                       |
| `sneaking`、`creative`、`survival` | 布尔值                                     |
| `health`                         | 数字、比较字符串或百分比字符串                         |
| `hunger`、`experience`、`level`    | 数字或比较字符串                                |
| `time`                           | 数字，或 `day`、`night`、`noon`、`midnight`    |
| `weather`                        | `clear`、`rain`、`thunder`                |
| `light`                          | 数字/比较字符串，或 `dark`、`dim`、`bright`、`full` |
| `altitude`                       | 数字，或使用 `>`、`>=`、`<`、`<=` 的比较字符串         |
| `enchanted`                      | 布尔值                                     |
| `damage`                         | 允许的当前物品损伤值上限                            |
| `count`                          | 允许的物品堆数量下限                              |
| `component`                      | 必须存在的组件路径                               |
| `custom_data`                    | 必须存在的路径字符串，或必须匹配的对象                     |
| `item_tag`                       | 物品标签 ID                                 |

```json
{
  "type": "text",
  "text": "仅在物品损坏、生存模式且高度低于 64 时显示",
  "conditions": {
    "survival": true,
    "damage": 100,
    "altitude": "<64"
  }
}
```

无效或未知的条件值会安全地判定为不通过，不会意外显示节点。

## 颜色

颜色支持 `#RRGGBB` 或命名值：`black`、`dark_blue`、`dark_green`、`dark_aqua`、`dark_red`、`dark_purple`、`gold`、`gray`、
`dark_gray`、`blue`、`green`、`aqua`、`red`、`light_purple`、`yellow`、`white`、`pink`、`cyan`、`magenta`、`lime`、`brown`。同时接受
`orange`、`grey`、`dark_grey`、`light_blue`、`light_green`、`light_red` 别名。

配置文件中的颜色使用有符号整数 ARGB 值。

## 配置

通用配置文件位于 `config/datatip-common.toml`。

| 选项                    | 默认值          | 说明                                |
|-----------------------|--------------|-----------------------------------|
| `enabled`             | `true`       | 启用 DataTip 内容                     |
| `default_color`       | `0xFFAAAAAA` | 整数 ARGB 格式的默认文本颜色                 |
| `default_line_height` | `0`          | 默认行高；`0` 使用原版字体高度                 |
| `max_width`           | `0`          | Tooltip 宽度覆盖值；`0` 使用自然宽度和原版屏幕适配限制 |
| `shift_hint_color`    | `0xFF888888` | 合并后的 Shift 提示颜色                   |

`max_width` 只是宽度覆盖值，并不允许 Tooltip 超出屏幕。最终宽度、换行、高度视口、位置、背景和边框仍然遵从原版 Tooltip 流程。

## JSON Schema

DataTip 启动时会导出 `.minecraft/datatip.schema.json`。建议在编辑器中将它映射到：

```text
assets/*/datatip/**/*.json
```

Schema 为内置内容类型、通用修饰符、条件、本地化结构和物品选择器提供补全与校验。运行时允许顶层 `$schema` 属性并会忽略它。如果将
Schema 复制到资源包根目录，`assets/<namespace>/datatip/` 下的文件可以使用正确的相对路径引用它。

仓库中的 [datatip.schema.json](datatip.schema.json) 用于开发时校验。

## 旧格式转换

加载器会识别没有 `type` 的旧对象、原始值/数组形式以及旧的嵌套 `trans` 属性。它会先转换，再把转换结果交给正常的现代解析流程。

- 转换输出会把 `trans` 规范化为 `translate`。
- `shift`、`prepend` 和 `conditions` 会保留。
- 现代 JSON 应使用 `translate`；`trans` 不是现代格式的别名。
- 转换后的文件会写入 `.minecraft/datatip_converted/<resource-namespace>/<resource-path>.json` 供检查。

例如资源 `example:equipment/weapons` 会导出为：

```text
.minecraft/datatip_converted/example/equipment/weapons.json
```

## 生成完整示例

数据生成器是完整覆盖示例的权威来源。Windows 下运行：

```powershell
.\gradlew.bat runData
```

类 Unix 系统下运行：

```bash
./gradlew runData
```

它会生成：

```text
src/generated/resources/assets/minecraft/datatip/showcase.json
src/generated/resources/assets/minecraft/datatip/all_conditions.json
```

`showcase.json` 覆盖所有内置内容类型，以及布局、本地化、动画、叠放、翻译键、Shift 折叠、通用变换、约束、overflow 和视口压力场景。
`all_conditions.json` 覆盖所有内置条件及常见值形态。

## Java API

### 注册内容类型

```java
TipContentRegistry.registerParser("my_type", (json, context) -> {
    String value = context.getString(json, "value", "");
    return new MyTipContent(value);
});
```

### 添加或清理运行时内容

```java
TipRuntimeContentRegistry.register(
    "example:my_item",
    new TipContentEntry(myContent, List.of(), false, false)
);

TipRuntimeContentRegistry.clearNamespace("example");
```

### 注册变量、条件和组件读取器

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

`TipEventManager` 还为模组集成提供资源重载、渲染前和渲染后钩子。需要由 Java 生成 JSON 时，可以使用
`com.cooobird.datatip.datagen` 下的数据生成构建器。

## 完整 JSON 示例

下面的文件可以直接保存为 `assets/example/datatip/readme_showcase.json`。它覆盖全部 15 种内置内容类型，以及翻译键、通用修饰符、Stack/Z
顺序和独立 Shift 折叠：

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

示例使用的翻译键可以分别放入 `assets/example/lang/en_us.json` 和 `assets/example/lang/zh_cn.json`。数据生成器输出的
`showcase.json` 仍然是完整压力测试的权威参考。

## 许可证

DataTip 使用 GNU Lesser General Public License 3.0。详见 [LICENSE](LICENSE)。
