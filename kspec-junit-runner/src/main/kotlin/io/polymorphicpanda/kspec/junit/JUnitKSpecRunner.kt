package io.polymorphicpanda.kspec.junit

import io.polymorphicpanda.kspec.KSpec
import io.polymorphicpanda.kspec.Utils
import io.polymorphicpanda.kspec.annotation.Configurations
import io.polymorphicpanda.kspec.context.ExampleContext
import io.polymorphicpanda.kspec.context.ExampleGroupContext
import io.polymorphicpanda.kspec.runner.KSpecRunner
import io.polymorphicpanda.kspec.runner.RunListener
import org.junit.runner.Description
import org.junit.runner.Runner
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier

class JUnitKSpecRunner<T: KSpec>(val clazz: Class<T>): Runner() {
    val describer = JUnitTestDescriber()
    val spec = clazz.newInstance()

    val _description by lazy(LazyThreadSafetyMode.NONE) {
        spec.spec()
        spec.root.visit(describer)
        spec.disable()
        describer.contextDescriptions[spec.root]!!
    }

    override fun run(notifier: RunNotifier?) {
        val runner = KSpecRunner(spec.root, { config ->
                spec.configure(config)
            },
            Utils.findAnnotation(clazz.kotlin, Configurations::class)
        )

        val runNotifier = io.polymorphicpanda.kspec.runner.RunNotifier()

        runNotifier.addListener(object: RunListener {
            override fun exampleGroupStarted(group: ExampleGroupContext) { }

            override fun exampleGroupFailure(group: ExampleGroupContext, failure: Throwable) { }

            override fun exampleGroupFinished(group: ExampleGroupContext) { }

            override fun exampleGroupIgnored(group: ExampleGroupContext) { }

            override fun exampleStarted(example: ExampleContext) {
                notifier!!.fireTestStarted(describer.contextDescriptions[example])
            }

            override fun exampleFailure(example: ExampleContext, failure: Throwable) {
                notifier!!.fireTestFailure(Failure(describer.contextDescriptions[example], failure))
            }

            override fun exampleFinished(example: ExampleContext) {
                notifier!!.fireTestFinished(describer.contextDescriptions[example])
            }

            override fun exampleIgnored(example: ExampleContext) {
                notifier!!.fireTestIgnored(describer.contextDescriptions[example])
            }
        })

        runner.run(runNotifier)
    }

    override fun getDescription(): Description = _description
}
