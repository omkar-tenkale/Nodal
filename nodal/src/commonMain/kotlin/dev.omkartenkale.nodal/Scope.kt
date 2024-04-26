package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.exceptions.DependencyNotFoundException
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.instance.ScopedInstanceFactory
import org.koin.core.logger.Level
import org.koin.core.logger.Logger
import org.koin.core.logger.MESSAGE
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass
import org.koin.core.scope.Scope as KoinScope

public class Scope(public val name: String, public val koinScope: KoinScope) {
    public var isClosed: Boolean = false
        private set

    public constructor(name: String, parentScope: Scope) : this(
        name, createKoinScope(name, parentScope)
    )

    public companion object {
        public fun createKoinScope(name: String, parentScope: Scope): KoinScope {
            val scope = parentScope.koinScope.getKoin().createScope(
                "$name:${hashCode()}",
                org.koin.core.qualifier.StringQualifier("$name:${hashCode()}")
            )
            scope.linkTo(parentScope.koinScope)
            return scope
        }
    }

    @OptIn(KoinInternalApi::class)
    public fun <T : Any> declareDefinition(
        primaryType: KClass<*>, definition: Definition<T>, qualifier: Qualifier? = null
    ) {

        val def = BeanDefinition(
            scopeQualifier = koinScope.scopeQualifier,
            primaryType = primaryType,
            qualifier = qualifier?.toKoinQualifier(),
            definition = { definition.invoke(this@Scope) },
            kind = Kind.Scoped
        )
        val indexKey = indexKey(def.primaryType, def.qualifier, def.scopeQualifier)
        val existingFactory =
            koinScope.getKoin().instanceRegistry.instances[indexKey] as? ScopedInstanceFactory
        if (existingFactory != null) {
            existingFactory.refreshInstance(koinScope.id, definition.invoke(this@Scope))
        } else {
            val factory = ScopedInstanceFactory(def)
            koinScope.getKoin().instanceRegistry.saveMapping(false, indexKey, factory)
        }
        println("REGISTER ${primaryType.simpleName} in $name  ${if (qualifier == null) "globally" else "for $qualifier"}")
    }

    public inline fun <reified T> get(
        qualifier: Qualifier? = null
    ): T = get(T::class, qualifier)

    public fun <T> get(kClass: KClass<*>, qualifier: Qualifier? = null): T {
        if (isClosed) error("Scope $name is closed")
        return try {
            println("Looking for ${kClass.simpleName} with self scope ${selfQualified(qualifier).toKoinQualifier().value}")
            koinScope.get(kClass, selfQualified(qualifier).toKoinQualifier())
        } catch (e: NoDefinitionFoundException) {
            return try {
                println("Looking for ${kClass.simpleName} with no scope")
                koinScope.get(kClass, qualifier?.toKoinQualifier())
            } catch (e: NoDefinitionFoundException) {
                throw DependencyNotFoundException(kClass, name, e)
            }
        }
    }

    public inline fun <reified T> getOrNull(
        qualifier: Qualifier? = null
    ): T? = getOrNull(T::class, qualifier)

    public fun <T> getOrNull(kClass: KClass<*>, qualifier: Qualifier? = null): T? {
        return try {
            get(kClass, qualifier)
        } catch (t: Throwable) {
            null
        }
    }

    public fun close() {
        isClosed = true
        koinScope.close()
    }

    public fun selfQualified(qualifier: Qualifier? = null): StringQualifier =
        StringQualifier("${name}:${qualifier?.value ?: ""}")
}

internal val emptyDependencyDeclaration: DependencyDeclaration = {}
public fun createRootScope(dependencyDeclaration: DependencyDeclaration = emptyDependencyDeclaration): Scope {
    val koinApp = koinApplication {
        printLogger(Level.DEBUG)
    }
    val rootScope = koinApp.koin.createScope(
        ROOT_SCOPE_ID, org.koin.core.qualifier.StringQualifier(ROOT_SCOPE_ID)
    )
    val scope = Scope(name = ROOT_SCOPE_ID, koinScope = rootScope)
    koinApp.koin.loadModules(listOf(module {
        single { koinApp }
    }))
    dependencyDeclaration.invoke(DependencyDeclarationDSL(scope))
    return scope
}

internal const val ROOT_SCOPE_ID = "NODAL_ROOT_SCOPE"

internal fun Qualifier?.toKoinQualifier(): org.koin.core.qualifier.Qualifier {
    return when (this) {
        is StringQualifier -> org.koin.core.qualifier.StringQualifier(value)
        else -> error("Must use ${StringQualifier::class.qualifiedName} as qualifier")
    }
}

public interface Qualifier {
    public val value: QualifierValue
}

internal typealias QualifierValue = String

public data class StringQualifier(override val value: QualifierValue) : Qualifier {
    override fun toString(): String {
        return value
    }
}

internal typealias DependencyDeclaration = DependencyDeclarationDSL.() -> Unit

internal typealias Definition<T> = Scope.() -> T

public class DependencyDeclarationDSL(public val scope: Scope) {
    public inline fun <reified T : Any> provides(
        qualifier: Qualifier? = null,
        noinline definition: Definition<T>,
    ) {
        scope.declareDefinition(T::class, definition, qualifier)
    }

    public fun <T : Any> provides(
        primaryType: KClass<*>,
        qualifier: Qualifier? = null,
        definition: Definition<T>,
    ) {
        scope.declareDefinition(primaryType, definition, qualifier)
    }

    public inline fun <reified T : Any> providesSelf(
        qualifier: Qualifier? = null,
        noinline definition: Definition<T>,
    ) {
        provides(scope.selfQualified(qualifier), definition)
    }

    public fun <T : Any> providesSelf(
        primaryType: KClass<*>,
        qualifier: Qualifier? = null,
        definition: Definition<T>,
    ) {
        provides(primaryType, scope.selfQualified(qualifier), definition)
    }

    public fun include(dependencyDeclaration: DependencyDeclaration): Unit = dependencyDeclaration()
}