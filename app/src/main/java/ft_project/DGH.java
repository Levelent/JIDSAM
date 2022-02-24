package ft_project;

import java.util.*;

public class DGH {
    private Node root;
    private AbstractSet<String> nodeValues;

    DGH(String rootData) {
        this.root = new Node(rootData);
        this.root.children = new ArrayList<Node>();
        nodeValues.add(rootData);
    }

    public Boolean add(String localRootName, String data) {
        if (nodeValues.contains(data) || !nodeValues.contains(localRootName)) {
            // Should probably delineate between whether it cant be added because
            // it already exists or because the localRoot could not be found
            return false;
        }
        Node localRoot = root.find(localRootName); // Find the correct node to add a child to... relying on this working
                                                   // if localRootName exists in our set
        localRoot.add(data); // add the child
        nodeValues.add(data);

        return true;
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
        return find(localRootName).countNodes(0);
    }

    public static class Node {
        private String data;
        private Node parent;
        private List<Node> children;

        Node(String data) {
            this.data = data;
            this.children = new ArrayList<Node>();
        }

        public void add(String data) {
            children.add(new Node(data));
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
            if (this.data == data) {
                return this;
            }
            for (Node n : this.children) {
                if (n.data == data) {
                    return n;
                }
            }
            return null;
        }
    }
}
