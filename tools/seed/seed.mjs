// Cadastra categorias e locais de dados.json no Firestore.
//
// As regras do Firestore não deixam o app escrever no catálogo, então este
// script usa o Admin SDK com a chave de uma conta de serviço (ver README).
//
// Uso:
//   node seed.mjs --simular                 mostra o que seria gravado, sem gravar
//   node seed.mjs caminho/da/chave.json     grava no Firestore
//
// Os ids dos documentos são fixos, então rodar de novo só atualiza, sem duplicar.
// Categorias e locais que saíram do dados.json são apagados do Firestore.

import { readFile } from "node:fs/promises";

const args = process.argv.slice(2);
const simular = args.includes("--simular");
const caminhoChave = args.find((a) => !a.startsWith("--"));

const dados = JSON.parse(await readFile(new URL("./dados.json", import.meta.url), "utf8"));
validar(dados);

const documentos = [
  ...dados.categorias.map(({ id, ...campos }) => ({ colecao: "categorias", id, campos })),
  ...dados.locais.map(({ id, ...campos }) => ({
    colecao: "locais",
    id,
    campos: { imagens: [], ...campos },
  })),
];

if (simular) {
  for (const d of documentos) console.log(`${d.colecao}/${d.id}`);
  console.log(`\n${documentos.length} documentos seriam gravados (simulação, nada foi enviado).`);
  process.exit(0);
}

if (!caminhoChave) {
  console.error("Informe o caminho da chave da conta de serviço: node seed.mjs chave.json");
  process.exit(1);
}

const { initializeApp, cert } = await import("firebase-admin/app");
const { getFirestore } = await import("firebase-admin/firestore");

const chave = JSON.parse(await readFile(caminhoChave, "utf8"));
initializeApp({ credential: cert(chave) });
const db = getFirestore();

const lote = db.batch();
for (const d of documentos) lote.set(db.collection(d.colecao).doc(d.id), d.campos);

const removidos = [];
for (const colecao of ["categorias", "locais"]) {
  const ids = new Set(documentos.filter((d) => d.colecao === colecao).map((d) => d.id));
  for (const doc of (await db.collection(colecao).get()).docs) {
    if (!ids.has(doc.id)) {
      lote.delete(doc.ref);
      removidos.push(`${colecao}/${doc.id}`);
    }
  }
}
await lote.commit();
if (removidos.length) console.log("Removidos (não estão mais no dados.json):", removidos.join(", "));

console.log(`Projeto ${chave.project_id}: ${dados.categorias.length} categorias e ${dados.locais.length} locais gravados.`);

function validar({ categorias, locais }) {
  const idsCategorias = new Set(categorias.map((c) => c.id));
  const erros = [];
  for (const l of locais) {
    for (const c of l.categoriaIds) {
      if (!idsCategorias.has(c)) erros.push(`${l.id}: categoria "${c}" não existe`);
    }
    if (typeof l.latitude !== "number" || typeof l.longitude !== "number") {
      erros.push(`${l.id}: latitude/longitude precisam ser números`);
    }
  }
  if (erros.length) {
    console.error("dados.json tem problemas:\n- " + erros.join("\n- "));
    process.exit(1);
  }
}
