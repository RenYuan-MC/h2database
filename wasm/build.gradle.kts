import org.teavm.gradle.api.OptimizationLevel

plugins {
    id("java")
    id("org.teavm") version "0.12.1-EAGLER-R3"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    main {
        java {
            srcDirs("../h2/src/main")
            // Optional integrations whose third-party dependencies are not available
            // (and not needed) in the browser build.
            exclude("org/h2/server/web/**")
            exclude("org/h2/tools/Server.java")
            exclude("org/h2/tools/Shell.java")
            exclude("org/h2/tools/Console.java")
            exclude("org/h2/tools/GUIConsole.java")
            exclude("org/h2/util/OsgiDataSourceFactory.java")
            exclude("org/h2/util/DbDriverActivator.java")
            exclude("org/h2/util/geometry/JTSUtils.java")
            exclude("org/h2/util/ThreadDeadlockDetector.java")
            exclude("org/h2/fulltext/FullTextLucene.java")
            exclude("org/h2/message/TraceWriterAdapter.java")
            // Authentication back ends that need javax.xml / javax.security.auth /
            // javax.naming / java.net, which are not available in the browser build.
            exclude("org/h2/security/auth/DefaultAuthenticator.java")
            exclude("org/h2/security/auth/H2AuthConfigXml.java")
            exclude("org/h2/security/auth/impl/JaasCredentialsValidator.java")
            exclude("org/h2/security/auth/impl/LdapCredentialsValidator.java")
        }
        resources {
            srcDirs("src/main/resources", "../h2/src/main")
            include("org/h2/res/**")
            include("META-INF/services/**")
        }
    }
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://eaglercraft-teavm-fork.github.io/maven/")
    }
    mavenCentral()
}

dependencies {
    implementation("org.teavm:teavm-jso-apis:0.12.1-EAGLER-R3")
    // Needed for org.teavm.classlib.ResourceSupplier (resource embedding entry point).
    compileOnly("org.teavm:teavm-core:0.12.1-EAGLER-R3")
}

// Eaglercraft 的 classlib 是 Java 11 字节码，且它的字符串拼接走 StringBuilder；
// 这里禁用 javac 的 invokedynamic 拼接（StringConcatFactory），以便和 TeaVM
// wasmGC 后端支持的形态一致。
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-XDstringConcat=inline")
}

tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

teavm.wasmGC {
    targetFileName = "h2wasm.wasm"
    mainClass = "org.h2.wasm.H2Wasm"
    optimization = OptimizationLevel.AGGRESSIVE
    outOfProcess = false
    processMemory = 1024
    debugInformation = true
}



