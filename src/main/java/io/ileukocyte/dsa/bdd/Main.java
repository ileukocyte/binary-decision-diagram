package io.ileukocyte.dsa.bdd;

import java.util.TreeMap;

public class Main {
    // Testing flags
    public static final boolean RUN_RANDOM_TESTS = true;
    public static final int INDIVIDUAL_TESTS = 100;
    public static final int MIN_VARIABLES = 13;
    public static final int MAX_VARIABLES = 21;
    public static final boolean USE_BEST_ORDER = true;
    public static final boolean SINGLE_TEST_OUTPUT = true;

    public static final boolean RUN_SPECIAL_TESTS = false;

    public static void main(String[] args) {
        if (RUN_RANDOM_TESTS) {
            var successful = INDIVIDUAL_TESTS;

            var totalTestCounter = 0;
            var totalReduction = 0.0;

            var avgValues = new TreeMap<Integer, Tests.TestEntry>();

            for (int i = MIN_VARIABLES; i <= MAX_VARIABLES; i++) {
                var reduction = 0.0;

                long creationTime = 0;
                long memoryUsed = 0;

                for (int j = 0; j < INDIVIDUAL_TESTS; j++) {
                    var dnf = Tests.generateDnfExpression(i);

                    System.gc();

                    var runtime = Runtime.getRuntime();
                    var memory = runtime.totalMemory() - runtime.freeMemory();
                    var now = System.nanoTime();

                    var bdd = USE_BEST_ORDER ? BinaryDecisionDiagram.createWithBestOrder(dnf) : BinaryDecisionDiagram.create(dnf);

                    memory = runtime.totalMemory() - runtime.freeMemory() - memory;
                    memoryUsed += memory >> 10;

                    creationTime += System.nanoTime() - now;

                    if (SINGLE_TEST_OUTPUT) {
                        System.out.printf("(%d variables) %d. ", i, j + 1);
                    }

                    if (!Tests.testBdd(bdd, SINGLE_TEST_OUTPUT)) {
                        successful--;
                    }

                    totalTestCounter++;
                    reduction += 1.0 - (double) bdd.size() / BinaryDecisionDiagram.fullNodeCount(i);
                    totalReduction += 1.0 - (double) bdd.size() / BinaryDecisionDiagram.fullNodeCount(i);
                }

                var testEntry = new Tests.TestEntry(
                        (reduction / INDIVIDUAL_TESTS) * 100,
                        creationTime / INDIVIDUAL_TESTS,
                        memoryUsed / INDIVIDUAL_TESTS
                );

                System.out.printf("Testing (%d variables) has finished, the number of successful tests: %d/%d\n", i, successful, INDIVIDUAL_TESTS);
                System.out.printf("Average reduction: %f%%\n", testEntry.reduction());
                System.out.printf("Average creation time: %d ns\n", testEntry.creationTime());
                System.out.printf("Average memory usage: %d kB\n", testEntry.memoryUsage());

                avgValues.put(i, testEntry);

                System.out.println("--------------------------------------------------");
            }

            System.out.println("All the tests have finished!");
            System.out.println("Average values by variable count:");

            for (var entry : avgValues.entrySet()) {
                var testEntry = entry.getValue();

                System.out.printf("- %d variables: %f%%, %d ns, %d kB\n", entry.getKey(), testEntry.reduction(), testEntry.creationTime(), testEntry.memoryUsage());
            }

            System.out.println("--------------------------------------------------");
            System.out.printf("[ Total average reduction: %f%% ]\n", (totalReduction / totalTestCounter) * 100);
        }

        if (RUN_SPECIAL_TESTS) {
            var functions = new String[][] {
                    {"!AB!F + !C!D + E!F + AGH + I!JK + L!M!N + XYZ + T", "ABCDEFGHIJKLMNXYZT"}, // an example from the Internet, should be reduced to 26 nodes
                    {"ABCD + AB + BC + CD", "ABCD"}, // a general reduction test (31 nodes -> 8 nodes)
                    {"AB + AC + BC", "ABC"}, // an example from the lecture (reduction combination)
                    {"ABC + D!D + E!E", "DEABC"}, // an S-reduction test (63 nodes -> 5 nodes)
                    {"ABC + AB + !AC + !ABC", "ABC"}, // an S-reduction test
                    {"AB + !AB + A!B + !A!B", "AB"}, // a tautology (1 node)
                    {"ABC + !A + !B + !C", "ABC"}, // a tautology (1 node)
                    {"A!A + B!B + C!C + D!D + E!E + F + G", "FGABCDE"} // a tautology (1 node)
            };

            for (var function : functions) {
                var bdd = BinaryDecisionDiagram.create(function[0], function[1]);

                System.out.printf("%s: %d nodes, tautology: %b\n", function[0], bdd.size(), bdd.isTautology());

                Tests.testBdd(bdd, SINGLE_TEST_OUTPUT);
            }
        }
    }
}