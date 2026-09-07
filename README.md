# Healthbar Indicators

A RuneLite plugin that flashes small game-sprite icons above your character when tracked effects expire, activate, or hit a configurable threshold.




https://github.com/user-attachments/assets/732baf43-9190-4ae7-ab5f-ca0329a644b6




## Features

- **Track potions, buffs, and debuffs** — choose from 20+ effects including combat potions, divine potions, stamina, antifire, antipoison, thralls, and more
- **Setup presets** — create multiple named setups (e.g. "Bossing", "Slayer", "PvP") and switch between them from the side panel
- **Two indicator modes** — "On Expire" shows when an effect wears off; "While Active" shows the entire time an effect is active (useful for debuffs like Teleblock)
- **Optional blinking** — disable blinking to keep triggered indicators continuously visible
- **Drop threshold alerts** — for skill-boost potions, set a boost level to start flashing before the effect fully expires (e.g. flash when Super Combat drops below +9)
- **Timeout** — each tracked effect has a configurable timeout so indicators don't flash forever if you stop that activity. Minutes and seconds are added together (for example, 0 minutes + 30 seconds). Both zero means no timeout. Existing loadouts retain their saved minutes with zero additional seconds.
- **Smart positioning** — icons automatically avoid overlapping with overhead prayer icons and the in-game healthbar
- **Zoom-aware scaling** — icons scale with camera zoom with adjustable sensitivity
- **Divine potion detection** — correctly handles divine potions that maintain a fixed boost, so you don't get false expiry alerts when the divine effect ends

## Configuration

| Setting | Description | Default |
|---------|-------------|---------|
| Flash Rate | How fast icons blink on/off (ms) | 500 |
| Blink Icons | Blink triggered indicators instead of showing them continuously | On |
| Icon Size | Base size of indicator icons (px) | 20 |
| Zoom Sensitivity % | How much icons scale with zoom (0 = fixed, 100 = full) | 0 |
| Invert Zoom Scaling | Icons grow when zooming out (like the healthbar) | Off |
| X/Y Offset | Fine-tune icon position | 0 |

## Prayer Point Bar settings

Enable **Show Prayer Point Bar** in the plugin settings' **Prayer Point Bar** section.
The light-blue bar shows current prayer points, with translucent gray for the depleted
portion. It sits below the standard overhead health bar and is covered by interfaces.
**Only with health bar** defaults to on; disable it to show the bar throughout gameplay.
These settings apply across all setups, including empty setups. The feature defaults off.
The bar touches the lower edge of the standard health bar. Below **Low prayer threshold**
(default 10 points), the depleted portion alternates red and gray using **Flash Rate (ms)**.
The blue fill stays steady. Set the threshold to 0 to disable the warning.
Placement approximates the standard small health bar; alternate health-bar skins may differ.

## Tracked Effects

### Potions
Super Combat, Super Attack, Super Strength, Super Defence, Ranging, Magic, Attack, Strength, Defence, Combat, Bastion, Battlemage, Saradomin Brew, Ruby Harvest (+ Mix), Sapphire Glacialis (+ Mix), Black Warlock (+ Mix), Prayer Regeneration, and all Divine variants

### Buffs
Stamina, Antifire, Super Antifire, Antipoison, Antivenom, Imbued Heart, Saturated Heart, Magic Imbue, Vengeance, Moonlight Potion, NMZ Absorption, Ring of Endurance, Shadow Veil, Death Charge, Mark of Darkness, Ward of Arceuus, Thrall Active, Thrall Cooldown

### Debuffs
Teleblock, In Wilderness


## Images
#### Plugin settings
<img width="270" height="567" alt="settings" src="https://github.com/user-attachments/assets/0d2b9d56-ac72-402c-9615-790fa225c660" />


#### Setup Page
<img width="277" height="297" alt="setup" src="https://github.com/user-attachments/assets/f639edc5-307d-4327-9733-aa464ba06614" />

#### Tracker Page
<img width="240" height="485" alt="Screenshot 2026-09-07 123455" src="https://github.com/user-attachments/assets/79b09890-80b0-42a9-b426-504eeba1b25b" />


