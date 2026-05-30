package com.magenta.areas.arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
    packages = "com.magenta.areas",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

    // ── Regla 1: El dominio no debe depender de Spring, JPA ni Kafka ──────────

    @ArchTest
    static final ArchRule domainDoesNotDependOnFrameworks =
        noClasses()
            .that().resideInAPackage("com.magenta.areas.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "javax.persistence..",
                "org.apache.kafka..",
                "org.hibernate..",
                "com.fasterxml.jackson.."
            )
            .because("El dominio debe ser puro Java — sin dependencias de framework");

    // ── Regla 2: Los casos de uso (application) no dependen de infrastructure ─

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure =
        noClasses()
            .that().resideInAPackage("com.magenta.areas.application..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.magenta.areas.infrastructure..")
            .because("La capa application no puede acoplarse a adaptadores de infraestructura");

    // ── Regla 3: Los controllers REST no acceden a repositorios directamente ──

    @ArchTest
    static final ArchRule controllersDoNotAccessRepositoriesDirectly =
        noClasses()
            .that().resideInAPackage("com.magenta.areas.infrastructure.adapter.in.web..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.magenta.areas.infrastructure.adapter.out.persistence..",
                "com.magenta.areas.infrastructure.adapter.out.kafka.."
            )
            .because("Los controllers sólo deben acceder a puertos de entrada (use cases)");

    // ── Regla 4: Los adaptadores out no importan adaptadores in ───────────────

    @ArchTest
    static final ArchRule outAdaptersDoNotDependOnInAdapters =
        noClasses()
            .that().resideInAPackage("com.magenta.areas.infrastructure.adapter.out..")
            .should().dependOnClassesThat()
            .resideInAPackage("com.magenta.areas.infrastructure.adapter.in..")
            .because("Los adaptadores de salida no deben acoplarse a los de entrada");

    // ── Regla 5: No hay ciclos entre paquetes de primer nivel ─────────────────

    @ArchTest
    static final ArchRule noCyclicDependencies =
        slices()
            .matching("com.magenta.areas.(*)..")
            .should().beFreeOfCycles()
            .because("Los bounded contexts internos deben estar libres de ciclos");
}
