package org.runestar.client.updater.deob;

import org.runestar.client.updater.deob.rs.DeobToDeob;

import java.nio.file.Paths;

public class Main {
    public static void main(String[] args)
    {
        Transformer.Companion.getDEFAULT().transform(Paths.get("./osrs_decomp.jar"),Paths.get("./deob.jar"));
    }
}
