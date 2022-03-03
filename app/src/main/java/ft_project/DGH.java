package ft_project;

import java.util.*;

public class DGH {
    private Node root;
    private AbstractSet<String> nodeValues;
    public final String name;

    DGH(String rootData, String name) {
        this.root = new Node(rootData);
        this.root.children = new ArrayList<Node>();
        this.nodeValues = new HashSet<String>();
        nodeValues.add(rootData);
        this.name = name;
    }

    DGH(String name) {
        this.root = null;
        this.nodeValues = new HashSet<String>();
        this.name = name;
    }

    public Boolean add(String localRootName, String data) {
        if (nodeValues.contains(data) || !nodeValues.contains(localRootName)) {
            // Should probably deliniate between whether it cant be added because
            // it already exists or because the localRoot could not be found

            return false;
        }

        Node localRoot = root.find(localRootName); // Find the correct node to add a child to... relying on this working
                                                   // if localRootName exists in our set
        if (localRoot == null) {

        }
        localRoot.add(data); // add the child
        nodeValues.add(data);

        return true;
    }

    public Boolean add(String data) {
        if (this.root == null) {
            this.root = new Node(data);
            this.root.children = new ArrayList<Node>();
            nodeValues.add(data);
            return true;
        } else {
            return this.add(this.root.data, data);

        }
    }

    public Boolean contains(String nodeName) {
        return nodeValues.contains(nodeName);
    }

    public Node find(String data) {
        return root.find(data);
    }

    public int countNodes() {
        return nodeValues.size();
    }

    public int countNodes(String localRootName) {
        try {
            return find(localRootName).countNodes(0);
        } catch (NullPointerException e) {
            System.out.println(localRootName);
            return find(root.data).countNodes(0);
        }

    }

    public String findCommonAncestor(String rootA, String rootB) {
        Node a = root.find(rootA);
        Node b = root.find(rootB);

        if (a == null || b == null) {
            return null;
        }

        if (rootA.equals(rootB)) {
            return rootA;
        }

        HashSet<String> found = new HashSet<String>();
        String lastA = a.data;
        String lastB = b.data;
        while (!(a == null && b == null)) {

            lastA = (a == null) ? lastA : a.data;
            lastB = (b == null) ? lastB : b.data;

            found.add(lastA);
            if (found.contains(lastB)) {
                return lastB;
            } else {
                found.add(lastB);
            }

            a = (a == null) ? null : a.parent;
            b = (b == null) ? null : b.parent;
        }

        return null;

    }

    public String toString() {
        return this.root.toString();
    }

    public static class Node {
        public final String data;
        public final Node parent;
        private List<Node> children;

        Node(String data) {
            this.data = data;
            this.children = new ArrayList<Node>();
            parent = null;
        }

        Node(String data, Node parent) {
            this.data = data;
            this.children = new ArrayList<Node>();
            this.parent = parent;
        }

        public void add(String data) {
            children.add(new Node(data, this));
        }

        /**
         * Count all nodes rooted at this node
         */
        public int countNodes(int count) {
            for (Node n : this.children) {
                count = n.countNodes(count);
            }
            return count + 1;
        }

        /**
         * Find the node with value: data
         */
        public Node find(String data) {
            if (this.data.equals(data)) {
                return this;
            }
            for (Node n : this.children) {
                Node found = n.find(data);
                if (found != null) {
                    return found;
                }
            }
            return null;
        }

        private String tsHelper(int depth, String str) {
            String tmp = "";
            for (int i = 0; i < depth; i++) {
                tmp += "\t";
            }
            tmp = tmp + this.data + System.lineSeparator();
            for (Node child : children) {
                tmp = child.tsHelper(depth + 1, tmp);
            }
            return str + tmp;
        }

        public String toString() {
            String str = this.data + System.lineSeparator();
            for (Node child : children) {
                str = child.tsHelper(1, str);
            }
            return str;
        }
    }
}
