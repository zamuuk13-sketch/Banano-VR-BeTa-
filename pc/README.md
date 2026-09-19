# 🖥️ BananoVR PC

Aplicativo principal do BananoVR para Windows.

## Etapa 12 — integração final da base de tracking

A base PC agora possui um receptor UDP nativo em C++ usando Winsock no Windows:
- porta padrão de tracking: 27182;
- recepção assíncrona em thread própria;
- callback para entregar pacotes ao Core/UI;
- captura do IP/porta do celular;
- timestamp de recebimento;
- desligamento seguro do socket.

Arquivos:
- `src/BananoVR/TrackingReceiver.h`
- `src/BananoVR/TrackingReceiver.cpp`
- `src/BananoVR/DeviceDiscovery.h`

Fluxo:
```text
Android → BananoVRPacket → UDP :27182 → TrackingReceiver → Core → Qt/C++ / Godot / SteamVR
```