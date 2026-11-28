# Sistemas distribuídos no Descubra Recife

Um sistema distribuído é um sistema em que várias máquinas trabalham juntas pela rede e, para quem usa, parecem um sistema só. No Descubra Recife, essas máquinas são o celular do usuário, os servidores do Firebase (Authentication e Firestore) e os servidores do Google.

O app usa duas formas principais de sistemas distribuídos: **replicação com consistência eventual** (o backup do roteiro) e **publish/subscribe em tempo real**. Usa também duas formas complementares: **autenticação federada** e **cache com tolerância a falhas** no catálogo.

---

## 1. Backup do roteiro: replicação com consistência eventual

### A ideia

O roteiro do usuário existe em **duas cópias (réplicas)** ao mesmo tempo:

| Réplica | Onde fica | Para que serve |
|---|---|---|
| Local | Banco Room, dentro do celular | O app abre o roteiro na hora, mesmo **sem internet** |
| Remota | Firestore, na nuvem (`clientes/{uid}/roteiros/atual`) | **Backup** na conta: o roteiro não se perde se o usuário trocar ou perder o celular, ou se o app perder o cache |

A tela sempre lê a cópia do celular. A cópia da nuvem é mantida igual a ela por **sincronização**. Por um tempo as duas podem ficar diferentes (por exemplo, sem internet), mas sempre acabam iguais quando a conexão volta. É isso que se chama **consistência eventual**.

### Quando sincroniza

1. **Toda vez que a aba Roteiro é aberta com internet**, o app busca a versão do servidor (`Source.SERVER`, que ignora o cache) e junta com a do celular. Assim a tela sempre mostra a versão mais atual.
2. **Ao entrar na conta**, inclusive em outro aparelho, o roteiro da nuvem é baixado.
3. **Ao adicionar ou remover um local**, a mudança é gravada no celular na hora e enviada para a nuvem. Se estiver sem internet, ela fica marcada como **pendente** e sobe na próxima sincronização.

Sem internet, a busca no servidor desiste depois de 8 segundos e a tela mostra a cópia do celular.

### Como os conflitos são resolvidos: "a última escrita vence"

Cada versão do roteiro guarda o **horário da última alteração** (`dataGeracao`). Quando as duas réplicas se encontram, o app aplica estas regras (arquivo `Sincronizacao.kt`):

| Situação | O que acontece |
|---|---|
| Roteiro só existe no celular e está pendente (feito offline) | **Sobe** para a nuvem |
| Roteiro só existe na nuvem (celular novo, app reinstalado) | **Baixa** para o celular |
| Existe nos dois e a mudança do celular é **mais recente** | O celular vence e **sobe** |
| Existe nos dois e a nuvem é **mais recente** (mudou em outro aparelho) | A nuvem vence e **baixa** |
| Já tinha sido sincronizado e sumiu da nuvem | Foi apagado em outro aparelho: **sai do celular** também |

Essa estratégia se chama **last-write-wins (LWW)**.

### Exemplos

- **Sem internet no Recife Antigo:** o usuário adiciona o Paço do Frevo. O app grava no celular e mostra "Sem internet · versão de 19/11/2026 às 11:40". Quando a internet volta e ele abre o roteiro, a mudança sobe e a tela passa a mostrar "Atualizado em 19/11/2026 às 11:40".
- **Celular novo, perdido ou cache apagado:** o usuário entra com a mesma conta e o roteiro volta inteiro da nuvem.
- **Dois aparelhos:** o roteiro mudou no tablet às 15h. No celular, que tinha a versão das 14h, a versão das 15h vence.

A data e a hora embaixo de "Seu Roteiro" mostram **de quando é a versão** que está na tela.

**Onde está no código:** `data/repository/Sincronizacao.kt` (regras), `RoteiroRepositoryImpl.kt` (aplica as regras no Room e no Firestore) e `data/local/` (Room). **Testes:** `SincronizacaoTest` (6 casos de conflito) e `RoteiroRepositoryTest` (sem internet, outro aparelho, versão mais nova).

---

## 2. Atualização em tempo real: publish/subscribe

### A ideia

No modelo comum, o app **pergunta** ao servidor se algo mudou. No **publish/subscribe**, o app **se inscreve** em um assunto e o servidor **avisa sozinho** quando ele muda.

Enquanto a aba Roteiro está aberta, o app fica inscrito na coleção do roteiro do usuário no Firestore (`addSnapshotListener`). Se o roteiro for alterado em **outro aparelho**, o Firestore envia a nova versão e a tela atualiza sem o usuário fazer nada. Quando a aba é fechada, a inscrição é cancelada, para não gastar bateria nem dados.

O app ignora dois tipos de aviso:
- os que vêm do **cache local**, porque não são a versão do servidor;
- os das **próprias escritas ainda não confirmadas** pelo servidor.

No teste no emulador, uma mudança feita direto na nuvem apareceu na tela em **3 segundos**.

**Onde está no código:** `FirestoreRoteiroRemoto.observar` (inscrição) e `RoteiroRepositoryImpl.acompanhar` (aplica no celular com as mesmas regras do item 1).

---

## 3. Outras formas presentes no app

**Autenticação federada (login com Google).** Quem confirma a identidade do usuário é o Google, outro sistema. Ele entrega ao app um token assinado (padrão OpenID Connect), que o Firebase Authentication valida nos próprios servidores. Por isso a mesma conta funciona em qualquer aparelho, e o perfil e a foto ficam no Firestore e acompanham o usuário.

**Cache com tolerância a falhas no catálogo.** Os locais e categorias vêm do Firestore, que guarda uma cópia no celular. O app tenta o servidor por até 4 segundos. Se não houver resposta (rede ruim ou sem internet), mostra a cópia guardada. O app continua funcionando quando uma parte do sistema distribuído (a rede ou o servidor) falha.

---

## Resumo para a apresentação

| Forma de sistema distribuído | O que faz no app | Onde ver |
|---|---|---|
| **Replicação + consistência eventual (LWW)** | Backup do roteiro na conta; funciona offline e sincroniza ao abrir a aba | Aba Roteiro: "Atualizado em…" / "Sem internet · versão de…" |
| **Publish/subscribe** | Roteiro atualiza sozinho quando muda em outro aparelho | Aba Roteiro aberta em dois aparelhos |
| Autenticação federada | Login com Google validado pelo Firebase | "Entrar com a conta Google" |
| Cache com tolerância a falhas | Catálogo abre mesmo sem internet | Home e detalhe do local offline |
