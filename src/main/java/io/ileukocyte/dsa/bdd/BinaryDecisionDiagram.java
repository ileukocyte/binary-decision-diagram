package io.ileukocyte.dsa.bdd;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BinaryDecisionDiagram {
    private int computedSize = -1;

    private final Node root;
    private final Node trueLeaf;
    private final Node falseLeaf;

    private final String order;

    private final Map<String, Map<String, Node>> map;

    private BinaryDecisionDiagram(String function, String order, Map<String, Map<String, Node>> map) {
        root = new Node(function);
        trueLeaf = new Node("1");
        falseLeaf = new Node("0");

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
        return root.getDataDnf();
    }

    public int computeSize() {
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
            throw new IllegalArgumentException("The provided format is not correct!");
        }

        var map = new HashMap<String, Map<String, Node>>();

        for (var variable : functionVariables.toCharArray()) {
            map.put(String.valueOf(variable), new HashMap<>());
        }

        var bdd = new BinaryDecisionDiagram(Node.format(function), order, map);

        bdd.parse(bdd.getRoot(), bdd.getOrder());

        return bdd;
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

        do {
            orders.add(temp);

            temp = temp + temp.charAt(0);
            temp = temp.substring(1);
        } while (!temp.equals(variables));

        var bdds = orders.stream()
                .map(o -> create(function, o))
                .sorted(Comparator.comparing(BinaryDecisionDiagram::computeSize))
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
            return switch (root.getData()) {
                case "0" -> false;
                case "1" -> true;
                // should never occur
                default -> throw new IllegalStateException("Unexpected value: " + root.getData());
            };
        } else {
            return use(input.substring(1), input.charAt(0) == '0' ? root.getLeft() : root.getRight());
        }
    }

    private void parse(Node root, String order) {
        if (order.isEmpty()) {
            return;
        }

        var function = root.getData();

        var left = function.replace(order.charAt(0), '0').replace(Character.toLowerCase(order.charAt(0)), '1');
        var right = function.replace(order.charAt(0), '1').replace(Character.toLowerCase(order.charAt(0)), '0');

        left = Node.parseDigits(left);
        right = Node.parseDigits(right);

        var mapLevel = map.get(String.valueOf(order.charAt(0)));

        /*if (left.equals(right)) {
            var child = mapLevel.getOrDefault(left, left.equals("0") ? falseLeaf : left.equals("1") ? trueLeaf : new Node(left));
            var newParent = root.getParents().iterator().next();

            child.addParent(newParent);

            if (root.equals(newParent.getLeft())) {
                newParent.setLeft(child);
            } else {
                newParent.setRight(child);
            }

            //var containedKey = mapLevel.containsKey(left);

            //mapLevel.putIfAbsent(left, child);

            if (!left.equals("0") && !left.equals("1")*//* && !containedKey*//*) {
                parse(child, order.substring(1));
            }

            *//*var child = mapLevel.getOrDefault(left, left.equals("0") ? falseLeaf : left.equals("1") ? trueLeaf : new Node(left));
            var newParent = root.getParents().iterator().next();

            child.addParent(newParent);

            Node sibling;

            if (root.equals(newParent.getLeft())) {
                newParent.setLeft(child);

                sibling = newParent.getRight();
            } else {
                newParent.setRight(child);

                sibling = newParent.getLeft();
            }

            var containedKey = mapLevel.containsKey(left);

            mapLevel.putIfAbsent(left, child);

            if (!left.equals("0") && !left.equals("1") && !containedKey) {
                parse(child, order.substring(1));
            }

            while (child.equals(sibling)) {
                var temp = newParent;

                try {
                    newParent = newParent.getParents().iterator().next();
                } catch (NoSuchElementException e) {
                    break;
                }

                child.parents.remove(temp);

                child.addParent(newParent);

                if (temp.equals(newParent.getLeft())) {
                    newParent.setLeft(child);

                    sibling = newParent.getRight();
                } else {
                    newParent.setRight(child);

                    sibling = newParent.getLeft();
                }
            }*//*
        } else {*/
            if (mapLevel.containsKey(left)) {
                mapLevel.get(left).addParent(root);

                root.setLeft(mapLevel.get(left));
            } else {
                var leftNode = left.equals("0") ? falseLeaf : left.equals("1") ? trueLeaf : new Node(left);

                leftNode.addParent(root);

                root.setLeft(leftNode);

                var containedKey = mapLevel.containsKey(left);

                mapLevel.putIfAbsent(left, leftNode);

                if (!left.equals("0") && !left.equals("1")/* && !containedKey*/) {
                    parse(leftNode, order.substring(1));
                }
            }

            if (mapLevel.containsKey(right)) {
                mapLevel.get(right).addParent(root);

                root.setRight(mapLevel.get(right));
            } else {
                var rightNode = right.equals("0") ? falseLeaf : right.equals("1") ? trueLeaf : new Node(right);

                rightNode.addParent(root);

                root.setRight(rightNode);

                var containedKey = mapLevel.containsKey(right);

                mapLevel.putIfAbsent(right, rightNode);

                if (!right.equals("0") && !right.equals("1") && !containedKey) {
                    parse(rightNode, order.substring(1));
                }
            }
        //}
    }

    private void fillNodeSet(Set<Node> nodes, Node root) {
        if (root == null) {
            return;
        }

        nodes.add(root);

        fillNodeSet(nodes, root.getLeft());
        fillNodeSet(nodes, root.getRight());
    }

    public static class Node {
        private final String data;
        private final String dataDnf;

        private Node left = null;
        private Node right = null;
        private final Set<Node> parents;

        public Node(String data) {
            this.data = data;

            parents = new HashSet<>();

            var pattern = Pattern.compile("([a-z])");
            var matcher = pattern.matcher(data);

            dataDnf = matcher.replaceAll(match -> "!" + match.group().toUpperCase()).replace("+", " + ");
        }

        public String getData() {
            return data;
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

        public String getDataDnf() {
            return dataDnf;
        }

        protected static String format(String function) {
            function = function.replaceAll("\\s+", "");

            var pattern = Pattern.compile("!([A-Z])");
            var matcher = pattern.matcher(function);

            return matcher.replaceAll(match -> match.group().toLowerCase().substring(1));
        }

        protected static String parseDigits(String input) {
            var filtered = Arrays.stream(input.split("\\+")).filter(c -> !c.contains("0")).toList();

            if (filtered.stream().anyMatch(c -> c.matches("1+"))) {
                return "1";
            }

            return filtered.isEmpty() ? "0" : filtered.stream().distinct()
                    .collect(Collectors.joining("+"))
                    .replace("1", "");
        }
    }
}