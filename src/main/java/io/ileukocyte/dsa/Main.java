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
        //"ABC + D!D + E!E", "DEABC"

        for (int i = 0; i < 100; i++) {
            var bdd = BinaryDecisionDiagram.createWithBestOrder(Tests.generateDnfExpression());

            System.out.printf("%d. ", i + 1);

            Tests.testBdd(bdd);
        }
        /*var bdd = BinaryDecisionDiagram.create("!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T", "ABCDEFGHIJKLMNXYZT");

        Tests.testBdd(bdd);

        System.out.println(bdd.computeSize());*/
    }
}