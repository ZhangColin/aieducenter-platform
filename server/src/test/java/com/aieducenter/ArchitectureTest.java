package com.aieducenter;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import com.cartisan.core.stereotype.DomainService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * 架构守护测试。
 *
 * <p>仅分析生产代码（排除测试类），守护 DDD 六边形架构约束。</p>
 *
 * <p>已知例外：</p>
 * <ul>
 *   <li>{@code User} 直接使用 {@code BCryptPasswordEncoder}（SKILL.md DDD-005 明确许可）</li>
 * </ul>
 */
@AnalyzeClasses(packages = "com.aieducenter", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // ── 分层规则 ────────────────────────────────────────────────

    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("Domain layer should not depend on infrastructure layer");

    /**
     * 领域层不依赖 Spring。
     *
     * <p>例外：User 聚合根使用 BCryptPasswordEncoder（SKILL.md DDD-005）。</p>
     */
    @ArchTest
    static final ArchRule domainShouldNotDependOnSpring =
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .and(DescribedPredicate.not(simpleName("User")))
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .because("Domain layer should be framework-agnostic (exception: User uses BCryptPasswordEncoder per SKILL.md DDD-005)");

    @ArchTest
    static final ArchRule controllersShouldOnlyDependOnApplication =
        noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..domain..")
            .orShould().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("Controllers should only depend on application services");

    @ArchTest
    static final ArchRule applicationShouldNotAccessDatabaseDirectly =
        noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().haveFullyQualifiedName("jakarta.persistence.EntityManager")
            .orShould().dependOnClassesThat().haveFullyQualifiedName("jakarta.persistence.EntityManagerFactory")
            .orShould().dependOnClassesThat().resideInAPackage("java.sql..")
            .because("Application services should access data through Repository ports, not directly");

    // ── 命名规范 ────────────────────────────────────────────────

    @ArchTest
    static final ArchRule controllersShouldBeSuffixed =
        classes()
            .that().areAnnotatedWith(RestController.class)
            .should().haveSimpleNameEndingWith("Controller")
            .because("REST controllers should be suffixed with 'Controller'");

    @ArchTest
    static final ArchRule appServicesShouldBeSuffixed =
        classes()
            .that().areAnnotatedWith(Service.class)
            .and().resideInAPackage("..application..")
            .should().haveSimpleNameEndingWith("AppService")
            .because("Application services should be suffixed with 'AppService'");

    @ArchTest
    static final ArchRule domainServicesShouldBeSuffixed =
        classes()
            .that().areAnnotatedWith(DomainService.class)
            .should().haveSimpleNameEndingWith("Service")
            .because("Domain services should be suffixed with 'Service'");

    @ArchTest
    static final ArchRule repositoriesShouldBeSuffixed =
        classes()
            .that().areAnnotatedWith(Repository.class)
            .should().haveSimpleNameEndingWith("Repository")
            .because("Repositories should be suffixed with 'Repository'");

    // ── 禁止规则 ────────────────────────────────────────────────

    @ArchTest
    static final ArchRule noFieldInjection =
        noFields()
            .should().beAnnotatedWith(Autowired.class)
            .because("Use constructor injection instead of field injection");

    @ArchTest
    static final ArchRule noJavaUtilDate =
        noClasses()
            .should().dependOnClassesThat().haveFullyQualifiedName("java.util.Date")
            .because("Use java.time API instead of java.util.Date");

    @ArchTest
    static final ArchRule noFloatingPointForMoney =
        noFields()
            .that().haveNameMatching(".*(?i)(price|amount|fee|cost|balance|money|payment|refund|commission).*")
            .should().haveRawType(Double.class)
            .orShould().haveRawType(Float.class)
            .because("Use BigDecimal for monetary fields to avoid precision loss")
            .allowEmptyShould(true);
}
