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

- [ ] Criar o projeto no Firebase e cadastrar o catálogo real (categorias e locais)
- [ ] Trocar os placeholders `bg_banner_recife` e `foto_perfil` pelas imagens do grupo
- [ ] Aplicar o visual do protótipo nas telas Início, Busca, Local e Roteiro (a FAQ já está pronta)
- [ ] Exibir fotos dos locais (carregar `imagens` com Coil)
- [ ] Ajustar a FAQ: ela cita exportação em PDF, que no documento é evolução futura (seção 24)
