package com.learnfirebase.ecommerce.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.learnfirebase.ecommerce");

    @Test
    void domainShouldNotDependOnOutside() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "..application..",
                        "..adapter..",
                        "..infrastructure..")
                .because("Domain model should be pure and free of frameworks")
                .check(importedClasses);
    }

    @Test
    void applicationShouldNotDependOnOutside() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "..adapter..",
                        "..infrastructure..")
                .because("Application layer should not depend on frameworks or details (Adapters/Infrastructure)")
                .check(importedClasses);
    }

    @Test
    void adaptersShouldDependOnApplicationAndDomain() {
        classes()
                .that().resideInAPackage("..adapter..")
                .should().onlyDependOnClassesThat().resideInAnyPackage(
                        "com.learnfirebase.ecommerce..adapter..",
                        "com.learnfirebase.ecommerce..application..",
                        "com.learnfirebase.ecommerce..domain..",
                        "com.learnfirebase.ecommerce..common..",
                        "org.springframework..",
                        "jakarta..",
                        "java..",
                        "org.slf4j..",
                        "com.fasterxml.jackson..",
                        "io.swagger..",
                        "org.apache..",
                        "lombok.."
                )
                .because("Adapters should act as ports to the outside world, calling application use cases")
                .check(importedClasses);
    }

    @Test
    void adaptersShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..adapter..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .because("Adapters should not directly depend on infrastructure details")
                .check(importedClasses);
    }

    @Test
    void infrastructureShouldNotDependOnAdapters() {
        noClasses()
                .that().resideInAPackage("..infrastructure..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..")
                .because("Infrastructure details should not depend on adapters")
                .check(importedClasses);
    }

    @Test
    void domainAndApplicationShouldNotDependOnAdapterDtos() {
        noClasses()
                .that().resideInAnyPackage("..domain..", "..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapter..dto..")
                .because("Domain and application layers should not leak adapter-specific DTOs")
                .check(importedClasses);
    }

    @Test
    void contextsShouldNotDependOnOtherContextsInternalLayers() {
        String[] contexts = {"cart", "order", "identity", "product", "notification", "inventory", "logistics", "promotion", "report", "chat"};
        for (String ctx1 : contexts) {
            for (String ctx2 : contexts) {
                if (!ctx1.equals(ctx2)) {
                    noClasses()
                            .that().resideInAPackage("..com.learnfirebase.ecommerce." + ctx1 + "..")
                            .should().dependOnClassesThat().resideInAnyPackage(
                                    "..com.learnfirebase.ecommerce." + ctx2 + ".adapter..",
                                    "..com.learnfirebase.ecommerce." + ctx2 + ".infrastructure.."
                            )
                            .because("Context " + ctx1 + " should not depend on context " + ctx2 + "'s internal adapter or infrastructure layers")
                            .check(importedClasses);
                }
            }
        }
    }
}
