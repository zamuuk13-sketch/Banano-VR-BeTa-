# 🍌 BananoVR

> Transforme seu celular em uma experiência VR conectada ao PC.

**BananoVR** é um projeto experimental de realidade virtual que transforma um smartphone em um headset VR conectado a um PC.

A proposta é simples: **o celular cuida da experiência física e dos sensores; o PC cuida do processamento pesado.**

## 🎯 Proposta

**📱 Celular → 🖥️ PC → 🥽 VR**

O celular poderá fornecer câmera, tracking de mãos, giroscópio, acelerômetro, outros sensores, tela estéreo e comunicação com o PC.

O PC ficará responsável pelo processamento pesado, renderização, streaming, gerenciamento de ambientes, jogos e integração com o SteamVR original.

## 🧩 Arquitetura planejada

~~~text
BANANOVR
├── 📱 Mobile
│   ├── Camera
│   ├── Hand Tracking
│   ├── Gyroscope / Motion Sensors
│   ├── VR Display
│   └── Communication
├── 🖥️ PC
│   ├── Qt / C++
│   ├── BananoVR Core
│   ├── SteamVR Integration
│   ├── Game Launcher
│   ├── Environment Manager
│   └── Streaming
└── 🌎 3D
    └── Godot 4
        ├── Environment Viewer
        ├── GLB / GLTF
        ├── Spawn Points
        ├── VR Camera
        └── 3D Transitions
~~~

**Qt/C++** será a interface principal do PC e o gerenciamento do sistema.

**BananoVR Core** cuidará de comunicação, protocolo, tracking, sensores e estado da experiência.

**Godot 4** será apenas o módulo visual 3D; não será o núcleo do aplicativo.

**Mobile** será responsável pela câmera, sensores, tracking e exibição VR.

## 🥽 SteamVR

O BananoVR **não pretende recriar o SteamVR**.

A proposta é utilizar o SteamVR original instalado no PC e desenvolver a integração necessária para que ele reconheça o sistema BananoVR.

~~~text
BananoVR
   ↓
Prepara conexão
   ↓
Inicia integração
   ↓
Abre SteamVR original
   ↓
SteamVR utiliza o sistema BananoVR
~~~

## 🎮 Jogos

A área **Meus jogos** deverá permitir adicionar executáveis EXE instalados no PC e iniciá-los pelo BananoVR.

A compatibilidade VR dependerá de cada jogo e da integração disponível.

## 🗺️ Ambientes

O projeto deverá permitir importar ambientes 3D personalizados, começando por formatos como:

- GLB / GLTF
- OBJ
- FBX
- outros formatos compatíveis com o pipeline

Depois da importação, o usuário poderá visualizar o ambiente e criar múltiplos Spawn Points com posição, rotação e altura.

Cada ambiente poderá possuir metadados próprios.

## 📱 Tracking

O projeto pretende utilizar APIs e SDKs existentes sempre que possível, em vez de implementar algoritmos complexos de tracking do zero.

Dados planejados:

~~~text
HEAD
├── Position
├── Rotation
└── Motion

LEFT HAND
├── Palm
├── Wrist
└── Fingers

RIGHT HAND
├── Palm
├── Wrist
└── Fingers

IMU
├── Gyroscope
├── Accelerometer
└── Orientation

DEVICE
├── Battery
├── Connection
└── Timestamp
~~~

A frequência real dependerá do hardware e das APIs disponíveis.

## 🔌 Comunicação

O BananoVR deverá trabalhar com USB e Wi-Fi.

A prioridade será baixa latência, estabilidade e reconexão.

## 🎨 Filosofia visual

A interface não seguirá uma estética cyberpunk exagerada.

A proposta é simples, moderna, limpa, escura, confortável, com animações suaves e controles fáceis de entender.

A complexidade técnica deverá ficar escondida atrás de uma interface simples.

## 🏠 Interface planejada

~~~text
🍌 BANANOVR

🏠 Início
🥽 SteamVR
🎮 Meus jogos
🗺️ Ambientes
🎵 Música
⚙️ Configurações
~~~

## 🚧 Estado atual

### Etapa 0 — Organização do projeto
- [x] Organização do repositório
- [x] Documentação inicial
- [x] Estrutura modular

### Próximas etapas mobile
- [ ] Base do aplicativo
- [ ] Câmera
- [ ] Hand Tracking
- [ ] Tracking avançado
- [ ] Sensores
- [ ] Posicionamento
- [ ] Tracking Lab
- [ ] Teste 3D
- [ ] Modo VR
- [ ] Comunicação USB
- [ ] Wi-Fi
- [ ] Integração mobile

### Depois do mobile
- [ ] Aplicativo PC
- [ ] BananoVR Core
- [ ] Gerenciador de ambientes
- [ ] Streaming PC → celular
- [ ] Integração SteamVR
- [ ] Controles VR
- [ ] Integração com jogos

## 📁 Estrutura inicial

~~~text
Banano-VR-BeTa-/
├── README.md
├── LICENSE
├── .gitignore
├── docs/
│   ├── ARCHITECTURE.md
│   ├── ROADMAP.md
│   └── PROTOCOL.md
├── mobile/
│   └── README.md
├── pc/
│   └── README.md
├── core/
│   └── README.md
├── godot/
│   └── README.md
└── assets/
    └── README.md
~~~

---

## 🍌 BananoVR

**Um celular. Um PC. Uma experiência VR.**

Este é o repositório oficial de desenvolvimento do BananoVR.
