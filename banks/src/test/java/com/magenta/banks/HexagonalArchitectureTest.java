package com.magenta.banks;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Tests de arquitectura hexagonal / ports-and-adapters.
 * Verifica que las reglas de dependencia entre capas no se violen.
 * Ref: IDEABASE/inyectados/stack-tech_spec.md §5.2 Regla de dependencias
 */
@AnalyzeClasses(
        packages = "com.magenta.banks",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HexagonalArchitectureTest {

    private static final String DOMAIN      = "..domain..";
    private static final String APPLICATION = "..application..";
    private static final String ADAPTER     = "..adapter..";
    private static final String INFRA       = "..infrastructure..";

    // ── El dominio no debe depender de Spring, JPA, Kafka, Redis ni HTTP ──────

    @ArchTest
    static final ArchRule domain_does_not_depend_on_spring =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta.persistence..",
                            "org.hibernate..",
                            "org.apache.kafka..",
                            "org.springframework.data..",
                            "org.springframework.web..",
                            "org.springframework.security.."
                    )
                    .because("El dominio no debe depender de ningún framework técnico (hexagonal)");

    // ── La capa de aplicación no debe depender de adaptadores ─────────────────

    @ArchTest
    static final ArchRule application_does_not_depend_on_adapters =
            noClasses().that().resideInAPackage(APPLICATION)
                    .should().dependOnClassesThat()
                    .resideInAPackage(ADAPTER)
                    .because("La capa de aplicación sólo depende del dominio y puertos");

    // ── Los adaptadores no deben depender directamente entre sí ──────────────

    @ArchTest
    static final ArchRule inbound_adapters_do_not_depend_on_outbound =
            noClasses().that().resideInAPackage("..adapter.in..")
                    .should().dependOnClassesThat()
                    .resideInAPackage("..adapter.out..")
                    .because("Adaptadores de entrada no deben depender de adaptadores de salida");

    // ── El dominio no debe depender de la capa de aplicación ─────────────────

    @ArchTest
    static final ArchRule domain_does_not_depend_on_application =
            noClasses().that().resideInAPackage(DOMAIN)
                    .should().dependOnClassesThat()
                    .resideInAPackage(APPLICATION)
                    .because("El dominio es independiente de la capa de aplicación");

    // ── Los controllers son sólo en el paquete web ────────────────────────────

    @ArchTest
    static final ArchRule controllers_reside_in_web_package =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..adapter.in.web..")
                    .because("Los controllers REST deben residir en adapter.in.web");

    // ── Los repositorios JPA sólo en persistence ─────────────────────────────

    @ArchTest
    static final ArchRule jpa_entities_reside_in_persistence =
            classes().that().areAnnotatedWith("jakarta.persistence.Entity")
                    .should().resideInAPackage("..adapter.out.persistence..")
                    .because("Las entidades JPA deben residir en adapter.out.persistence");
}
