## Micronaut 4.7.5 Documentation

- [User Guide](https://docs.micronaut.io/4.7.5/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.7.5/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.7.5/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow)

## Package structure

```
com.taskmanager/
├── application/             # Application layer. Business logic
│    ├── port/               # Ports (interfaces)
│    │    ├── inbound/       # Input ports (use cases/interactors)
│    │    └── outbound/      # Output ports (repositories/gateways)
│    ├── domain/             # Core domain model
│    │    ├── shared/        # Shared kernel, common value objects
│    │    └── task/          
│    └── service/            # Application services    
├── infrastructure/          # Infastructure layer. External concerns
│    ├── persistence/        # Database adapters
│    └── api/                # API adapters for external services
└── api/                     # Representation layer. Application inbound adapters
```

## Running the application

### Prerequisites
- JDK 21
- Gradle 8.5+
- MongoDB (local instance or container)

### Local Development

Start MongoDB:
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```
Run the application:
```bash
./gradlew run
```

Build and Run with Docker Compose:
```bash
./gradlew dockerBuildNative
docker compose up
```
The application will be available at http://localhost:8080

## Testing
Run the test suite:
```bash
./gradlew test
```

Test reports will be generated in:
* JaCoCo coverage: build/reports/jacoco/test/html/index.html
* Test results: build/reports/tests/test/index.html
