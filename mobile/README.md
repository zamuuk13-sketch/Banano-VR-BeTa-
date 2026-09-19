# 📱 BananoVR Mobile

## Etapa 3 — Hand Tracking

O BananoVR agora possui a primeira camada real de tracking das mãos.

A detecção usa MediaPipe Hand Landmarker sobre o pipeline CameraX. O Hand Landmarker suporta até duas mãos e, em LIVE_STREAM, entrega resultados de forma assíncrona usando tracking entre frames para reduzir latência. citeturn2search0

### Implementado

- CameraX + ImageAnalysis;
- KEEP_ONLY_LATEST para não acumular frames antigos;
- processamento fora da UI;
- até 2 mãos;
- 21 landmarks por mão;
- esqueleto desenhado sobre a câmera;
- resultados assíncronos;
- base pronta para gestos e envio ao PC.

O Android recomenda fechar cada ImageProxy e usar uma estratégia de backpressure apropriada para análise contínua. citeturn0search0turn0search3

### Modelo

A biblioteca usada é com.google.mediapipe:tasks-vision:0.10.29, versão encontrada no exemplo oficial atual do Hand Landmarker consultado para esta etapa. citeturn2search4

O modelo deve estar em mobile/app/src/main/assets/hand_landmarker.task.

### Head tracking a 60 FPS

Importante: o **head tracking não será feito pela câmera**.

Na etapa de sensores vamos usar giroscópio/rotação diretamente, com uma pipeline independente da inferência visual. O Android fornece sensores de movimento e rotação apropriados para jogos, AR e orientação. citeturn1search0

A meta do BananoVR para o head tracking é **60 Hz com baixa latência e sem depender do FPS da câmera**. Não vamos prometer 60 FPS da detecção de mãos em qualquer aparelho, porque isso depende do sensor, iluminação, resolução e custo da inferência.

## Próxima etapa

Etapa 4 — Hand Tracking avançado: mão esquerda/direita, dedos, articulações, gestos, estabilidade e dados preparados para o PC.
