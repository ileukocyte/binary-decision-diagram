package io.ileukocyte.dsa;

import io.ileukocyte.dsa.bdd.BinaryDecisionDiagram;
import io.ileukocyte.dsa.bdd.Tests;

/*import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;*/

public class Main {
    public static void main(String[] args) {
        //"!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T", "ABCDEFGHIJKLMNXYZT"
        //"ABCD + AB + BC + CD", "ABCD"
        //"AB + AC + BC", "ABC"
        //var formula = Files.readString(Path.of("C:\\Users\\alexi\\Desktop\\formula.txt")); // "GMRFDATIKLHEQNOJPSCB"
        //"A!B + BA + B!A + !A!B", "AB"
        //"ABC + !A + !B + !C", "ABC"

        var bdd = BinaryDecisionDiagram.createWithBestOrder("!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T");

        System.out.println(bdd.computeSize());

        for (int i = 0; i < Math.pow(2, 18); i++) {
            //if (i % 50_000 == 0 && i > 0) System.out.println("50k done");

            var values = String.format("%18s", Integer.toBinaryString(i)).replace(' ', '0');

            var parseResult = Tests.parseFunction(bdd.getFunction(), values, bdd.getOrder());

            try {
                var bddResult = bdd.use(values);

                if (bddResult != parseResult) {
                    System.out.printf("%s. BDD: %b, actual: %b\n", values, bddResult, parseResult);
                }
            } catch (IllegalStateException e) {
                System.out.printf("IllegalStateException for %s: (%s)\n", values, parseResult);

                e.printStackTrace();

                return;
            }
        }

        System.out.println("Done!");
    }
}