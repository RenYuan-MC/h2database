import fs from 'node:fs';
import path from 'node:path';
import vm from 'node:vm';
import { fileURLToPath } from 'node:url';
const dir = path.dirname(fileURLToPath(import.meta.url));
vm.runInThisContext(fs.readFileSync(path.join(dir, '..', 'wasm-gc-runtime.js'), 'utf8'));
globalThis.fetch = async url => new Response(fs.readFileSync(String(url)), {
  headers: { 'Content-Type': 'application/wasm' }
});
for (const [name, cases] of [
  ['catch-fixed', [[0, 2], [1, 0]]],
  ['weak-fixed', [[42, 42], [123, 123]]]
]) {
  const instance = await TeaVM.wasmGC.load(path.join(dir, name, 'probe.wasm'), {});
  for (const [input, expected] of cases) {
    const actual = instance.exports.run(input);
    if (actual !== expected) throw new Error(`${name}: ${actual} != ${expected}`);
    console.log(`${name}.run(${input}) = ${actual}: PASS`);
  }
}
