package org.runestar.client.updater.deob.rs

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.kxtra.slf4j.getLogger
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.Type.INT_TYPE
import org.objectweb.asm.Type.LONG_TYPE
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import org.runestar.client.updater.common.invert
import org.runestar.client.updater.deob.Transformer
import org.runestar.client.updater.deob.util.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger

object MultiplierAnnotations : Transformer {

    private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    private val logger = getLogger()

    override fun transform(source: Path, destination: Path) {
        val classNodes = readJar(source)

        val multFile: Path = source.resolveSibling(source.fileName.toString() + ".mult.json")
        check(Files.exists(multFile))

        val annoDecoders: Map<String, Number> = mapper.readValue(multFile.toFile())

        var numValueInjections = 0
        var numValueInjectionsMissed = 0;
        for (mult in annoDecoders.keys) {
            val clasz = classNodes.find { classNode -> classNode.name == mult.split(".")[0] }
            if (clasz != null) {
                val field = clasz.fields.find { field -> field.name == mult.split(".")[1] }
                if (field !=null) {
                    if (annoDecoders[mult] is Long) {
                        field.visitAnnotation("Lnet/runelite/mapping/ObfuscatedGetter;", true).visit("longValue", annoDecoders[mult])
                        numValueInjections++
                    } else {
                        field.visitAnnotation("Lnet/runelite/mapping/ObfuscatedGetter;", true).visit("intValue", annoDecoders[mult])
                        numValueInjections++
                    }
                } else {
                    numValueInjectionsMissed++
                }
            } else {
                numValueInjectionsMissed++
            }
        }

        Logger.getAnonymousLogger().info("Added " + numValueInjections + " ObfuscatedGetter Annotations, missed " + numValueInjectionsMissed)

        writeJar(classNodes, destination)
    }
}