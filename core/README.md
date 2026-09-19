# 🧠 BananoVR Core

Núcleo compartilhado do projeto.

## Etapa 12

A primeira fronteira real entre transporte e Core foi criada através de `TrackingReceiver`.

O receptor entrega pacote bruto e metadados por callback, mantendo rede independente de Qt, Godot e SteamVR.

Próximas evoluções: parser tipado, sincronização de relógio, reconexão e seleção automática USB/Wi-Fi.
