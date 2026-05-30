package com.magenta.servicios.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Verifica las reglas de dependencia de la arquitectura hexagonal:
 *
 *   adapter/in  --> application --> domain
 *   adapter/out --> application --> domain
 *   domain      --> ningún framework (Spring, JPA, Kafka, Redis, HTTP)
 */
class HexagonalArchitectureTest {

    private static final String BASE = "com.magenta.servicios";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE);
    }

    @Test
    void domain_must_not_depend_on_spring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.apache.kafka..",
                        "org.springframework.data.redis..",
                        "org.springframework.web..",
                        "org.springframework.security.."
                )
                .because("El dominio no debe depender de ningún framework tecnológico");
        rule.check(classes);
    }

    @Test
    void domain_must_not_depend_on_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".domain..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE + ".infrastructure.adapter..")
                .because("El dominio no debe conocer los adaptadores");
        rule.check(classes);
    }

    @Test
    void application_must_not_depend_on_adapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".application..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE + ".infrastructure.adapter..")
                .because("La capa application no puede conocer los detalles de los adaptadores");
        rule.check(classes);
    }

    @Test
    void web_adapters_must_not_depend_on_out_adapters_directly() {
        ArchRule rule = noClasses()
                .that().resideInAPackage(BASE + ".infrastructure.adapter.in..")
                .should().dependOnClassesThat()
                .resideInAPackage(BASE + ".infrastructure.adapter.out..")
                .because("Los adaptadores de entrada deben hablar sólo con la capa application (ports/in)");
        rule.check(classes);
    }

    @Test
    void no_cyclic_dependencies_between_modules() {
        slices()
                .matching(BASE + ".(*)..")
                .should().beFreeOfCycles()
                .check(classes);
    }
}
