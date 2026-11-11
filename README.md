# Control+ · Aplicativos para Android e Windows

Este projeto gera, **automaticamente no GitHub**, o `ControlPlus.apk` (Android) e o `ControlPlus.exe` (Windows).
Os dois abrem o Control+ em tela cheia, sem barra de navegador e sem a faixa do Google.
Quando o Control+ é atualizado no Apps Script, os aplicativos **não precisam ser reinstalados**.

## Como gerar (uma vez só)

1. Crie uma conta em https://github.com (gratuita) e clique em **New repository**.
   - Nome: `control-plus-apps` · marque **Private** · clique em **Create repository**.
2. Na página do repositório, clique em **uploading an existing file** e **arraste todo o conteúdo desta pasta**
   (inclusive a pasta `.github`). Clique em **Commit changes**.
   - Se a pasta `.github` não subir: **Add file → Create new file**, digite o nome
     `.github/workflows/gerar-aplicativos.yml` e cole o conteúdo desse arquivo.
3. Abra o arquivo `config/app-url.txt`, clique no lápis ✏️, apague o texto e cole o **link do Control+**
   (o que termina em `/exec`). Clique em **Commit changes**.
4. Vá na aba **Actions**: a geração começa sozinha e leva uns 5 a 8 minutos.
5. Quando ficar verde ✅, vá em **Releases** (lado direito da página inicial do repositório)
   e baixe `ControlPlus.apk` e `ControlPlus.exe`.

## Instalar

- **Android:** abra o `ControlPlus.apk` no celular. Se pedir, autorize "Instalar apps desconhecidos" para o navegador/arquivos.
- **Windows:** abra o `ControlPlus.exe`. Na primeira vez o Windows avisa ("O Windows protegeu o computador"):
  clique em **Mais informações → Executar assim mesmo**. Dica: clique com o botão direito no ícone da barra de tarefas → **Fixar**.

## Observações

- Precisa de internet (os dados ficam na sua planilha do Google).
- Se um dia o link do Control+ mudar, edite `config/app-url.txt`: o GitHub gera versões novas sozinho.
- O APK é sempre assinado com a mesma chave (`android/app/controlplus.jks`), então versões novas instalam por cima.
  Mantenha o repositório **privado**.
