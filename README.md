# Create Mining Laser - Datapack Guide

This mod is **data-driven**: tiers, recipes, models, and textures can be added or replaced from a resource/data pack (or another mod).

This README shows:

- how to **add a new Tier** (data file),
- how to **add recipes** for that tier (data file),
- where to put the **model & textures** for the laser head,
- and what each field means.

> Examples below use `<namespace>`. Adjust namespaces as needed.

---

## Directory layout (TL;DR)

```
<namespace>/
├── data/
│   └── <namespace>/
│           ├── drill_tiers/
│           │   └── t10.json                <-- TierDef (data-driven)
│           └── recipes/
│               └── drill_core/
│                   └── t10_ores.json       <-- DrillCore recipe(s)
└── assets/
    └── <namespace>/
        ├── models/
        │   └── block/
        │       └── laser_head_t10.json     <-- Head model (inherits from base)
        └── textures/
            └── block/
                └── laser_head_t10.png      <-- Head texture (same UV layout as base)
```

---

## 1) Add a new **Tier** (data-driven)

A **tier** defines: which **core item** activates it, its **RPM gate**, its **stress at 128 RPM**, and which **laser head model** to render.

Create a file at:

```
data/<namespace>/drill_tiers/<tier_id>.json
```

### Tier JSON schema

```jsonc
{
  // (optional) if omitted, the id is inferred from the filename
  "id": "<namespace>:t10",

  // sort order in JEI/tooltips (lower = earlier)
  "order": 10,

  // the item that must be inserted into the controller to activate this tier
  "core_item": "<namespace>:drill_core_t10",

  // base stress draw at min_rpm (scaled by the config suScale)
  // e.g. 32.0 with suScale=1000 => 32,000 SU shown/consumed at min_rpm
  "stress_at_minRPM": 32.0,

  // RPM gate for this tier; the drill does nothing below this
  "min_rpm": 128,
  // (optional) used only to clamp the x2 speed bonus in code; UI pitch/volume scales up to this
  "max_rpm": 256,

  // the partial model to render as the head (block model ResourceLocation, without "models/")
  // this file will be under assets/create_mininglaser/models/block/...
  "head_partial": "<namespace>:block/laser_head_t10"
}
```

### Notes

- **`stress_at_minRPM`** is interpreted at the tier’s **`min_rpm`** (default 128). The config multiplier `suScale` is applied on top (see “Config” below).
- **`core_item`** can be **any existing item** (from your mod or others). You do **not** need a special item class—just reference it here.
- **`head_partial`** must point to a **block model** (see next section).

---

## 2) Laser head **model & texture**

Every tier points to a **partial block model** via `head_partial`. New heads should **inherit** from the base head so you only swap textures.

Place files here:

- Model: `assets/<namespace>/models/block/laser_head_t10.json`
- Texture: `assets/<namespace>/textures/block/laser_head_t10.png`

> Keep your texture’s resolution and UV layout compatible with the base (the base uses a 64×64 sheet). If you change UVs, you’ll need a full model edit.

### Base model to inherit from

The mod ships a base head model (e.g. `laser_head_t1.json`). Your custom models should inherit from it.

### Minimal inheriting model

```json
{
  "parent": "create_mininglaser:block/laser_head_t1",
  "textures": {
    "1": "<namespace>:block/laser_head_t10r",
    "particle": "create_mininglaser:block/drill_casing"
  }
}
```

> If you exported a full model from Blockbench, you can still simplify it to a `parent` + `textures` override as above—this keeps everything consistent with Create’s partials.

---

## 3) Add **recipes** for a tier

Recipes tell the drill **what to roll** while the specified tier is active. They also support **dimension/biome filters** so you can restrict outputs (e.g., quartz only in Nether).

Create files at:

```
data/<namespace>/recipes/drill_core/<name>.json
```

### Recipe JSON schema

```jsonc
{
  "type": "create_mininglaser:drill_core",

  // link to your tier by id (the file name or "id" field of your TierDef)
  "tier": "<namespace>:t10",

  // base duration in ticks (20 ticks = 1 second) @ 1× speed
  // effective time is divided by speed multiplier (up to 2× at max_rpm)
  "duration": 200,

  // ordered drop table; the first successful roll returns
  "drops": [
    {
      "item": "minecraft:iron_ore",
      "chance": 0.35,      // 35% per completion
      "min": 1,
      "max": 2,

      // optional environment filter
      "env": {
        // only allow in these dimensions (resource locations)
        "dimensions": ["minecraft:overworld"],

        // biomes: either explicit ids...
        // "biomes": ["minecraft:old_growth_pine_taiga"]
        // ...or tags prefixed with '#'
        "biomes": 
        [
        "#minecraft:is_overworld",
        "minecraft:plains"
        ]
      }
    },

    // Try next entry if previous didn't roll
    {
      "item": "minecraft:copper_ore",
      "chance": 0.25,
      "min": 1,
      "max": 3
    }
  ]
}
```

#### Environment filter details

- `dimensions`: array of resource locations. Only matches when the controller is in one of these dimensions.
- `biomes`: array of **biome ids** (`"minecraft:desert"`) and/or **biome tags** (prefix with `"#"`, e.g. `"#minecraft:is_nether"`).  
  If **any** tag matches, the filter passes.

> If neither `dimensions` nor `biomes` is provided, the drop is allowed everywhere.

---

## 4) How it behaves in-game

- **RPM gate:** The drill **does nothing** below `min_rpm` for the active tier.
- **Speed multiplier:** At `min_rpm` you get **1×** speed; it scales linearly up to **2×** at `max_rpm` (clamped).
- **Stress draw:** At `min_rpm` the drill draws `stress_at_minRPM × suScale` SU.  
  The mod reports **impact per RPM** to Create so the network sees the correct load.
- **JEI:** Displays the tier’s core item, duration, stress (with config scale), and filters as friendly text.

---

## 5) Config (server config)

`create_mininglaser-common.toml`:

- `suScale` — global multiplier applied to all tiers’ `stress_at_minRPM`.  
  Example: `1000.0` means a tier value of `32.0` renders/consumes **32,000 SU** at `min_rpm`.

---

## 6) Troubleshooting

- **Recipe not showing / not rolling**
  - File path must be `data/<namespace>/recipes/drill_core/*.json`.
  - `"type"` must be exactly `"create_mininglaser:drill_core"`.
  - `"tier"` must match the tier’s **id** (from your tier JSON file name or `"id"` field).
  - Check logs on `/reload` for JSON or registry errors.

- **Tier not activating**
  - Make sure the **core item** in your tier JSON (`core_item`) exists and is the item you insert.
  - Ensure `min_rpm` is reachable by your Create network.

- **Head model not rendering**
  - `head_partial` must point to an existing block model JSON in `assets/<namespace>/models/block/...`
  - If you inherit, verify `"parent": "create_mininglaser:block/laser_head_t1"`.
  - Texture paths must exist under `assets/<namespace>/textures/block/`.

- **Texture looks scrambled**
  - Use the **same UV layout** as the base. Keep your texture **64×64** to match the base head unless you replicated all UVs.

---

## 7) Complete minimal examples

### Tier: `t10`

`data/rose_quartz_drills/drill_tiers/t10.json`

```json
{
  "id": "rose_quartz_drills:t10",
  "order": 10,
  "core_item": "rose_quartz_drills:rose_quartz_laser_t10",
  "stress_at_minRPM": 32.0,
  "min_rpm": 128,
  "max_rpm": 256,
  "head_partial": "rose_quartz_drills:block/laser_head_rose_quartz"
}
```

### Head model inheriting from base

`assets/rose_quartz_drills/models/block/laser_head_rose_quartz.json`

```json
{
  "parent": "create_mininglaser:block/laser_head_t1",
  "textures": {
    "1": "rose_quartz_drills:block/laser_head_rose_quartz",
    "particle": "create_mininglaser:block/drill_casing"
  }
}
```

`assets/rose_quartz_drills/textures/block/laser_head_rose_quartz.png`  
(64×64 PNG matching the base UVs)

### Recipe for that tier

`data/rose_quartz_drills/recipes/drill_core/rose_quartz_laser_ores.json`

```json
{
  "type": "create_mininglaser:drill_core",
  "tier": "rose_quartz_drills:t10",
  "duration": 200,
  "drops": [
    {
      "item": "minecraft:quartz_ore",
      "chance": 0.35,
      "min": 1,
      "max": 3,
      "env": { "dimensions": ["minecraft:the_nether"] }
    },
    {
      "item": "minecraft:iron_ore",
      "chance": 0.30,
      "min": 1,
      "max": 2,
      "env": { "biomes": ["#minecraft:is_overworld"] }
    }
  ]
}
```

---

## 8) Developer notes

- Tiers are looked up at runtime from the datapack registry (`TierDefs`).  
  If you really need to hard-register tiers in code, you can still call `TierDefs.register(new TierDef(...))` during common setup, but prefer datapacks so pack makers can extend your mod.
- The renderer reads `head_partial` directly; no extra Java registration is required.

---

## 9) License & contributions

Feel free to open issues/PRs with new example tiers and recipes. Include your datapack folder so others can test quickly.

---

If anything in this guide doesn’t match your build (field names or paths), check your log on `/reload` - the mod will print where it expects tier and recipe files, and any JSON parse errors.