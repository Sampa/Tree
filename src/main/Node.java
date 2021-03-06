public class Node<T extends Comparable<T>> {
    private T data;
    private Node<T> left;
    private Node<T> right;
    private Node<T> nextSmaller;
    private Node<T> nextLarger;
    private int size; //local size

    public int getSize() {
        return size;
    }

    public Node(T data) {
        this.data = data;
        size = 0;
    }
    public Node<T> getAncestor(){
        Node<T> start = this;
        Node<T> asc = null;
        while(start !=null){
            if( this.getData().compareTo(start.getData())< 0){
                asc = start;
                start = start.getLeft();
            }else
                start = start.getRight();
        }
        return asc;
    }

    /**
     * L�gger till en nod i det bin�ra s�ktr�det. Om noden redan existerar s�
     * l�mnas tr�det of�r�ndrat.
     *
     * @param data
     *            datat f�r noden som ska l�ggas till.
     * @return true om en ny nod lades till tr�det.
     */
    public boolean add(T data) {
        try {
            Node<T> node = add(data,this);
            if(node.right !=null)
                node.nextLarger = findMinNode(node.right);
            else{
                node.nextLarger = getAncestor();
            }
            node.nextSmaller = findMinNode(node.left);
        }catch (IllegalStateException ise){
            return false;
        }

        return true;
    }

    public T getData() {
        return data;
    }

    /*
        *  compareTo docs:
        *  "It is strongly recommended, but not strictly required that (x.compareTo(y)==0) == (x.equals(y))"
        *  Därför ingen if(data.equals(branchRoot)) return null;
        *  @param branchRoot noden vi är på alias subträdets root
        *  @return nya noden
        */
    private Node<T> add(T data, Node<T> branchRoot){
        //basecase, vi har hittat en plats där nya noden ska ligga
        if(branchRoot == null) {
            Node<T> node = new Node<>(data);

            return node;
        }
        //kan ha 3 lägen: lägre==-1, högre==1, equal==0
        int result = data.compareTo(branchRoot.data);
        if(result<0)
            branchRoot.left = add(data, branchRoot.left);
        else if(result>0)
            branchRoot.right = add(data, branchRoot.right);
        else
            throw new IllegalStateException();
        return branchRoot;
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
    private T findMin(Node<T> branchRoot) {
        if (branchRoot.left==null)
            return branchRoot.data;
        else
            return findMin(branchRoot.left);
    }
    public Node<T> findMinNode() {
        return findMinNode(this);
    }
    /*
    *@param branchRoot
    *       nod/subträd
    *@return T element, det lägsta i subträdet. Null om parametern branchRoot var felaktig
    */
    private Node<T> findMinNode(Node<T> branchRoot) {
        if(branchRoot==null)
            return null;
        if (branchRoot.left==null)
            return branchRoot;
        else
            return findMinNode(branchRoot.left);
    }
    /**
     * Tar bort ett element ur tr�det. Om elementet inte existerar s l�mnas
     * tr�det of�r�ndrat.
     *
     * @param info
     *            elementet som ska tas bort ur tr�det.
     * @return en referens till nodens subtr�d efter borttaget.
     */
    public Node<T> remove(T info) {
        return remove(info,this);
    }


    private Node<T> remove(T data, Node<T> branchRoot) {
        if(branchRoot==null)
            return null;
        int result = data.compareTo(branchRoot.data);
        if(result<0)
            branchRoot.left = remove(data,branchRoot.left);
        else if(result>0)
            branchRoot.right = remove(data,branchRoot.right);
        else{
            //found it
            if(branchRoot.isLeaf())
                return  null;
            if(branchRoot.hasTwoChildren()){
                branchRoot.data = findMin(branchRoot.right);
                branchRoot.right = remove(branchRoot.data, branchRoot.right);
                return branchRoot;
            }else //har bara ett barn som då, returnera det så länkas det omo
                return branchRoot.left != null ? branchRoot.left : branchRoot.right;
        }
        return branchRoot;
    }


    @Override
    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
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
        return this.left !=null && this.right !=null;
    }

    private boolean isLeaf() {
        return this.left==null && this.right==null;
    }



    /**
     * Det h�gsta djupet i det (sub)tr�d som noden utg�r root i.
     *
     * @return djupet.
     */
    public int depth() {
        return depth(0,this);
    }
    /*
    * @param count
    *       räknar djupet
    * @param branchRoot
    *       noden vi undersökar, kan vara null referens
    * vi räknar fram 1 och adderar det största av subträdens höjd, om vi stöter på en null-pekare tar vi bort 1
    * lite annorlunda än bokens då vi inte gör något recursivt anrop ifall vi vet att det kommer att vara tomt åt det hållet
    */
    private int depth(int count, Node<T> branchRoot){
        //saknar branchRoot ett subträd kompenseras det med -1 så totala höjden blir 0
        //finns det ett subträd räknar vi ut höjden på det istället
        int leftDepth = branchRoot.left == null ? -1: depth(count,branchRoot.left);
        int rightDepth = branchRoot.right == null ? -1: depth(count,branchRoot.right);
        //+1 för nästa nod och den djupaste av träden
        return 1+Math.max(leftDepth,rightDepth);
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
        return string.substring(0,string.length()-2);
    }

    /*
    * inorder traversal för att skriva ut elementen i ordning
    * Tar INTE hänsyn till vilket element i ordningen man är på
    * @param string
    *              strängen så långt
    * @param branchRoot
    *              subträdet/noden som ska skrivas ut
    */
    private String buildString(String string, Node<T> branchRoot) {
        //noden själv
        string +=branchRoot.data.toString()+", ";
        //vänster subträd
        if(branchRoot.left!= null) {
            string = buildString(string,branchRoot.left);
        }
        //höger subträd
        if(branchRoot.right!= null)
            string = buildString(string,branchRoot.right);
        return string;
    }

    public Node<T> getRight() {
        return right;
    }

    public Node<T> getLeft() {
        return left;
    }
    public int size() {
        return countSize(0,this);
    }
    /*
* @param count
* vad räkningen är på
* @param branchRoot
* noden som ska tittas på
*/
    private int countSize(int count, Node<T> branchRoot){
        //fanns inget så räkna inget
        if(branchRoot==null)
            return 0;
        count++;
        //traversera vänstra subträdet om det finns
        count = branchRoot.left == null ? count : countSize(count,branchRoot.left);
        //travesera högra subträdet om det finns
        count = branchRoot.right == null ? count : countSize(count, branchRoot.right);
        return count;
    }
    public void setSize(int size) {
        this.size = size;
    }

    public void setNextSmaller(Node<T> node){
        nextSmaller = node;
    }
    public void setNextLarger(Node<T> node){
        nextLarger = node;
    }

    public Node<T> getNextLarger() {
        if(right !=null)
           nextLarger = findMinNode(right);
        else{
            nextLarger = getAncestor();
        }
        return nextLarger;
    }

    public Node<T> getNextSmaller() {
        return nextSmaller;
    }
}