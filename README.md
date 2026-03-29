# Healthbar Indicators

A RuneLite plugin that flashes small game-sprite icons above your character when tracked effects expire, activate, or hit a configurable threshold.


https://github.com/user-attachments/assets/a37d92ef-f13f-4fc3-a484-5879c3c72216


## Features

- **Track potions, buffs, prayers, and debuffs** — choose from 60+ effects including combat potions, divine potions, stamina, antifire, antipoison, prayers, thralls, and more
- **Setup presets** — create multiple named setups (e.g. "Bossing", "Slayer", "PvP") and switch between them from the side panel
- **Two blink modes** — "On Expire" flashes when an effect wears off; "While Active" flashes the entire time an effect is active (useful for debuffs like Teleblock)
- **Drop threshold alerts** — for skill-boost potions, set a boost level to start flashing before the effect fully expires (e.g. flash when Super Combat drops below +9)
- **Timeout** — each tracked effect has a configurable timeout so indicators don't flash forever if you stop that activity
- **Smart positioning** — icons automatically avoid overlapping with overhead prayer icons and the in-game healthbar
- **Zoom-aware scaling** — icons scale with camera zoom with adjustable sensitivity
- **Divine potion detection** — correctly handles divine potions that maintain a fixed boost, so you don't get false expiry alerts when the divine effect ends

## Configuration

| Setting | Description | Default |
|---------|-------------|---------|
| Flash Rate | How fast icons blink on/off (ms) | 500 |
| Icon Size | Base size of indicator icons (px) | 20 |
| Zoom Sensitivity % | How much icons scale with zoom (0 = fixed, 100 = full) | 50 |
| Invert Zoom Scaling | Icons grow when zooming out (like the healthbar) | On |
| X/Y Offset | Fine-tune icon position | 0 |

## Tracked Effects

### Potions
Super Combat, Super Attack, Super Strength, Super Defence, Ranging, Magic, Attack, Strength, Defence, Combat, Bastion, Battlemage, Saradomin Brew, Prayer Regeneration, and all Divine variants

### Buffs
Stamina, Antifire, Super Antifire, Antipoison, Antivenom, Imbued Heart, Magic Imbue, Vengeance, Moonlight Potion, NMZ Absorption, Ring of Endurance, Shadow Veil, Death Charge, Thrall Active, Thrall Cooldown

### Debuffs
Teleblock, In Wilderness

### Prayers
All standard prayers, protection prayers, Piety, Rigour, Augury, Preserve, Deadeye, Mystic Vigour, and all Ruinous Powers prayers
