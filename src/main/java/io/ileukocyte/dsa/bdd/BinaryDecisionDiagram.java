package io.ileukocyte.dsa.bdd;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BinaryDecisionDiagram {
    // calculated dynamically once right after the BDD is created
    private int computedSize = -1;

    private Node root;
    private final Node falseLeaf;
    private final Node trueLeaf;

    private final String function;
    private final String order;

    // used for I-reduction
    private final Map<String, Map<String, Node>> map;

    private BinaryDecisionDiagram(String function, String order, Map<String, Map<String, Node>> map) {
        root = new Node(Node.format(function));
        falseLeaf = new Node("0");
        trueLeaf = new Node("1");

        this.function = function;
        this.order = order;
        this.map = map;
    }

    public Node getRoot() {
        return root;
    }

    public String getOrder() {
        return order;
    }

    public String getFunction() {
        return function;
    }

    public int size() {
        if (computedSize == -1) {
            var nodes = new HashSet<Node>();

            fillNodeSet(nodes, root);

            computedSize = nodes.size();
        }

        return computedSize;
    }

    public static BinaryDecisionDiagram create(String function, String order) {
        if (function.isEmpty() || order.isEmpty()) {
            throw new IllegalArgumentException("Neither the function nor the order can be empty!");
        }

        var functionVariables = function.chars()
                .mapToObj(i -> (char) i)
                .filter(c -> c >= 'A' && c <= 'Z')
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));
        var orderVariables = order.chars()
                .mapToObj(i -> (char) i)
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));

        if (!functionVariables.equals(orderVariables)) {
            throw new IllegalArgumentException("The function and order provided do not correspond to each other!");
        }

        if (!function.matches("[!A-Z+\\s]+")) {
            throw new IllegalArgumentException("The provided format is not correct! DNF (e.g., ABC + A!B!C) should be used instead!");
        }

        var map = new HashMap<String, Map<String, Node>>();

        for (var variable : functionVariables.toCharArray()) {
            // creating an individual map for each variable
            map.put(String.valueOf(variable), new HashMap<>());
        }

        var bdd = new BinaryDecisionDiagram(function, order, map);

        bdd.parse(bdd.getRoot(), bdd.getOrder());
        bdd.size();

        return bdd;
    }

    public static BinaryDecisionDiagram create(String function) {
        var sortedOrder = function.chars()
                .mapToObj(n -> (char) n)
                .filter(c -> c >= 'A' && c <= 'Z')
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));

        return create(function, sortedOrder);
    }

    public static BinaryDecisionDiagram createWithBestOrder(String function) {
        if (function.isEmpty()) {
            throw new IllegalArgumentException("The provided function must not be empty!");
        }

        var variables = function.chars()
                .mapToObj(i -> (char) i)
                .filter(c -> c >= 'A' && c <= 'Z')
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(""));
        var temp = variables;

        var orders = new HashSet<String>();

        // linear method (e.g., ABC, BCA, CAB)
        do {
            orders.add(temp);

            temp = temp + temp.charAt(0);
            temp = temp.substring(1);
        } while (!temp.equals(variables));

        // checking which order generates the least nodes
        var bdds = orders.stream()
                .map(o -> create(function, o))
                .sorted(Comparator.comparing(BinaryDecisionDiagram::size))
                .toList();

        return bdds.get(0);
    }

    public boolean use(String input) {
        if (!input.matches("[01]+") || input.length() != order.length()) {
            throw new IllegalArgumentException("The input does not match the required format!");
        }

        return use(input, root);
    }

    private boolean use(String input, Node root) {
        if (root.getLeft() == null && root.getRight() == null) {
            // just in case a leaf node contains something else than "0" or "1"
            // should never occur
            if (!root.getFunction().equals(falseLeaf.getFunction()) && !root.getFunction().equals(trueLeaf.getFunction())) {
                throw new IllegalStateException("Unexpected value: " + root.getFunction());
            }

            return root.getFunction().equals(trueLeaf.getFunction());
        } else {
            var data = root.getFunctionDnf();

            // checking whether the current variable has been reduced
            if (!data.contains(String.valueOf(order.charAt(order.length() - input.length())))) {
                return use(input.substring(1), root);
            }

            return use(input.substring(1), input.charAt(0) == '0' ? root.getLeft() : root.getRight());
        }
    }

    private void parse(Node root, String order) {
        if (order.isEmpty()) {
            return;
        }

        var function = root.getFunction();

        var left = function.replace(order.charAt(0), '0').replace(Character.toLowerCase(order.charAt(0)), '1');
        var right = function.replace(order.charAt(0), '1').replace(Character.toLowerCase(order.charAt(0)), '0');

        left = Node.parseDigits(left);
        right = Node.parseDigits(right);

        // I-reduction map
        var mapLevel = map.get(String.valueOf(order.charAt(0)));

        var containsLeft = mapLevel.containsKey(left);
        var leftNode = mapLevel.getOrDefault(left, left.equals("0") ? falseLeaf : left.equals("1") ? trueLeaf : new Node(left));

        leftNode.addParent(root);
        root.setLeft(leftNode);

        mapLevel.put(left, leftNode);

        var containsRight = mapLevel.containsKey(right);
        var rightNode = mapLevel.getOrDefault(right, right.equals("0") ? falseLeaf : right.equals("1") ? trueLeaf : new Node(right));

        rightNode.addParent(root);
        root.setRight(rightNode);

        mapLevel.put(right, rightNode);

        if (!left.equals("0") && !left.equals("1")) {
            if (!containsLeft) {
                parse(leftNode, order.substring(1));
            } else {
                sReduction(leftNode);
            }
        }

        if (!right.equals("0") && !right.equals("1")) {
            if (!containsRight) {
                parse(rightNode, order.substring(1));
            } else {
                sReduction(rightNode);
            }
        }

        sReduction(root);
    }

    private void sReduction(Node root) {
        if (root.getLeft() != null && root.getRight() != null && root.getLeft().getFunction().equals(root.getRight().getFunction())) {
            var child = root.getLeft();

            child.removeParent(root);

            if (!root.equals(this.root)) {
                for (var grandparent : root.getParents()) {
                    child.addParent(grandparent);

                    if (root.equals(grandparent.getLeft())) {
                        grandparent.setLeft(child);
                    }

                    if (root.equals(grandparent.getRight())) {
                        grandparent.setRight(child);
                    }
                }
            } else {
                this.root = child;
            }
        }
    }

    // used for getting all the unique nodes of the BDD
    private void fillNodeSet(Set<Node> nodes, Node root) {
        if (root == null) {
            return;
        }

        nodes.add(root);

        fillNodeSet(nodes, root.getLeft());
        fillNodeSet(nodes, root.getRight());
    }

    public static int fullNodeCount(int variables) {
        return (int) (Math.pow(2, variables + 1) - 1);
    }

    public static class Node {
        private final String function;

        // non-formatted function
        private final String functionDnf;

        private Node left = null;
        private Node right = null;
        private final Set<Node> parents;

        public Node(String function) {
            this.function = function;

            parents = new HashSet<>();

            var pattern = Pattern.compile("([a-z])");
            var matcher = pattern.matcher(function);

            functionDnf = matcher.replaceAll(match -> "!" + match.group().toUpperCase()).replace("+", " + ");
        }

        public String getFunction() {
            return function;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        public Set<Node> getParents() {
            return parents;
        }

        private void setLeft(Node left) {
            this.left = left;
        }

        private void setRight(Node right) {
            this.right = right;
        }

        private void addParent(Node parent) {
            parents.add(parent);
        }

        private void removeParent(Node parent) {
            parents.remove(parent);
        }

        public String getFunctionDnf() {
            return functionDnf;
        }

        protected static String format(String function) {
            function = function.replaceAll("\\s+", "");

            var pattern = Pattern.compile("!([A-Z])");
            var matcher = pattern.matcher(function);

            return matcher.replaceAll(match -> match.group().toLowerCase().substring(1));
        }

        protected static String parseDigits(String input) {
            var functionVariables = input.chars()
                    .mapToObj(i -> (char) i)
                    .filter(c -> c >= 'A' && c <= 'Z')
                    .distinct()
                    .sorted()
                    .map(String::valueOf)
                    .toList();

            var filtered = Arrays.stream(input.split("\\+")).filter(c -> !c.contains("0")).toList();

            if (filtered.stream().anyMatch(c -> c.matches("1+"))) {
                return "1";
            }

            var parsed = filtered.isEmpty() ? "0" : filtered.stream().distinct()
                    .collect(Collectors.joining("+"))
                    .replace("1", "");

            filtered = Arrays.stream(parsed.split("\\+")).distinct().toList();

            for (var variable : functionVariables) {
                if (filtered.contains(variable) && filtered.contains(variable.toLowerCase())) {
                    return "1";
                }
            }

            return String.join("+", filtered);
        }
    }
}