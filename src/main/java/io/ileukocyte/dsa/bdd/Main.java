package io.ileukocyte.dsa.bdd;

public class Main {
    // Testing flags
    public static final boolean RUN_TESTS = true;
    public static final int INDIVIDUAL_TESTS = 100;
    public static final boolean SINGLE_TEST_OUTPUT = true;

    public static final boolean RUN_SPECIAL_TESTS = false;

    public static void main(String[] args) {
        if (RUN_TESTS) {
            var successful = INDIVIDUAL_TESTS;

            var testCounter = 0;
            var totalReduction = 0.0;

            for (int i = 13; i <= 21; i++) {
                var reduction = 0.0;
                var creationTime = 0;

                for (int j = 0; j < INDIVIDUAL_TESTS; j++) {
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
                    reduction += 1.0 - (double) bdd.computeSize() / BinaryDecisionDiagram.fullNodeCount(i);
                    totalReduction += 1.0 - (double) bdd.computeSize() / BinaryDecisionDiagram.fullNodeCount(i);
                }

                System.out.printf("Testing (%d variables) has finished, the number of successful tests: %d/%d\n", i, successful, INDIVIDUAL_TESTS);
                System.out.printf("Average reduction: %f%%\n", (reduction / INDIVIDUAL_TESTS) * 100);
                System.out.printf("Average creation time: %d ns\n", creationTime / INDIVIDUAL_TESTS);
                System.out.println("--------------------------------------------------");
            }

            System.out.printf("[ Total average reduction: %f%% ]\n", (totalReduction / testCounter) * 100);
        }

        if (RUN_SPECIAL_TESTS) {
            var functions = new String[][] {
                    {"!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T", "ABCDEFGHIJKLMNXYZT"},
                    {"ABCD + AB + BC + CD", "ABCD"},
                    {"AB + AC + BC", "ABC"},
                    {"AB + !AB + A!B + !A!B", "AB"},
                    {"ABC + !A + !B + !C", "ABC"},
                    {"ABC + D!D + E!E", "DEABC"},
                    {"ABC + AB + !AC + !ABC", "ABC"}
            };

            for (var function : functions) {
                var bdd = BinaryDecisionDiagram.create(function[0], function[1]);

                System.out.printf("%s: %d nodes\n", function[0], bdd.computeSize());

                Tests.testBdd(bdd, SINGLE_TEST_OUTPUT);
            }
        }
    }
}