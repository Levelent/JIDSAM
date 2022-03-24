package ft_project;

import java.util.*;

public class DGH {
    private Node root;
    private AbstractSet<String> nodeValues;
    private AbstractMap<String, Integer> countCache;
    private AbstractMap<String, String> commonAncestorCache;
    public final String name;

    /**
     * Constructor where root data is supplied with name of dgh tree
     * 
     * @param rootData data at the root of the tree
     * @param name     associated with the tree
     */
    DGH(String rootData, String name) {
        this.root = new Node(rootData);
        this.root.children = new ArrayList<Node>();
        this.nodeValues = new HashSet<String>();
        this.countCache = new HashMap<>();
        this.commonAncestorCache = new HashMap<>();
        nodeValues.add(rootData);
        this.name = name;
    }

    /**
     * Constructor which creates empty tree with given name
     * 
     * @param name associated with the tree
     */
    DGH(String name) {
        this.root = null;
        this.nodeValues = new HashSet<String>();
        this.name = name;
        this.countCache = new HashMap<>();
        this.commonAncestorCache = new HashMap<>();
    }

    /**
     * Add a data to a given node in the tree
     * 
     * @param localRootName node to add the data to
     * @param data          to add
     * @return if data was added successful
     */
    public Boolean add(String localRootName, String data) {
        if (nodeValues.contains(data) || !nodeValues.contains(localRootName)) {
            return false;
        }

        Node localRoot = root.find(localRootName); // Find the correct node to add a child to
        localRoot.add(data); // add the child
        nodeValues.add(data);

        return true;
    }

    /**
     * Add data to the tree
     * 
     * @param data to add to the tree
     * @return if data was added successfully
     */
    public Boolean add(String data) {
        if (this.root == null) {
            this.root = new Node(data);
            this.root.children = new ArrayList<Node>();
            nodeValues.add(data);
            return true;
        }
        return this.add(this.root.data, data);
    }

    /**
     * Check if tree contains a given value
     * 
     * @param nodeName value to check for
     * @return if the tree contains the value
     */
    public Boolean contains(String nodeName) {
        return nodeValues.contains(nodeName);
    }

    /**
     * Find the node that contains a given piece of data
     * 
     * @param data to search for
     * @return node that contains the data
     */
    public Node find(String data) {
        return root.find(data);
    }

    /**
     * Get the number of nodes that make up the tree
     * 
     * @return the number of nodes
     */
    public int countNodes() {
        return nodeValues.size();
    }

    /**
     * Count the number of nodes at a given point in the tree
     * 
     * @param localRootName root of subtree to count nodes of
     * @return number of nodes
     */
    public int countNodes(String localRootName) {
        // try {
        // return find(localRootName).countNodes(0);
        // } catch (NullPointerException e) {
        // return find(root.data).countNodes(0);
        // }

        Integer count = this.countCache.get(localRootName);
        if (count == null) {
            count = find(localRootName).countNodes(0);
            this.countCache.put(localRootName, count);
        }
        return count;
    }

    /**
     * Find the first common ancestor between two data points in the tree
     * 
     * @param rootA data point one
     * @param rootB data point two
     * @return ancestor of root a and root b
     */
    public String findCommonAncestor(String rootA, String rootB) {

        String key = rootA + rootB;
        String commonAncestor = commonAncestorCache.get(key);
        if (commonAncestor != null) {
            return commonAncestor;
        }

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
                commonAncestorCache.put(key, lastB);
                return lastB;
            } else {
                found.add(lastB);
            }

            a = (a == null) ? null : a.parent;
            b = (b == null) ? null : b.parent;
        }

        return null;
    }

    /**
     * Get the data associated with the root
     * 
     * @return root value
     */
    public String getRootValue() {
        return this.root.data;
    }

    /**
     * @return string representation of root node
     */
    public String toString() {
        return this.root.toString();
    }

    public static class Node {
        public final String data;
        public final Node parent;
        private List<Node> children;

        /**
         * Constructor with given data but no parent
         * 
         * @param data to be stored in node
         */
        Node(String data) {
            this.data = data;
            this.children = new ArrayList<Node>();
            parent = null;
        }

        /**
         * Constructor for given data with parent
         * 
         * @param data   to be store in node
         * @param parent of the node created
         */
        Node(String data, Node parent) {
            this.data = data;
            this.children = new ArrayList<Node>();
            this.parent = parent;
        }

        /**
         * Add given data to the node
         * 
         * @param data to add to the node
         */
        public void add(String data) {
            children.add(new Node(data, this));
        }

        /**
         * Count all nodes rooted at this node
         * 
         * @param count value to start from
         * @return number of nodes count including provided
         */
        public int countNodes(int count) {
            for (Node n : this.children) {
                count += n.countNodes(0);
            }
            return count + 1;
        }

        /**
         * Find the node with given value
         * 
         * @param data that the node contains
         * @return the node which contains the data
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

        /**
         * Helper function to generate tab spaces for output
         * 
         * @param depth of tabs
         * @param str   the string to be output
         * @return string with added tabs
         */
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

        /**
         * @return string representation of the node
         */
        public String toString() {
            String str = this.data + System.lineSeparator();
            for (Node child : children) {
                str = child.tsHelper(1, str);
            }
            return str;
        }
    }
}
