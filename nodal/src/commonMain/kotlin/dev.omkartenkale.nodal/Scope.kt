package dev.omkartenkale.nodal

import dev.omkartenkale.nodal.exceptions.DependencyInstantiationException
import dev.omkartenkale.nodal.exceptions.DependencyNotFoundException
import dev.omkartenkale.nodal.exceptions.DependencyRedeclarationException
import org.koin.core.KoinApplication
import org.koin.core.annotation.KoinInternalApi
import org.koin.core.definition.BeanDefinition
import org.koin.core.definition.Kind
import org.koin.core.definition.indexKey
import org.koin.core.error.DefinitionOverrideException
import org.koin.core.error.InstanceCreationException
import org.koin.core.error.NoDefinitionFoundException
import org.koin.core.instance.ScopedInstanceFactory
import org.koin.core.logger.Level
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.koin.ext.getFullName
import kotlin.reflect.KClass
import org.koin.core.scope.Scope as KoinScope

public class Scope(public val name: String, public val koinScope: KoinScope, public val parentScope: Scope?) {
    public var isClosed: Boolean = false
        private set

    public val qualifier: StringQualifier = StringQualifier(name)
    public constructor(name: String, parentScope: Scope) : this(
        name, createKoinScope(name, parentScope), parentScope
    )

    public companion object {
        public fun createKoinScope(name: String, parentScope: Scope): KoinScope = parentScope.koinScope.getKoin().createScope(
            name,
            org.koin.core.qualifier.StringQualifier(name)
        )
    }

    internal fun printAllDependencies(){
        @OptIn(KoinInternalApi::class)
        fun KoinApplication.print(){
            println("-------------------------------------------------")
            for(i in 0..< this.koin.instanceRegistry.instances.size){
                this.koin.instanceRegistry.instances.values.forEach{
                    println("Dependency: ${it.beanDefinition.toString()}")
                }
                this.koin.instanceRegistry.instances.entries
            }
            println("-------------------------------------------------")
        }
        koinScope.get<KoinApplication>().print()
    }

    @OptIn(KoinInternalApi::class)
    public fun <T : Any> declareDefinition(
        primaryType: KClass<*>, definition: Definition<T>, qualifier: Qualifier? = null
    ) {
        val def = BeanDefinition(
            scopeQualifier = this.qualifier.toKoinQualifier(),
            primaryType = primaryType,
            qualifier = qualifier?.toKoinQualifier(),
            definition = { definition.invoke(this@Scope) },
            kind = Kind.Scoped
        )
        val indexKey = indexKey(def.primaryType, def.qualifier, this.qualifier.toKoinQualifier())

        try {
            koinScope.getKoin().instanceRegistry.saveMapping(allowOverride = false, indexKey, ScopedInstanceFactory(def))
        }catch (e: DefinitionOverrideException){
            throw DependencyRedeclarationException(primaryType.simpleName ?: "Anonymous Class", this.name)
        }
    }

    public inline fun <reified T> get(
        qualifier: Qualifier? = null
    ): T = get(T::class, qualifier)

    public fun <T> get(kClass: KClass<*>, qualifier: Qualifier? = null): T = get(kClass, qualifier, true)

    internal fun <T> get(kClass: KClass<*>, qualifier: Qualifier? = null, checkSelf : Boolean): T {
        if (isClosed) error("Scope $name is closed")
        if(checkSelf){
            koinScope.getOrNull<T>(kClass, qualifier.selfQualified().toKoinQualifier())?.let{
                return it
            }
        }
        try {
            return koinScope.get(kClass, qualifier?.toKoinQualifier())
        } catch (e: NoDefinitionFoundException) {
            parentScope?.let{
                try {
                    return it.get(kClass, qualifier, false)
                } catch (e: NoDefinitionFoundException) {
                    throw DependencyNotFoundException(kClass, name, e)
                }
            }
            throw e
        }catch (e: InstanceCreationException) {
            throw DependencyInstantiationException(e)
        }
    }

    public inline fun <reified T> getOrNull(
        qualifier: Qualifier? = null
    ): T? = getOrNull(T::class, qualifier)

    public fun <T> getOrNull(kClass: KClass<*>, qualifier: Qualifier? = null): T? {
        return try {
            get(kClass, qualifier)
        } catch (e: DependencyNotFoundException) {
            null
        }
    }

    public fun close() {
        isClosed = true
        koinScope.close()
    }
}

public fun Qualifier?.selfQualified(): StringQualifier =
    StringQualifier("self:${this?.value ?: ""}")

internal val emptyDependencyDeclaration: DependencyDeclaration = {}
@OptIn(KoinInternalApi::class)
public fun createRootScope(dependencyDeclaration: DependencyDeclaration = emptyDependencyDeclaration): Scope {
    val koinApp = koinApplication {
        printLogger(Level.DEBUG)
    }
    val rootScope = koinApp.koin.createScope(
        ROOT_SCOPE_ID, org.koin.core.qualifier.StringQualifier(ROOT_SCOPE_ID)
    )

    val scope = Scope(name = ROOT_SCOPE_ID, koinScope = rootScope, parentScope = null)
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

public inline fun <reified T> qualifiedAs(): Qualifier = StringQualifier(T::class.getFullName())
public fun qualifiedAs(qualifier: String): Qualifier = StringQualifier(qualifier)

public class DependencyDeclarationDSL(public val scope: Scope) {
    public inline fun <reified T : Any> provides(
        qualifier: Qualifier? = null,
        noinline definition: Definition<T>,
    ) {
        provides(
            primaryType = T::class,
            qualifier = qualifier,
            definition = definition
        )
    }

    public fun <T : Any> provides(
        primaryType: KClass<*>,
        qualifier: Qualifier? = null,
        definition: Definition<T>,
    ) {
        scope.declareDefinition(
            primaryType = primaryType,
            definition = definition,
            qualifier = qualifier,
        )
    }

    public inline fun <reified T : Any> providesSelf(
        qualifier: Qualifier? = null,
        noinline definition: Definition<T>,
    ) {
        provides(
            qualifier = qualifier.selfQualified(),
            definition = definition
        )
    }

    public fun <T : Any> providesSelf(
        primaryType: KClass<*>,
        qualifier: Qualifier? = null,
        definition: Definition<T>,
    ) {
        provides(
            primaryType = primaryType,
            qualifier = qualifier.selfQualified(),
            definition = definition
        )
    }

    public fun include(dependencyDeclaration: DependencyDeclaration): Unit = dependencyDeclaration()
}