# Descubra Recife

Aplicativo Android nativo (Kotlin) para explorar pontos turísticos do Recife por categoria, montar roteiros personalizados e avaliar os locais visitados.

Projeto Integrador 4 + Mobile · Faculdade Nova Roma · Prof. Francisco Jakes Cardoso Dourado

Esta versão (`1.0.0`) cobre a etapa **Início do Desenvolvimento: ambiente e estrutura base**. Ela segue o *Documento de Arquitetura de Solução* v1.0.

---

## 1. Ambiente de desenvolvimento

| Ferramenta | Versão usada | Observação |
|---|---|---|
| Android Studio | Ladybug (2024.2) ou mais novo | IDE oficial (seção 12 do doc.) |
| JDK | 17 | já vem embutido no Android Studio |
| Gradle (wrapper) | 8.11.1 | baixado automaticamente pelo `./gradlew` |
| Android Gradle Plugin | 8.7.3 | |
| Kotlin | 2.0.21 | |
| compileSdk / targetSdk | 35 (Android 15) | |
| minSdk | 26 (Android 8.0) | |
| Emulador | Pixel com API 35 | ou um celular físico com depuração USB |

### Passo a passo

1. Instale o [Android Studio](https://developer.android.com/studio). No primeiro uso, ele baixa o Android SDK.
2. `File > Open` e escolha esta pasta (`DescubraRecife`). Aguarde o *Gradle Sync*.
3. Em *Device Manager*, crie um emulador (Pixel, API 35).
4. Escolha a variante **devDebug** em *Build Variants* e clique em ▶ Run.

Sem nenhuma configuração extra, o app abre em **modo demonstração**: o login aceita qualquer e-mail com uma senha de 6 ou mais caracteres, e o catálogo usa locais de exemplo em memória. Assim dá para desenvolver as telas antes de o Firebase estar pronto.

Pelo terminal:

```bash
./gradlew assembleDevDebug      # gera o APK de desenvolvimento
./gradlew testDevDebugUnitTest  # roda os testes unitários
```

### Conectando ao Firebase

1. No [console do Firebase](https://console.firebase.google.com), crie o projeto `descubra-recife`.
2. Adicione **dois apps Android**: `com.descubra.recife` (produção) e `com.descubra.recife.dev` (desenvolvimento).
3. Ative **Authentication > E-mail/senha** e crie o **Firestore Database**.
4. Baixe o `google-services.json` (ele já traz os dois apps) e coloque em `app/google-services.json`.
5. Publique as regras de [`firestore.rules`](firestore.rules) em *Firestore > Regras*.
6. Faça o Sync do Gradle de novo. O plugin do Google Services só é aplicado quando o arquivo existe. Na tela de Perfil deve aparecer "Firebase conectado".

> O `google-services.json` está no `.gitignore`. Cada membro do grupo baixa o seu do console, e ele nunca vai para o repositório.

### Cadastrando o catálogo inicial

As regras do Firestore não deixam o app gravar categorias e locais. Por isso, o catálogo inicial (5 categorias e 13 pontos turísticos do Recife, em [`tools/seed/dados.json`](tools/seed/dados.json)) é enviado por um script que usa uma chave de administrador. Para rodar, é preciso ter o Node.js instalado.

1. No console do Firebase, abra **Configurações do projeto → Contas de serviço → Gerar nova chave privada**. Salve o arquivo em `tools/seed/`.
2. Rode:
   ```bash
   cd tools/seed
   npm install
   npm run simular                                   # lista o que vai ser gravado
   node seed.mjs descubra-recife-firebase-adminsdk-XXXX.json
   ```
3. Confira os dados em **Firestore Database → Dados**. Rodar o script de novo só atualiza os mesmos documentos, sem duplicar nada.

> ⚠️ A chave dá acesso total ao projeto. Ela está no `.gitignore` e **não pode ir para o GitHub nem ser compartilhada**. Depois do cadastro, você pode apagar o arquivo e revogar a chave no console.

Para mudar ou adicionar locais, edite o `dados.json` e rode o script de novo. As coordenadas são aproximadas, então vale conferir cada uma no Google Maps. Horários e valores devem ser confirmados nos canais oficiais de cada local.

### Ambientes (seção 12)

| Ambiente | Variante Gradle | applicationId |
|---|---|---|
| Desenvolvimento | `devDebug` | `com.descubra.recife.dev` |
| Homologação | `devRelease` (APK para testes internos do grupo) | `com.descubra.recife.dev` |
| Produção | `prodRelease` (APK entregue na disciplina) | `com.descubra.recife` |

Como os applicationIds são diferentes, as versões dev e prod podem ficar instaladas lado a lado no mesmo celular.

---

## 2. Estrutura base

Estrutura em camadas **UI → ViewModel → Repositório → Firebase/Room** (seção 8), no padrão **MVVM**, com a interface em **Jetpack Compose**.

```
app/src/main/java/com/descubra/recife/
├── MainActivity.kt              # única Activity, hospeda o Compose
├── DescubraRecifeApp.kt         # Application: cria o AppContainer
├── di/AppContainer.kt           # injeção de dependências manual (Firebase ou demo)
├── domain/model/                # Cliente, Categoria, Local, Avaliacao, Roteiro (seção 9)
├── data/
│   ├── repository/              # interfaces + implementações Firebase
│   │   ├── Repositorios.kt          AuthRepository, CatalogoRepository, AvaliacaoRepository, RoteiroRepository
│   │   ├── FirebaseAuthRepository.kt
│   │   ├── FirestoreRepositorios.kt
│   │   ├── RoteiroRepositoryImpl.kt Room (offline) + Firestore (sincronização)
│   │   └── RascunhoRoteiro.kt       locais escolhidos antes de salvar
│   ├── local/                   # Room: AppDatabase, RoteiroDao, entidades
│   ├── remote/                  # nomes das coleções do Firestore
│   └── demo/                    # repositórios em memória (modo demonstração)
├── ui/
│   ├── theme/                   # cores do app (laranja #D97736, claro/escuro)
│   ├── components/              # barra de navegação, ícones, card de local
│   ├── navigation/NavHost.kt    # rotas e navegação
│   └── screens/                 # auth, home, busca, local, roteiro, perfil, faq
└── util/Mapas.kt                # "Como chegar" via Intent (ADR-004)

tools/seed/                      # script que cadastra o catálogo inicial no Firestore
```

### Modelo de dados no Firestore

```
categorias/{id}            nome, icone, slug
locais/{id}                titulo, descricao, endereco, custoAcesso, horarioFuncionamento,
                           contato, latitude, longitude, categoriaIds[], imagens[]
avaliacoes/{id}            clienteId, localId, nota (1–5), comentario, data
clientes/{uid}             nome, email, dataNascimento, telefone
clientes/{uid}/roteiros/{id}   dataGeracao, localIds[]
```

As associativas `Categoria_Local` e `Roteiro_Local` do documento viram os arrays `categoriaIds` e `localIds`, o jeito comum de modelar N:N no Firestore. A entidade `Imagem` (1:N com Local) vira o array `imagens` com as URLs.

No **Room**, as tabelas `roteiros` e `roteiro_locais` guardam uma cópia de cada roteiro com os dados básicos dos locais. Assim o roteiro abre mesmo sem internet (RA-004).

### Interface (protótipo do grupo)

As telas seguem o protótipo do Figma: splash com a máscara, login, cadastro, início, menu lateral, editar perfil, roteiro e FAQ. A tela de detalhe do local não estava no protótipo e usa o mesmo estilo das outras.

- **Fonte:** Poppins (`res/font`).
- **Cores:** laranja `#D97736`, pêssego `#FFD8B1` e a textura de cerâmica no topo das telas.
- **Regra de contraste:** todo texto e ícone sobre laranja é branco (`SobreLaranja` em `ui/theme/Theme.kt`).
- **Imagens:** o banner, a máscara, os ícones das categorias e as fotos dos locais foram recortados do protótipo e ficam em `res/drawable-nodpi`. Locais sem foto mostram a textura do banner.
- **Ajustes de usabilidade em relação ao protótipo:**
  - Os cards de destino mostram nome e bairro em branco sobre um degradê, em vez da etiqueta minúscula.
  - O "Seu Roteiro" vira um roteiro único por cliente, salvo a cada local adicionado.
  - Os textos do cadastro foram passados para o português do Brasil.
  - O "Código de confirmação" e o login com Google ficam para uma próxima etapa.

### Rastreabilidade (seção 25)

| Requisito | Onde está no código |
|---|---|
| RA-001 Kotlin nativo | projeto inteiro |
| RA-002 Cadastro/login | `FirebaseAuthRepository`, `ui/screens/auth` |
| RA-003 Locais, Categorias, Avaliações, Roteiros | `FirestoreRepositorios`, `ui/screens/home`, `local`, `roteiro` |
| RA-004 Roteiro offline | `data/local` (Room), `RoteiroRepositoryImpl` |
| RA-005 Sincronizar roteiros | `FirestoreRoteiroRemoto`, `RoteiroRepositoryImpl.sincronizar` |
| RA-006 Escalabilidade | Firestore gerenciado |

### Pendências da seção 26 resolvidas nesta etapa

| Pendência | Decisão |
|---|---|
| Compose vs. Views/XML | **Jetpack Compose** (a tela de FAQ do grupo já estava em Compose) |
| Padrão de arquitetura | **MVVM** com repositórios |
| Regras de acesso do Firestore | rascunho em [`firestore.rules`](firestore.rules) |
| Versionamento | SemVer `1.0.0` (`versionName`) e `versionCode` incremental |

Continuam em aberto: a estratégia de backup e o critério de exclusão e retenção de dados.

---

## 3. Próximos passos

- [x] Criar o projeto no Firebase (`descubra-recife`, apps dev e prod)
- [ ] Rodar o script de cadastro do catálogo e revisar as coordenadas
- [ ] Foto de perfil do cliente (hoje o avatar mostra a inicial do nome)
- [x] Aplicar o visual do protótipo nas telas
- [ ] Trocar as fotos recortadas do protótipo por fotos em boa resolução
- [ ] Exibir fotos dos locais (carregar `imagens` com Coil)
- [ ] Ajustar a FAQ: ela cita exportação em PDF, que no documento é evolução futura (seção 24)
