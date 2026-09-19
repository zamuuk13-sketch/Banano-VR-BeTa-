# BananoVR — Arquitetura

O projeto é dividido em módulos para manter interface, núcleo de comunicação e visualização 3D desacoplados.

~~~text
Mobile
  │
  │ USB / Wi-Fi
  ▼
BananoVR Core
  ├── Tracking
  ├── Sensors
  ├── Protocol
  └── Connection
  │
  ├──► PC Application (Qt/C++)
  └──► 3D Module (Godot 4)
             └──► Environment / VR Visualization
~~~

## Mobile
Câmera, hand tracking, orientação, movimento, sensores, estado do dispositivo e modo VR.

## PC Application
Interface, configurações, conexão, jogos, ambientes e integração SteamVR.

## Core
Protocolo, comunicação, tracking, timestamps, sincronização e estado da conexão.

## Godot
Visualização 3D, ambientes, spawn points, câmera, iluminação e transições.

## SteamVR
Será utilizado como software externo. O BananoVR não substitui nem recria o SteamVR.
