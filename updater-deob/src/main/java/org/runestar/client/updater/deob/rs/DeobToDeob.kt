package org.runestar.client.updater.deob.rs

import com.google.common.collect.ImmutableList
import org.kxtra.slf4j.getLogger
import org.runestar.client.updater.deob.Transformer
import org.runestar.client.updater.deob.util.readJar
import org.runestar.client.updater.deob.util.writeJar
import java.nio.file.Path
import java.util.logging.Logger

/*
    This is useful for moving annotations from one deob structure, to another,
    if for example, one has incorrect annotations. Very niche case.
 */
object DeobToDeob : Transformer {

    private val logger = getLogger()

    override fun transform(source: Path, destination: Path) {
        val runeliteClassNodes = readJar(Path.of("./runelite.jar"))
        val runestarClassNodes = readJar(Path.of("./runestar.jar"))

        val outputJar = Path.of("./FUCKYEABOY.jar")

        var classAnnotationsOverwritten = 0
        var fieldAnnotationsOverwritten = 0
        var methodAnnotationsOverwritten = 0
        for (c in runeliteClassNodes) {
            val clasz = runestarClassNodes.find { classNode -> classNode.name == c.name }
            if (clasz != null) {
                var targetAnnotationsCache = clasz.visibleAnnotations

                if (clasz.visibleAnnotations != null)
                    targetAnnotationsCache = ImmutableList.copyOf(clasz.visibleAnnotations)

                if (targetAnnotationsCache != null) {
                    clasz.visibleAnnotations.clear()

                    if (c.visibleAnnotations != null)
                        for (a in c.visibleAnnotations) {
                            clasz.visibleAnnotations.add(a)
                            classAnnotationsOverwritten++
                        }
                }

                for (f in c.fields) {
                    val field = c.fields.find { fieldNode -> fieldNode.name == f.name }
                    val runestarField = clasz.fields.find { fieldNode -> fieldNode.name == f.name }
                    if (field != null && runestarField != null) {
                        if (field.visibleAnnotations != null) {
                            targetAnnotationsCache = ImmutableList.copyOf(runestarField.visibleAnnotations)
                            if (targetAnnotationsCache != null) {
                                runestarField.visibleAnnotations.clear()

                                if (f.visibleAnnotations != null)
                                    for (a in f.visibleAnnotations) {
                                        runestarField.visibleAnnotations.add(a)
                                        println(field.name)
                                        fieldAnnotationsOverwritten++
                                    }
                            }
                        }
                    }
                }

                for (m in c.methods) {
                    val method = c.methods.find { methodNode -> methodNode.name == m.name }
                    val runestarMethod = clasz.methods.find { methodNode -> methodNode.name == m.name }
                    if (method != null && runestarMethod != null) {
                        if (runestarMethod.visibleAnnotations != null) {
                            targetAnnotationsCache = ImmutableList.copyOf(runestarMethod.visibleAnnotations)
                            if (targetAnnotationsCache != null) {
                                runestarMethod.visibleAnnotations.clear()

                                if (m.visibleAnnotations != null)
                                    for (a in m.visibleAnnotations) {
                                        runestarMethod.visibleAnnotations.add(a)
                                        methodAnnotationsOverwritten++
                                    }
                            }
                        }
                    }
                }
            }
        }

        Logger.getAnonymousLogger().info("Class annotations overwritten: " + classAnnotationsOverwritten)
        Logger.getAnonymousLogger().info("Field annotations overwritten: " + fieldAnnotationsOverwritten)
        Logger.getAnonymousLogger().info("Method annotations overwritten: " + methodAnnotationsOverwritten)
        writeJar(runestarClassNodes, outputJar)
    }
}