# 📱 BananoVR Mobile

## Etapa 2 — Câmera

A câmera agora possui uma implementação real usando **Android CameraX**.

### O que já funciona

- solicitação da permissão de câmera;
- preview da câmera em tela cheia;
- câmera traseira;
- troca para a câmera frontal;
- retorno para a traseira;
- tratamento básico de câmera indisponível;
- ciclo de vida gerenciado pelo CameraX.

O CameraX foi escolhido porque fornece uma API moderna de câmera e permite combinar posteriormente **Preview** com **ImageAnalysis**, que será importante para o hand tracking. citeturn0search0turn0search1

### Próxima etapa

A próxima etapa adicionará o tracking das mãos sobre o fluxo de câmera.
