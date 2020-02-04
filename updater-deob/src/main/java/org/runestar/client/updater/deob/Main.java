package org.runestar.client.updater.deob;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args)
    {
        Transformer.Companion.getDEFAULT().transform(Paths.get("./gamepack.jar"),Paths.get("./deob.jar"));
    }
}
