# BananoVR — Protocolo

## Versão 1

O Android envia mensagens JSON pelo UDP.

### Portas
- Tracking: `27182/UDP`
- Discovery planejada: `27183/UDP`

### Cabeçalho lógico
```json
{"type":"bananovr.tracking","version":1,"timestampNanos":0}
```

### Head
Yaw, pitch, roll, posição estimada, velocidade, quaternion e frequência do sensor.

### Hands
Cada mão pode conter lado, confiança, gesto, timestamp e 21 landmarks XYZ.

### Observação
A posição mobile atual é estimada por integração de aceleração e pode sofrer drift. O PC deve tratá-la como posição estimada até existir posicionamento visual/absoluto.

### Pipeline
```text
Mobile Sensor/Camera → BananoVRPacket v1 → UDP → TrackingReceiver → Core → Qt / Godot / SteamVR
```
