### `server.properties`:

```properties
accepts-transfers=true
allow-flight=true
difficulty=peaceful
enforce-secure-profile=false
pvp=true
rate-limit=-1
simulation-distance=30
view-distance=30
```

### `bukkit.yml`:

```yaml
  connection-throttle: -1
```

### `spigot.yml`

```yaml
world-settings:
  default:
    entity-tracking-range:
      display: 600
settings:
  moved-too-quickly-multiplier: 200.0
```

### `paper-global.yml`:

```yaml
chunk-loading-advanced:
chunk-loading-basic:
  player-max-chunk-generate-rate: -1.0
  player-max-chunk-load-rate: -1.0
  player-max-chunk-send-rate: -1.0
```