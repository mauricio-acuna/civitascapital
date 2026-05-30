package com.magenta.customers.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Valida las reglas de arquitectura hexagonal definidas en ARCHITECTURE.md § 4.
 */
@DisplayName("Hexagonal Architecture Rules")
class HexagonalArchTest {

    private static JavaClasses classes;

    @BeforeAll
    static void loadClasses() {
        classes = new ClassFileImporter().importPackages("com.magenta.customers");
    }

    @Test
    @DisplayName("El paquete domain NO importa Spring ni JPA")
    void domainHasNoSpringNorJpaDependency() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().accessClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "javax.persistence.."
                );
        rule.check(classes);
    }

    @Test
    @DisplayName("El paquete application NO importa infraestructura")
    void applicationHasNoInfrastructureDependency() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().accessClassesThat()
                .resideInAPackage("..infrastructure..");
        rule.check(classes);
    }

    @Test
    @DisplayName("Los controllers residen en infrastructure.adapter.in.web")
    void controllersInCorrectPackage() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                .should().resideInAPackage("..infrastructure.adapter.in.web..");
        rule.check(classes);
    }
}
