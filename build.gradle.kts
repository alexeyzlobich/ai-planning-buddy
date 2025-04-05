import com.google.protobuf.gradle.id

plugins {
    alias(libs.plugins.shadow)
    alias(libs.plugins.micronaut.application)
    alias(libs.plugins.micronaut.aot)
    alias(libs.plugins.protobuf)

    jacoco
    alias(libs.plugins.sonarqube)
}

version = "0.1"
group = "com.taskmanager"

repositories {
    mavenCentral()
}


val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    annotationProcessor(libs.micronaut.data.processor)
    annotationProcessor(libs.micronaut.http.validation)
    annotationProcessor(libs.micronaut.serde.processor)
    annotationProcessor(libs.lombok)
    annotationProcessor("io.micronaut.openapi:micronaut-openapi")
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")

    implementation(libs.micronaut.reactor)
    implementation(libs.micronaut.serde.jackson)
    implementation(libs.micronaut.mongo.sync)
    implementation(libs.javax.annotation)
    implementation(libs.micronaut.discovery)
    implementation(libs.micronaut.grpc)
    implementation(libs.lombok)
    implementation(libs.anthropic.java)
    implementation(libs.langchain4j)
    implementation(libs.langchain4j.anthropic)
    implementation("io.micronaut.validation:micronaut-validation")

    compileOnly(libs.micronaut.http.client)
    compileOnly("io.micronaut.openapi:micronaut-openapi-annotations")

    runtimeOnly(libs.logback.classic)
    runtimeOnly(libs.snakeyaml)

    testImplementation(libs.micronaut.http.client)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito.core)
    testImplementation(libs.jackson.databind)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.junit)

    mockitoAgent(libs.mockito.core) { isTransitive = false }
}


application {
    mainClass = "com.taskmanager.Application"
}
java {
    sourceCompatibility = JavaVersion.toVersion("21")
    targetCompatibility = JavaVersion.toVersion("21")
}

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/grpc")
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    plugins {
        id("grpc") {
            artifact = libs.grpc.java.get().toString()
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}


graalvmNative.toolchainDetection = false

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.taskmanager.*")
    }
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = true
        convertYamlToJava = true
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}

tasks.named<io.micronaut.gradle.docker.NativeImageDockerfile>("dockerfileNative") {
    jdkVersion = "21"
}

tasks.test {
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required = true
    }

    // Exclude gRPC generated code from coverage
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude("**/com/taskmanager/api/grpc/generated/**")
                exclude("**/config/*Config.class")
            }
        })
    )
}
