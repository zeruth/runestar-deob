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

object SignatureAnnotations : Transformer {

    private val mapper = jacksonObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)

    private val logger = getLogger()

    override fun transform(source: Path, destination: Path) {
        val classNodes = readJar(source)
        var opFile = destination.resolveSibling(destination.fileName.toString() + ".op.json").toFile()

        var annoDecoders: Map<String, String> = mapper.readValue(opFile)

        var garbageValueInjections = 0;
        var garbageValueMissedInjections = 0
        for (mult in annoDecoders.keys) {
            val clasz = classNodes.find { classNode -> classNode.name == mult.split(".")[0] }
            if (clasz != null) {
                val method = (clasz.methods.find { method -> method.name == mult.split(".")[1].split("(")[0] &&
                        method.desc == "(" + mult.split(".")[1].split("(")[1]})
                if (method !=null) {
                    method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("garbageValue", annoDecoders[mult])
                    garbageValueInjections++
                } else {
                    System.out.println("Didnt get Field")
                    garbageValueMissedInjections++
                }
            } else {
                System.out.println("Didnt get Class ")
                garbageValueMissedInjections++
            }
        }
        Logger.getAnonymousLogger().info("Added " + garbageValueInjections + " ObfuscatedSignature Annotations, missed " + garbageValueMissedInjections)


        var descriptionInjections = 0;
        var descriptionMissedInjections = 0
        for (mult in annoDecoders.keys) {
            val clasz = classNodes.find { classNode -> classNode.name == mult.split(".")[0] }
            if (clasz != null) {
                val method = (clasz.methods.find { method -> method.name == mult.split(".")[1].split("(")[0] &&
                        method.desc == "(" + mult.split(".")[1].split("(")[1]})
                if (method !=null) {

                    if (method.visibleAnnotations!=null) {
                        val annotation = method.visibleAnnotations.find { annotation -> annotation.desc == "Lnet/runelite/mapping/ObfuscatedSignature;"}
                        if (annotation != null) {
                            val garbageVal = annotation.values.get(1)
                            method.visibleAnnotations.remove(annotation)
                            if (method.signature!=null)
                            {
                                method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("signature", method.signature)
                            }
                            if (method.desc!=null)
                            {
                                val newAnnotation = method.visibleAnnotations.find { newAnnotation -> annotation.desc == "Lnet/runelite/mapping/ObfuscatedSignature;"}
                                if (newAnnotation!=null)
                                {
                                    newAnnotation.visit("garbageValue", method.desc)
                                    descriptionInjections++
                                }
                            }
                        } else {
                            println("Didnt get ObfuscatedSignature annotation (should already exist)")
                            descriptionMissedInjections++
                        }
                    } else {
                        method.visitAnnotation("Lnet/runelite/mapping/ObfuscatedSignature;", true).visit("signature", annoDecoders[mult])
                        descriptionInjections++
                    }
                } else {
                    System.out.println("Didnt get Field")
                    descriptionMissedInjections++
                }
            } else {
                System.out.println("Didnt get Class ")
                descriptionMissedInjections++
            }
        }
        Logger.getAnonymousLogger().info("Added " + descriptionInjections + " ObfuscatedSignature Annotations, missed " + descriptionMissedInjections)

        writeJar(classNodes, destination)
    }
}