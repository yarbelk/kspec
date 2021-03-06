package io.polymorphicpanda.kspec.junit

import io.polymorphicpanda.kspec.context.*
import org.junit.runner.Description
import java.io.Serializable
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class JUnitUniqueId private constructor(val id: Int): Serializable {
    companion object {
        var current = AtomicInteger()
        fun next(): JUnitUniqueId = JUnitUniqueId(current.andIncrement)
    }
}

class JUnitTestDescriber: ContextVisitor {
    val contextDescriptions = LinkedHashMap<Context, Description>()

    private fun className(context: Context?): String {
        if (context == null) {
            return ""
        }

        return when(context) {
            is ExampleContext -> "${className(context.parent)}.${context.description}"
            is ExampleGroupContext -> {
                val parent = className(context.parent)

                if (parent.isEmpty()) {
                    return context.description
                }

                "$parent.${context.description}"
            }
            else -> ""
        }

    }

    override fun preVisitExampleGroup(context: ExampleGroupContext): ContextVisitResult {
        contextDescriptions.put(context, Description.createSuiteDescription(context.description, JUnitUniqueId.next()))
        return ContextVisitResult.CONTINUE
    }

    override fun postVisitExampleGroup(context: ExampleGroupContext): ContextVisitResult {
        val current = contextDescriptions[context]

        if (context.parent != null) {
            val parent = contextDescriptions[context.parent!!]
            parent!!.addChild(current)
        }

        return ContextVisitResult.CONTINUE
    }

    override fun onVisitExample(context: ExampleContext): ContextVisitResult {
        val desc = Description.createTestDescription(
                className(context.parent), context.description, JUnitUniqueId.next()
        )
        contextDescriptions.put(
                context,
                desc
        )

        val parent = contextDescriptions[context.parent]
        parent!!.addChild(desc)

        return ContextVisitResult.CONTINUE
    }

}

