package org.runestar.client.updater.deob;

import org.runestar.client.updater.deob.util.UtilKt;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args)
    {
        Transformer.Companion.getDEFAULT().transform(Paths.get("./210/"), UtilKt.readClasses(Paths.get("./gamepack.jar")));
    }
}
