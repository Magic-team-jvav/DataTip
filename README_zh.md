# DataTip

JSON 驱动的自定义物品 Tooltip 系统。在资源包中定义 tooltip，路径为 `assets/<modid>/datatip/datatip.json`。

## 快速开始

```json
{
  "minecraft:diamond": {
    "type": "text",
    "text": "一颗闪亮的钻石",
    "color": "#55FFFF"
  }
}
```

将此文件放入资源包的 `assets/minecraft/datatip/datatip.json`，然后在游戏中悬停钻石即可看到效果。

## 内容类型

| 类型           | 说明    | 示例                                                            |
|--------------|-------|---------------------------------------------------------------|
| `text`       | 文本内容  | `{"type": "text", "text": "Hello", "color": "white"}`         |
| `item`       | 物品图标  | `{"type": "item", "item": "minecraft:diamond", "size": 32}`   |
| `block`      | 3D 方块 | `{"type": "block", "block": "minecraft:stone", "size": 48}`   |
| `entity`     | 3D 实体 | `{"type": "entity", "entity": "minecraft:wolf", "size": 48}`  |
| `progress`   | 进度条   | `{"type": "progress", "progress": 0.75, "width": 100}`        |
| `carousel`   | 轮播容器  | `{"type": "carousel", "intervalSeconds": 3, "frames": [...]}` |
| `typewriter` | 打字机效果 | `{"type": "typewriter", "lines": ["逐字显示"]}`                   |
| `atlas`      | 纹理渲染  | `{"type": "atlas", "item": "minecraft:apple", "size": 32}`    |
| `image`      | 图片    | `{"type": "image", "texture": "mymod:gui/icon.png"}`          |
| `chart`      | 图表    | `{"type": "chart", "chartType": "bar", "entries": [...]}`     |
| `vbox`       | 垂直布局  | `{"type": "vbox", "gap": 4, "children": [...]}`               |
| `hbox`       | 水平布局  | `{"type": "hbox", "gap": 8, "children": [...]}`               |
| `divider`    | 分割线   | `{"type": "divider", "color": "#555555"}`                     |
| `spacer`     | 间距    | `{"type": "spacer", "height": 8}`                             |

### 内容类型详情

#### text
文本内容，支持丰富的样式选项。
```json
{
  "type": "text",
  "text": "你好世界",
  "color": "#55FFFF",
  "bold": true,
  "align": "center"
}
```

#### item
在 tooltip 中渲染物品图标。
```json
{
  "type": "item",
  "item": "minecraft:diamond",
  "size": 32,
  "offsetY": 4
}
```

#### block
渲染 3D 方块模型，支持自动旋转。
```json
{
  "type": "block",
  "block": "minecraft:chest",
  "size": 48,
  "autoRotate": true
}
```

#### entity
渲染 3D 实体模型，支持自动旋转。
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
进度条，支持多种样式。
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
自动切换多帧显示。
```json
{
  "type": "carousel",
  "intervalSeconds": 3,
  "frames": [
    {"type": "text", "text": "第一帧"},
    {"type": "text", "text": "第二帧"}
  ]
}
```

#### typewriter
逐字显示动画效果。
```json
{
  "type": "typewriter",
  "lines": ["你好", "世界"],
  "charsPerSecond": 10,
  "pauseSeconds": 1,
  "loop": false
}
```

#### atlas
从纹理图集渲染（物品/方块 ID 自动转换）。
```json
{
  "type": "atlas",
  "item": "minecraft:apple",
  "size": 32
}
```

#### image
渲染自定义纹理图片。
```json
{
  "type": "image",
  "texture": "mymod:textures/gui/icon.png",
  "width": 32,
  "height": 32
}
```

#### chart
柱状图、饼图或折线图，支持变量。
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
垂直/水平布局容器。
```json
{
  "type": "vbox",
  "gap": 4,
  "children": [...]
}
```

#### divider
分割线，支持多种样式。
```json
{
  "type": "divider",
  "color": "#555555",
  "style": "dashed"
}
```

#### spacer
内容之间的空白间距。
```json
{
  "type": "spacer",
  "height": 8
}
```

## 文本属性

| 属性              | 类型            | 默认值     | 说明                         |
|-----------------|---------------|---------|----------------------------|
| `text`          | String/Object | -       | 文本内容（支持多语言对象）              |
| `color`         | String        | "white" | 颜色（命名色或十六进制，支持表达式）         |
| `align`         | String        | "left"  | 对齐：`left`、`center`、`right` |
| `bold`          | boolean       | false   | 粗体                         |
| `italic`        | boolean       | false   | 斜体                         |
| `underlined`    | boolean       | false   | 下划线                        |
| `strikethrough` | boolean       | false   | 删除线                        |
| `shift`         | boolean       | false   | 需要按住 Shift 才显示             |
| `maxWidth`      | int           | 0       | 最大宽度（0=不换行）                |

## 进度条属性

| 属性           | 类型      | 默认值        | 说明                                          |
|--------------|---------|------------|---------------------------------------------|
| `progress`   | float   | 0.0        | 进度值（0.0-1.0）                                |
| `width`      | int     | 100        | 宽度                                          |
| `height`     | int     | 8          | 高度                                          |
| `colorFg`    | String  | "#55FF55"  | 前景色                                         |
| `colorBg`    | String  | "#333333"  | 背景色                                         |
| `style`      | String  | "gradient" | 样式：`flat`、`gradient`、`segmented`、`animated` |
| `showLabel`  | boolean | false      | 显示标签                                        |
| `label`      | String  | -          | 自定义标签文本                                     |
| `labelAlign` | String  | "left"     | 标签对齐：`left`、`center`、`right`                |

## 分割线属性

| 属性          | 类型     | 默认值       | 说明                           |
|-------------|--------|-----------|------------------------------|
| `color`     | String | "#555555" | 颜色                           |
| `style`     | String | "solid"   | 样式：`solid`、`dashed`、`dotted` |
| `width`     | int    | 0         | 宽度（0=填充）                     |
| `widthMode` | String | "fill"    | 模式：`fill`、`fixed`、`centered` |

## 轮播容器属性

| 属性                | 类型    | 默认值 | 说明       |
|-------------------|-------|-----|----------|
| `frames`          | Array | -   | 内容帧数组    |
| `intervalSeconds` | int   | 3   | 帧切换间隔（秒） |

## 打字机属性

| 属性               | 类型      | 默认值   | 说明      |
|------------------|---------|-------|---------|
| `lines`          | Array   | -     | 文本行数组   |
| `charsPerSecond` | int     | 2     | 每秒字符数   |
| `pauseSeconds`   | int     | 1     | 行间暂停（秒） |
| `loop`           | boolean | false | 循环播放    |

## 图表属性

| 属性              | 类型     | 默认值       | 说明                      |
|-----------------|--------|-----------|-------------------------|
| `chartType`     | String | "bar"     | 图表类型：`bar`、`pie`、`line` |
| `width`         | int    | 100       | 宽度                      |
| `height`        | int    | 60        | 高度                      |
| `title`         | String | -         | 标题                      |
| `entries`       | Array  | -         | 数据条目数组                  |
| `titleColor`    | String | "#FFFFFF" | 标题颜色                    |
| `labelColor`    | String | "#AAAAAA" | 标签颜色                    |
| `valueColor`    | String | "#FFFFFF" | 数值颜色                    |
| `zeroLineColor` | String | "#888888" | 零线颜色（区分正负值）             |

## 实体/物品/方块/纹理/图片通用属性

| 属性        | 类型  | 默认值 | 说明               |
|-----------|-----|-----|------------------|
| `offsetX` | int | 0   | X 轴偏移（正值向右，负值向左） |
| `offsetY` | int | 0   | Y 轴偏移（正值向下，负值向上） |

## 变量

| 变量                     | 说明                |
|------------------------|-------------------|
| `{durability}`         | 当前耐久              |
| `{max_durability}`     | 最大耐久              |
| `{damage}`             | 已损坏值              |
| `{durability_percent}` | 耐久百分比             |
| `{durability_bar}`     | 耐久条（可视化）          |
| `{count}`              | 物品数量              |
| `{item_name}`          | 物品称               |
| `{item_id}`            | 物品 ID             |
| `{enchantment_count}`  | 附魔数量              |
| `{is_enchanted}`       | 是否附魔              |
| `{rarity}`             | 稀有度               |
| `{max_stack_size}`     | 最大堆叠数             |
| `{is_stackable}`       | 是否可堆叠             |
| `{is_damageable}`      | 是否可损坏             |
| `{player_health}`      | 玩家生命值             |
| `{player_max_health}`  | 玩家最大生命值           |
| `{player_hunger}`      | 玩家饥饿值             |
| `{player_experience}`  | 玩家经验等级            |
| `{player_x}`           | 玩家 X 坐标           |
| `{player_y}`           | 玩家 Y 坐标           |
| `{player_z}`           | 玩家 Z 坐标           |
| `{game_time}`          | 游戏时间              |
| `{is_day}`             | 是否白天              |
| `{is_raining}`         | 是否下雨              |
| `{is_thundering}`      | 是否雷暴              |
| `{health_bar}`         | 生命条（可视化）          |
| `{nbt:path}`           | 组件值（见下方 NBT 变量说明） |

### NBT 变量

使用 `{nbt:path}` 语法读取物品组件/NBT 数据。

**NeoForge 1.21.1（组件系统）：**
```json
{
  "type": "text",
  "text": "名称: {nbt:custom_name}",
  "color": "white"
}
```

**支持的路径：**
- `custom_name` - 自定义物品名称
- `item_name` - 物品名称
- `lore` - 物品描述
- `damage` - 损坏值
- `max_damage` - 最大损坏值
- `enchantments` - 附魔

**Forge 1.20.1（NBT 系统）：**
```json
{
  "type": "text",
  "text": "名称: {nbt:display.Name}",
  "color": "white"
}
```

**支持的路径：**
- `display.Name` - 自定义物品名称
- `display.Lore` - 物品描述
- `Damage` - 损坏值
- `Enchantments` - 附魔
| `{nbt:path}`           | 组件值（如 `{nbt:custom_name}`、`{nbt:lore}`） |

## 条件

| 条件           | 说明   | 示例                                     |
|--------------|------|----------------------------------------|
| `dimension`  | 维度   | `"dimension": "minecraft:the_nether"`  |
| `biome`      | 生物群系 | `"biome": "minecraft:desert"`          |
| `holding`    | 手持物品 | `"holding": "minecraft:diamond_sword"` |
| `sneaking`   | 是否潜行 | `"sneaking": true`                     |
| `creative`   | 创造模式 | `"creative": true`                     |
| `survival`   | 生存模式 | `"survival": true`                     |
| `health`     | 生命值  | `"health": "50%"` 或 `"health": 10`     |
| `hunger`     | 饥饿值  | `"hunger": 15`                         |
| `experience` | 经验等级 | `"experience": 30`                     |
| `time`       | 时间   | `"time": "day"` 或 `"time": 6000`       |
| `weather`    | 天气   | `"weather": "rain"`                    |
| `light`      | 光照   | `"light": "dark"` 或 `"light": 8`       |
| `altitude`   | 海拔   | `"altitude": ">=64"`                   |
| `enchanted`  | 是否附魔 | `"enchanted": true`                    |
| `damage`     | 损坏值  | `"damage": 100`                        |
| `count`      | 物品数量 | `"count": 16`                          |
| `nbt`        | 组件存在 | `"nbt": "custom_name"`                 |
| `item_tag`   | 物品标签 | `"item_tag": "minecraft:swords"`       |

### NBT 条件

检查物品是否有指定的组件/NBT 数据。

**NeoForge 1.21.1：**
```json
{
  "conditions": {
    "nbt": "custom_name"
  }
}
```

**Forge 1.20.1：**
```json
{
  "conditions": {
    "nbt": "display.Name"
  }
}
```

### 物品标签条件

检查物品是否属于指定标签。
```json
{
  "conditions": {
    "item_tag": "minecraft:swords"
  }
}
```

## 表达式

支持在文本和颜色中使用表达式：

```json
{
  "type": "text",
  "text": "状态: {durability > 100 ? '良好' : '需要修复'}",
  "color": "{durability > 100 ? 'green' : 'red'}"
}
```

支持的运算符：
- 比较：`>`、`<`、`==`、`!=`、`>=`、`<=`
- 逻辑：`&&`、`||`、`!`
- 算术：`+`、`-`、`*`、`/`
- 三元：`condition ? true_value : false_value`

## 多语言

```json
{
  "type": "text",
  "text": {
    "zh_cn": "你好世界",
    "en_us": "Hello World"
  },
  "color": "aqua"
}
```

## 特殊属性

| 属性           | 说明                                    |
|--------------|---------------------------------------|
| `align`      | 对齐：`left`、`center`、`right`（所有内容类型都支持） |
| `shift`      | 需要按住 Shift 才显示                        |
| `prepend`    | 显示在物品名之后（原版内容之前）                      |
| `conditions` | 条件配置                                  |

## 颜色

### 命名颜色

| 颜色                  | 十六进制      |
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

### 十六进制颜色

任何 6 位十六进制值，带 `#` 前缀：`"#FF6600"`、`"#AABBCC"`、`"#00FF00"`

## 配置

文件：`config/datatip-common.toml`

| 选项                  | 类型      | 默认值        | 说明            |
|---------------------|---------|------------|---------------|
| `enabled`           | boolean | true       | 启用/禁用 DataTip |
| `defaultColor`      | int     | 0xFFAAAAAA | 默认文本颜色        |
| `defaultLineHeight` | int     | 12         | 默认行高          |
| `maxWidth`          | int     | 200        | 最大 tooltip 宽度 |

## 旧格式支持

旧格式会自动转换为新格式。转换后的 JSON 保存到 `.minecraft/datatip_converted/` 目录。

**支持的旧格式：**

```json
{
  "minecraft:diamond": ["第 1 行", "第 2 行"],
  "minecraft:diamond_sword": {
    "text": {"zh_cn": ["削铁如泥"], "en_us": ["Cuts through iron"]},
    "color": "gold",
    "shift": true
  }
}
```

**转换输出：**
```
.minecraft/datatip_converted/
  └── confluence/
      └── datatip/
          └── datatip.json
```

## 完整示例

```json
{
  "minecraft:diamond": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "钻石", "color": "#55FFFF", "align": "center"},
      {"type": "divider", "color": "#555555", "widthMode": "centered", "width": 80},
      {"type": "text", "text": "一种珍贵的宝石", "color": "gray", "align": "center"},
      {"type": "spacer", "height": 4},
      {"type": "text", "text": "左对齐文本", "color": "white", "align": "left"},
      {"type": "text", "text": "居中文本", "color": "gold", "align": "center"},
      {"type": "text", "text": "右对齐文本", "color": "aqua", "align": "right"}
    ]
  },

  "minecraft:diamond_sword": {
    "type": "hbox",
    "gap": 8,
    "children": [
      {"type": "item", "item": "minecraft:diamond_sword", "size": 32},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "钻石剑", "color": "aqua", "align": "center"},
        {"type": "text", "text": "耐久: {durability}/{max_durability}", "color": "gray"},
        {"type": "text", "text": "百分比: {durability_percent}%", "color": "gold"}
      ]}
    ]
  },

  "minecraft:diamond_pickaxe": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "钻石镐", "color": "aqua", "align": "center"},
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
        {"type": "text", "text": "金苹果", "color": "gold", "align": "center"},
        {"type": "text", "text": "恢复生命值", "color": "red"}
      ]},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "Golden Apple", "color": "gold", "align": "center"},
        {"type": "text", "text": "Restores health", "color": "red"}
      ]},
      {"type": "vbox", "gap": 2, "children": [
        {"type": "text", "text": "金蘋果", "color": "gold", "align": "center"},
        {"type": "text", "text": "恢復生命值", "color": "red"}
      ]}
    ]
  },

  "minecraft:nether_star": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "下界之星", "color": "light_purple", "align": "center"},
      {"type": "typewriter", "lines": ["Boss 掉落物", "用于合成信标", "稀有物品"], "charsPerSecond": 10, "pauseSeconds": 1, "color": "gray"}
    ]
  },

  "minecraft:stone": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "石头", "color": "gray"},
      {"type": "divider", "color": "#555555", "style": "solid"},
      {"type": "text", "text": "实线上方", "color": "white"},
      {"type": "divider", "color": "#555555", "style": "dashed"},
      {"type": "text", "text": "虚线上方", "color": "white"},
      {"type": "divider", "color": "#555555", "style": "dotted"},
      {"type": "text", "text": "点线上方", "color": "white"}
    ]
  },

  "minecraft:bow": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "耐久: {durability}/{max_durability}", "color": "gray"},
      {"type": "text", "text": "百分比: {durability_percent}%", "color": "gold"},
      {"type": "text", "text": "耐久条: {durability_bar}", "color": "green"},
      {"type": "text", "text": "生命条: {health_bar}", "color": "red"}
    ]
  },

  "minecraft:iron_ingot": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "铁锭", "color": "white", "align": "center"},
      {"type": "divider", "color": "#555555", "widthMode": "centered", "width": 60},
      {"type": "text", "text": "生命: {player_health}/{player_max_health}", "color": "red"},
      {"type": "text", "text": "饥饿: {player_hunger}", "color": "gold"},
      {"type": "text", "text": "经验: {player_experience}", "color": "green"}
    ]
  },

  "minecraft:clock": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "时间: {game_time}", "color": "gold"},
      {"type": "text", "text": "白天: {is_day}", "color": "yellow"},
      {"type": "text", "text": "下雨: {is_raining}", "color": "aqua"},
      {"type": "text", "text": "雷暴: {is_thundering}", "color": "red"}
    ]
  },

  "#minecraft:swords": {
    "type": "text",
    "text": "所有剑类武器", "color": "yellow"
  },

  "minecraft:diamond_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "钻石块", "color": "aqua", "align": "center"},
      {"type": "text", "text": "只在下界显示", "color": "dark_red"}
    ],
    "conditions": {
      "dimension": "minecraft:the_nether"
    }
  },

  "minecraft:emerald_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "绿宝石块", "color": "green"},
      {"type": "text", "text": "按住 Shift 才能看到这段文字", "color": "gray", "shift": true}
    ]
  },

  "minecraft:gold_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "金块", "color": "gold"},
      {"type": "text", "text": "整条 tooltip 需要按住 Shift", "color": "gray"},
      {"type": "text", "text": "所有内容都会被折叠", "color": "yellow"}
    ],
    "shift": true
  },

  "minecraft:iron_block": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "铁块", "color": "white"},
      {"type": "text", "text": "这段内容会显示在物品名后面", "color": "gray"}
    ],
    "prepend": true
  },

  "minecraft:enchanted_golden_apple": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "附魔金苹果", "color": "gold", "bold": true, "align": "center"},
      {"type": "divider", "color": "#FFD700", "widthMode": "centered", "width": 80},
      {"type": "hbox", "gap": 8, "children": [
        {"type": "item", "item": "minecraft:enchanted_golden_apple", "size": 32},
        {"type": "vbox", "gap": 2, "children": [
          {"type": "text", "text": "稀有食物", "color": "light_purple"},
          {"type": "progress", "progress": 1.0, "width": 80, "height": 6, "colorFg": "#FFD700", "showLabel": true, "label": "满耐久", "labelAlign": "left"}
        ]}
      ]},
      {"type": "text", "text": "耐久条: {durability_bar}", "color": "gray"},
      {"type": "text", "text": "生命条: {health_bar}", "color": "red"}
    ]
  },

  "minecraft:wolf_spawn_egg": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "狼刷怪蛋", "color": "white", "align": "center"},
      {"type": "entity", "entity": "minecraft:wolf", "size": 48, "rotationSpeed": 1.0, "autoRotate": true},
      {"type": "text", "text": "可以驯服为宠物", "color": "gray"}
    ]
  },

  "minecraft:crafting_table": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "工作台", "color": "white", "align": "center"},
      {"type": "block", "block": "minecraft:crafting_table", "size": 48, "rotationSpeed": 0.5, "autoRotate": true},
      {"type": "text", "text": "用于合成物品", "color": "gray"}
    ]
  },

  "minecraft:apple": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "苹果", "color": "red", "align": "center"},
      {"type": "atlas", "item": "minecraft:apple", "size": 32},
      {"type": "text", "text": "恢复饥饿值", "color": "gray"}
    ]
  },

  "minecraft:red_concrete": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "红色混凝土", "color": "red", "align": "center"},
      {"type": "atlas", "block": "minecraft:red_concrete", "size": 32},
      {"type": "text", "text": "装饰方块", "color": "gray"}
    ]
  },

  "minecraft:iron_sword": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "铁剑", "color": "white", "align": "center"},
      {"type": "text", "text": "状态: {durability > 100 ? '良好' : '需要修复'}", "color": "{durability > 100 ? 'green' : 'red'}"},
      {"type": "text", "text": "耐久: {durability}/{max_durability} ({durability_percent}%)", "color": "gray"}
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
      {"type": "text", "text": "煤炭", "color": "gray"},
      {"type": "spacer", "height": 8},
      {"type": "text", "text": "上面有 8px 间距", "color": "white"}
    ]
  },

  "minecraft:emerald": {
    "type": "vbox",
    "gap": 2,
    "children": [
      {"type": "text", "text": "粗体文本", "color": "green", "bold": true},
      {"type": "text", "text": "斜体文本", "color": "green", "italic": true},
      {"type": "text", "text": "下划线文本", "color": "green", "underlined": true},
      {"type": "text", "text": "删除线文本", "color": "green", "strikethrough": true}
    ]
  },

  "minecraft:compass": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "指南针", "color": "#FF5555", "bold": true},
      {"type": "text", "text": "位置: {player_x}, {player_y}, {player_z}", "color": "white"},
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
      {"type": "text", "text": "红石", "color": "#FF0000", "bold": true},
      {"type": "chart", "chartType": "pie", "width": 80,
       "entries": [
         {"label": "红石粉", "value": 60, "color": "#FF0000"},
         {"label": "红石火把", "value": 25, "color": "#FF5555"},
         {"label": "红石中继器", "value": 15, "color": "#FFAAAA"}
       ]
      }
    ]
  },

  "minecraft:wheat_seeds": {
    "type": "vbox",
    "gap": 4,
    "children": [
      {"type": "text", "text": "小麦种子", "color": "#55AA55", "bold": true},
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
      {"type": "text", "text": "画", "color": "#AAAAAA", "bold": true},
      {"type": "entity", "entity": "minecraft:painting", "size": 48, "autoRotate": false, "offsetY": 8},
      {"type": "text", "text": "装饰性物品", "color": "gray"}
    ]
  }
}
```

## 模组开发者

### 事件钩子
```java
// 渲染前事件（修改或取消）
TipEventManager.onPreRender(event -> {
    event.setItemStack(customStack);
    // 或 event.cancel() 取消渲染
});

// 渲染后事件（添加额外信息）
TipEventManager.onPostRender(event -> {
    event.addExtraLine("来自其他模组的额外信息");
});

// 变量解析事件（注入自定义变量）
TipEventManager.onResolveVariable(event -> {
    if (event.getVariableName().equals("custom_var")) {
        event.setValue("custom_value");
    }
});
```

## 热重载

按 F3+T 或使用 `/reload` 命令重新加载 tooltip，无需重启游戏。

## 许可证

GNU LGPL 3.0
