# BananoVR — Protocolo

Documento conceitual do protocolo entre mobile e PC.

## Princípios
- baixa latência;
- mensagens pequenas;
- timestamps;
- identificação do dispositivo;
- reconexão;
- evolução compatível.

## Dados planejados

### Head
- posição;
- rotação;
- velocidade quando disponível.

### Mãos
- palma;
- pulso;
- articulações dos dedos;
- confiança do tracking quando fornecida pelo SDK.

### IMU
- giroscópio;
- acelerômetro;
- orientação;
- timestamp.

### Device
- bateria;
- conexão;
- capacidades;
- timestamp.

## Pacote conceitual

~~~text
BANANOVR_PACKET
├── VERSION
├── TIMESTAMP
├── HEAD
├── LEFT_HAND
├── RIGHT_HAND
├── IMU
└── DEVICE
~~~

O formato definitivo de serialização será definido durante a implementação.
