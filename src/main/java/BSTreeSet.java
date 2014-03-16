import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/**
 * @param <T>
 */
public class BSTreeSet<T extends Comparable<T>> {
    private static final int MAX_DEPTH_DIFFERENCE = 1;
    private Node root;
    private Node lead = new Node(null);
    private Node tail = new Node(null);
    private int modCount = 0;

    public static void main(String[] args) {
        BSTreeSet<Integer> tree = new BSTreeSet<>();
        for (int i = 0; i < 5; i++) {
            Random rnd = new Random();
            tree.add(rnd.nextInt(500));
        }
        System.out.println(tree.toString());
        tree.test();
    }

    public void test() {
        BSTreeIterator i = iterator(BSTreeIterator.ORDER_HIGH_TO_LOW);
        while (i.hasPrevious()) {
            System.out.print(i.previous() + ", ");
        }
        i = iterator(BSTreeIterator.ORDER_LOW_TO_HIGH);
        while (i.hasNext())
            System.out.println(i.next());
    }

    /**
     *
     * @param data
     * @return
     */
    public boolean add(T data) {
        boolean add;
        if (root == null) {
            root = new Node(data);
            tail.setNextLarger(null);
            tail.setNextSmaller(root.findMaxNode());
            lead.setNextLarger(root);
            root.setNextLarger(tail);
            root.setNextSmaller(lead);
            add = true;
        } else
            add = root.add(data);
        if (add)
            root.size++;
        return add;
    }

    public boolean remove(T data) {
        if (!isEmpty()) {
            if (!contains(data))
                return false;
            root = root.remove(data);
            root.setSize(root.size());
            return true;
        }
        return false;
    }

    public boolean contains(T data) {
        return !isEmpty() && contains(data, root);
    }

    public void clear() {
        root = null;
    }

    public int size() {
        return isEmpty() ? 0 : root.getSize();
    }

    public int depth() {
        return root == null ? -1 : root.depth();
    }

    public int depth(Node node) {
        return node == null ? 0 : node.depth + 1;
    }

    public String toString() {
        return "[" + (root == null ? "" : root.toString()) + "]";
    }

    private boolean isEmpty() {
        return root == null;
    }

    private void makeEmpty() {
        root = null;
    }

    private boolean contains(T data, Node branchRoot) {
        //basecase, vi kan inte gå längre och har inte hittat noden
        if (branchRoot == null)
            return false;
        //lägre==-1, högre==1, equal==0
        int result = data.compareTo(branchRoot.getData());
        if (result < 0)
            return contains(data, branchRoot.getLeft());
        else if (result > 0)
            return contains(data, branchRoot.getRight());
        return true;
    }


    /**
     * @param order BSTreeIterator.ORDER_HIGH_TO_LOW or BSTReeIterator.ORDER_LOW_TO_HIGH
     * @return iterator
     * @throws java.lang.IllegalArgumentException if order is not valid
     */
    public BSTreeIterator iterator(int order) {
        if (order != BSTreeIterator.ORDER_HIGH_TO_LOW)
            if (order != BSTreeIterator.ORDER_LOW_TO_HIGH)
                throw new IllegalArgumentException();
        return new BSTreeIterator(order);
    }

    /**
     * @return iterator
     */
    public BSTreeIterator iterator() {
        return new BSTreeIterator(BSTreeIterator.ORDER_LOW_TO_HIGH);
    }

    /**
     *
     */
    public class Node {
        private T data;
        private Node left;
        private Node right;
        private Node nextSmaller;
        private Node nextLarger;
        private int size = 0;
        public int depth = 1;

        public int getSize() {
            return size;
        }

        public Node(T data) {
            this.data = data;
        }

        /**
         *
         * @return
         */
        public Node getNext() {
            if (right != null)
                findMinNode(right);
            Node start = root;
            Node asc = null;
            while (start != null) {
                if (this.getData().compareTo(start.getData()) < 0) {
                    asc = start;
                    start = start.getLeft();
                } else
                    start = start.getRight();
            }
            return asc;
        }

        /**
         * @return
         */
        public Node getPrev() {
            if (left != null)
                return findMaxNode(left);
            Node start = root;
            Node asc = null;
            while (start != null) {
                if (getData().compareTo(start.getData()) > 0) {
                    asc = start;
                    start = start.getRight();
                } else
                    start = start.getLeft();
            }
            return asc;
        }

        /**
         * L�gger till en nod i det bin�ra s�ktr�det. Om noden redan existerar s�
         * l�mnas tr�det of�r�ndrat.
         *
         * @param data datat f�r noden som ska l�ggas till.
         * @return true om en ny nod lades till tr�det.
         */
        public boolean add(T data) {
            try {
                Node node = add(data, this);
                //node.balance();
                node.nextLarger = getNext();
                node.nextSmaller = getPrev();

            } catch (IllegalStateException ise) {
                return false;
            }
            return true;
        }

        /**
         * @param data
         * @param branchRoot
         * @return
         */
        private Node add(T data, Node branchRoot) {
            //basecase, vi har hittat en plats där nya noden ska ligga
            if (branchRoot == null)
                return new Node(data);
            //kan ha 3 lägen: lägre==-1, högre==1, equal==0
            int result = data.compareTo(branchRoot.data);
            if (result < 0)
                branchRoot.left = add(data, branchRoot.left);
            else if (result > 0)
                branchRoot.right = add(data, branchRoot.right);
            else
                throw new IllegalStateException();
            return branchRoot;
        }

        /**
         * @return
         */
        private Node balance() {
            if (BSTreeSet.this.depth(left) - BSTreeSet.this.depth(right) > MAX_DEPTH_DIFFERENCE)
                if (BSTreeSet.this.depth(left.left) >= BSTreeSet.this.depth(left.right))
                    rotateWithLeftChild();
                else
                    doubleWithLeftChild();
            else if (BSTreeSet.this.depth(right) - BSTreeSet.this.depth(left) > BSTreeSet.MAX_DEPTH_DIFFERENCE)
                if (BSTreeSet.this.depth(right.right) >= BSTreeSet.this.depth(right.left))
                    rotateWithRightChild();
                else
                    doubleWithRightChild();

            depth = Math.max(BSTreeSet.this.depth(left), BSTreeSet.this.depth(right)) + 1;
            System.out.println(this.data + "har" + depth);
            return this;
        }
        // Assume t is either balanced or within one of being balanced

        /**
         * Rotate binary tree node with left child.
         * For AVL trees, this is a single rotation for case 1.
         * Update heights, then return new root.
         */
        private Node rotateWithLeftChild() {
            Node k1 = left;
            left = k1.right;
            k1.right = this;
            depth = Math.max(BSTreeSet.this.depth(left), BSTreeSet.this.depth(right)) + 1;
            k1.depth = Math.max(BSTreeSet.this.depth(k1.left), depth) + 1;
            return k1;
        }

        /**
         * Rotate binary tree node with right child.
         * For AVL trees, this is a single rotation for case 4.
         * Update heights, then return new root.
         */
        private Node rotateWithRightChild() {
            Node k2 = right;
            right = k2.left;
            k2.left = this;
            depth = Math.max(BSTreeSet.this.depth(left), BSTreeSet.this.depth(right)) + 1;
            k2.depth = Math.max(BSTreeSet.this.depth(k2.right), depth) + 1;
            return k2;
        }

        /**
         * Double rotate binary tree node: first left child
         * with its right child; then node k3 with new left child.
         * For AVL trees, this is a double rotation for case 2.
         * Update heights, then return new root.
         */
        private Node doubleWithLeftChild() {
            left = rotateWithRightChild();
            return rotateWithLeftChild();
        }

        /**
         * Double rotate binary tree node: first right child
         * with its left child; then node k1 with new right child.
         * For AVL trees, this is a double rotation for case 3.
         * Update heights, then return new root.
         */
        private Node doubleWithRightChild() {
            right = rotateWithLeftChild();
            return rotateWithRightChild();
        }

        /**
         * Privat hj�lpmetod som �r till nytta vid borttag. Ni beh�ver inte
         * skriva/utnyttja denna metod om ni inte vill.
         *
         * @return det minsta elementet i det (sub)tr�d som noden utg�r root i.
         */
        private T findMin() {
            return findMin(this);
        }

        /*
        *@param branchRoot
        *       nod/subträd
        *@return T element, det lägsta i subträdet. Null om parametern branchRoot var felaktig
        */
        private T findMin(Node branchRoot) {
            if (branchRoot.left == null)
                return branchRoot.data;
            else
                return findMin(branchRoot.left);
        }

        public Node findMinNode() {
            return findMinNode(this);
        }

        /*
        *@param branchRoot
        *       nod/subträd
        *@return T element, det lägsta i subträdet. Null om parametern branchRoot var felaktig
        */
        private Node findMinNode(Node branchRoot) {
            if (branchRoot == null)
                return null;
            if (branchRoot.left == null)
                return branchRoot;
            else
                return findMinNode(branchRoot.left);
        }

        /**
         * Tar bort ett element ur tr�det. Om elementet inte existerar s l�mnas
         * tr�det of�r�ndrat.
         *
         * @param info elementet som ska tas bort ur tr�det.
         * @return en referens till nodens subtr�d efter borttaget.
         */
        public Node remove(T info) {
            return remove(info, this);
        }

        /**
         *
         * @param data
         * @param branchRoot
         * @return
         */
        private Node remove(T data, Node branchRoot) {
            if (branchRoot == null)
                return null;
            int result = data.compareTo(branchRoot.data);
            if (result < 0)
                branchRoot.left = remove(data, branchRoot.left);
            else if (result > 0)
                branchRoot.right = remove(data, branchRoot.right);
            else {
                //found it
                if (branchRoot.isLeaf())
                    return null;
                if (branchRoot.hasTwoChildren()) {
                    branchRoot.data = findMin(branchRoot.right);
                    branchRoot.right = remove(branchRoot.data, branchRoot.right);
                    return branchRoot;
                } else //har bara ett barn som då, returnera det så länkas det omo
                    return branchRoot.left != null ? branchRoot.left : branchRoot.right;
            }
            return branchRoot;
        }


        @Override
        public boolean equals(Object o) {
            Node that = (Node) o;
            if (size != that.size)
                return false;
            if (!data.equals(that.data)) return false;
            if (left != null ? !left.equals(that.left) : that.left != null) return false;
            if (nextLarger != null ? !nextLarger.equals(that.nextLarger) : that.nextLarger != null) return false;
            if (nextSmaller != null ? !nextSmaller.equals(that.nextSmaller) : that.nextSmaller != null) return false;
            if (right != null ? !right.equals(that.right) : that.right != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = data.hashCode();
            result = 31 * result + (left != null ? left.hashCode() : 0);
            result = 31 * result + (right != null ? right.hashCode() : 0);
            result = 31 * result + (nextSmaller != null ? nextSmaller.hashCode() : 0);
            result = 31 * result + (nextLarger != null ? nextLarger.hashCode() : 0);
            result = 31 * result + size;
            return result;
        }

        private boolean hasTwoChildren() {
            return this.left != null && this.right != null;
        }

        private boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        public int depth() {
            return depth;
        }

        private int depth(int count, Node branchRoot) {
            //saknar branchRoot ett subträd kompenseras det med -1 så totala höjden blir 0
            //finns det ett subträd räknar vi ut höjden på det istället
            int leftDepth = branchRoot.left == null ? -1 : depth(count, branchRoot.left);
            int rightDepth = branchRoot.right == null ? -1 : depth(count, branchRoot.right);
            //+1 för nästa nod och den djupaste av träden
            return 1 + Math.max(leftDepth, rightDepth);
        }

        /**
         * Returnerar en str�ngrepresentation f�r det (sub)tr�d som noden utg�r root
         * i. Denna representation best�r av elementens dataobjekt i sorterad
         * ordning med ", " mellan elementen.
         *
         * @return str�ngrepresentationen f�r det (sub)tr�d som noden utg�r root i.
         */
        public String toString() {
            //bygg en sträng,obs kommer ha avslutande komma och space (, )
            String string = buildString("", (this));
            return string.substring(0, string.length() - 2);
        }

        public T getData() {
            return data;
        }

        /*
        * inorder traversal för att skriva ut elementen i ordning
        * Tar INTE hänsyn till vilket element i ordningen man är på
        * @param string
        *              strängen så långt
        * @param branchRoot
        *              subträdet/noden som ska skrivas ut
        */
        private String buildString(String string, Node branchRoot) {
            //vänster subträd
            if (branchRoot.left != null) {
                string = buildString(string, branchRoot.left);
            }
            //noden själv
            string += branchRoot.data.toString() + ", ";
            //höger subträd
            if (branchRoot.right != null)
                string = buildString(string, branchRoot.right);
            return string;
        }

        public Node getRight() {
            return right;
        }

        public Node getLeft() {
            return left;
        }

        public int size() {
            return countSize(0, this);
        }

        private int countSize(int count, Node branchRoot) {
            //fanns inget så räkna inget
            if (branchRoot == null)
                return 0;
            count++;
            //traversera vänstra subträdet om det finns
            count = branchRoot.left == null ? count : countSize(count, branchRoot.left);
            //travesera högra subträdet om det finns
            count = branchRoot.right == null ? count : countSize(count, branchRoot.right);
            return count;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public void setNextSmaller(Node node) {
            nextSmaller = node;
        }

        public void setNextLarger(Node node) {
            nextLarger = node;
        }

        public Node getNextLarger() {
            if (right != null)
                nextLarger = findMinNode(right);
            else {
                nextLarger = getNext();
            }
            return nextLarger;
        }

        public Node getNextSmaller() {
            return nextSmaller;
        }

        public Node findMaxNode() {
            return findMaxNode(this);
        }

        /*
        *@param branchRoot
        *       nod/subträd
        *@return T element, det lägsta i subträdet. Null om parametern branchRoot var felaktig
        */
        private Node findMaxNode(Node branchRoot) {
            if (branchRoot == null)
                return null;
            if (branchRoot.right == null)
                return branchRoot;
            else
                return findMaxNode(branchRoot.right);
        }
    }

    /**
     * Inner iterator class
     */
    private class BSTreeIterator implements Iterator<T> {
        public static final int ORDER_HIGH_TO_LOW = 0;
        public static final int ORDER_LOW_TO_HIGH = 1;
        private Node current;
        private int expectedModCount = modCount;
        public boolean okToRemove = false;

        private BSTreeIterator(int order) {
            current = order == ORDER_LOW_TO_HIGH ? root.findMinNode() : root.findMaxNode();
        }

        public boolean hasNext() {
            if (isEmpty())
                throw new NoSuchElementException();
            //current !=end kommer vara true tills vi nått slutet på listan (noden end)
            try {
                return current.getNextLarger() != tail;
            } catch (NullPointerException npe) {
                return false;
            }
        }

        public T next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (!hasNext())
                throw new NoSuchElementException();
            T nextData = current.getData();
            current = current.getNextLarger();
            okToRemove = true;
            return nextData;
        }

        public boolean hasPrevious() {
            if (isEmpty())
                throw new NoSuchElementException();
            //current !=tail kommer vara true tills vi nått slutet på listan (noden end)
            try {
                return current.getNextSmaller() != lead;
            } catch (NullPointerException npe) {
                return false;
            }
        }

        public T previous() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (!hasPrevious())
                throw new NoSuchElementException();
            T prevData = current.getData();
            current = current.getPrev();
            okToRemove = true;
            return prevData;
        }

        public Node previousNode() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (!hasPrevious())
                throw new NoSuchElementException();
            current = current.getPrev();
            okToRemove = true;
            return current.getPrev();
        }

        public void remove() {
             /*
                Kontrollerar att vi inte försöker modifiera/iterera samma lista från fler än en tråd samtidigt
            */
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
             /*
              Om inte next() körts lyckosamt
            */
            if (!okToRemove)
                throw new IllegalStateException();

            //hämta indexet av elementet i listan
            if (current.getNextSmaller().equals(lead)) {
                BSTreeSet.this.remove(tail.getData());
            } else {
                BSTreeSet.this.remove(current.getData());
            }
            //öka antalet gånger tråden som itererar på listan körts
            expectedModCount++;
            //måste återställas nu när vi tagit bort ett element ifall remove skulle köras senare igen
            okToRemove = false;
        }
    }

}
