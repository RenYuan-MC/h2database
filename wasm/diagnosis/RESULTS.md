# H2 / TeaVM wasmGC diagnosis — 2026-10-24

The original H2 and TeaVM source files were not changed. All patch sources,
compiler overrides, regression programs and experiment outputs are in this directory.

## Confirmed causes

1. The existing MVStore module fails at byte 495987 (`0x79173`). Its external
   `.teadbg` maps this address to `SingleFileStore.lockFileChannel`, line 137.
   The catch at line 136 names `java.nio.channels.OverlappingFileLockException`,
   which is absent from the current TeaVM classlib. The instruction uses source
   type 26 (`Throwable`) and target type 19 (an isolated placeholder structure).
   Target type 19 is not a subtype of source type 26.
2. Local additions to `TReference` introduce `referent` and `queue` instance fields.
   `WeakReferenceGenerator` still accesses field 2 as the native `externref` slot.
   In the reproduced module the actual native slot is field 4; field 2 is the
   Java referent. Constructor, dereference and clear generation all use the
   obsolete constant. This explains both directions of the reference mismatch.

## Patches and validation scope

- `patch-src/.../TOverlappingFileLockException.java`: minimal missing exception.
- `patch-src/.../TReference.java`: restore a fieldless base class while keeping
  the added constructor signatures. `get()` returns null. This preserves the
  existing stub scope; it does not implement PhantomReference collection/enqueue.
- `backend-missing-catch.patch`: skip a catch type absent from the linked class
  source. Tested independently, without the classlib override.
- `backend-weak-offset.patch`: use the last field of the exact WeakReference
  structure instead of constant field 2 in constructor/get/clear. Tested
  independently, with the original classlib. This patch alone does not repair
  the current PhantomReference semantics.

Both backend patches passed `git apply --check` against the existing TeaVM checkout.
They have not been applied to that checkout.

## Regressions verified with Node 24.19.0 / V8 13.6.233.17-node.51

| Experiment | Module validation | Runtime |
| --- | --- | --- |
| Existing MVStore module | FAIL: br_on_cast 19 / 26 | Not instantiated |
| CatchProbe, original classlib, SIMPLE | FAIL: br_on_cast 9 / 10 | Not instantiated |
| CatchProbe, missing exception supplied, FULL | PASS | run(0)=2, run(1)=0 |
| CatchProbe, backend guard only, FULL | PASS | run(0)=2, run(1)=0 |
| WeakProbe, original classlib, SIMPLE and FULL | FAIL: externref written to Java referent | Not instantiated |
| WeakProbe, fieldless Reference, FULL | PASS | run(42)=42, run(123)=123 |
| WeakProbe, backend offset patch only, FULL | PASS | Validation only; PhantomReference remains incorrect |
| H2Probe, two classlib corrections, existing Gradle build, AGGRESSIVE | PASS | Database operations not tested |
| Full H2Wasm, two classlib corrections, independent TeaVMTool FULL build | PASS | Database operations not tested |
| Full H2Wasm, two classlib corrections, existing Gradle build, AGGRESSIVE | PASS | Database operations not tested |

`SIMPLE` / `FULL` are TeaVM core names for Gradle `NONE` / `AGGRESSIVE`.
The independent TeaVMTool experiments use their own settings; the Gradle
experiments are the comparison against the actual project configuration.

Chrome 142 was not verified: the browser tool could not connect to the local
test server. `regression.html` runs validation and the small runtime assertions
in a browser. From the wasm project run `node test/serve.mjs 8732`, then open
`http://localhost:8732/diagnosis/regression.html`. The server used during this
investigation was stopped.

## Reproduce the isolated Gradle builds

Run from `F:\RenYuan-Survival\h2database\wasm` in PowerShell. The precompiled
classlib overrides are in `diagnosis/patch-classes`; their Java sources are in
`diagnosis/patch-src`. The init script prepends the overrides and changes the
output directory and enables symbol names. It does not change the checked-in
build file or overwrite the original failing wasm.

```powershell
.\gradlew.bat --offline --no-configuration-cache `
  '-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m' `
  --init-script diagnosis\patched-build.init.gradle generateWasmGC
node test\validate.mjs diagnosis\gradle-h2-fixed\h2wasm.wasm

.\gradlew.bat --offline --no-configuration-cache `
  '-Dorg.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m' `
  --init-script diagnosis\patched-build.init.gradle `
  '-PdiagnosisEntry=org.h2.wasm.H2Wasm' `
  '-PdiagnosisOutput=gradle-full-h2-fixed' generateWasmGC
node test\validate.mjs diagnosis\gradle-full-h2-fixed\h2wasm.wasm

node diagnosis\run-mini.mjs
```

The first attempt at the full Gradle build exhausted the default 512 MiB Gradle
heap. With `outOfProcess=false`, TeaVM's `processMemory` setting does not enlarge
the Gradle JVM; the command above sets the Gradle heap explicitly. With the 2 GiB
heap the full Gradle build completed successfully in 1 minute 38 seconds and
the resulting module passed `new WebAssembly.Module(bytes)` with empty options.

## Supporting evidence

- `h2-probe-before.wat`: disassembly of the original failing artifact.
- `weak-before.wat`: named disassembly showing the write to `$referent` at field 2
  while `$nativeRef` is field 4.
- `ReadDebug.java`: reads wasmGC ExternalDebugFile, not the JavaScript debug format.
- `src/probe/CatchProbe.java` and `src/probe/WeakProbe.java`: minimal regression inputs.
- Upstream missing-catch issue: https://github.com/konsoletyper/teavm/issues/1218

The original `test/wasmtypes.mjs` has an unrelated decoder problem: its heaptype
reader assumes most non-small indices are one-byte abstract heap types. Positive
type indices >=64 require signed LEB decoding. Use TeaVM's disassembler instead.
