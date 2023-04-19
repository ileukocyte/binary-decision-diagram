package io.ileukocyte.dsa;

import io.ileukocyte.dsa.bdd.BinaryDecisionDiagram;
import io.ileukocyte.dsa.bdd.Tests;

public class Main {
    public static final boolean RUN_TESTS = true;
    public static final boolean SINGLE_TEST_OUTPUT = true;

    public static void main(String[] args) {
        if (RUN_TESTS) {
            var tests = 100;
            var successful = tests;

            var testCounter = 0;
            var totalReduction = 0.0;

            for (int i = 13; i <= 21; i++) {
                var reduction = 0.0;
                var creationTime = 0;

                for (int j = 0; j < tests; j++) {
                    var now = System.nanoTime();

                    var dnf = Tests.generateDnfExpression(i);
                    var bdd = BinaryDecisionDiagram.createWithBestOrder(dnf);

                    creationTime += System.nanoTime() - now;

                    if (SINGLE_TEST_OUTPUT) {
                        System.out.printf("(%d variables) %d. ", i, j + 1);
                    }

                    if (!Tests.testBdd(bdd, SINGLE_TEST_OUTPUT)) {
                        successful--;
                    }

                    testCounter++;
                    reduction += 1.0 - bdd.computeSize() / (Math.pow(2, i + 1) - 1);
                    totalReduction += 1.0 - bdd.computeSize() / (Math.pow(2, i + 1) - 1);
                }

                System.out.printf("Testing (%d variables) has finished, the number of successful tests: %d/%d\n", i, successful, tests);
                System.out.printf("Average reduction: %f%%\n", (reduction / tests) * 100);
                System.out.printf("Average creation time: %d ns\n", creationTime / tests);
                System.out.println("--------------------------------------------------");
            }

            System.out.printf("[ Total average reduction: %f%% ]\n", (totalReduction / testCounter) * 100);
        }

        //"!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T", "ABCDEFGHIJKLMNXYZT"
        //"ABCD + AB + BC + CD", "ABCD"
        //"AB + AC + BC", "ABC"
        //"A!B + BA + B!A + !A!B", "AB"
        //"ABC + !A + !B + !C", "ABC"
        //"ABC + D!D + E!E", "DEABC"
    }
}