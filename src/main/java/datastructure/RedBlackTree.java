package datastructure;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

/**
 * @author cheng
 * 参考的网站：https://zh.wikipedia.org/zh-cn/红黑树
 * 红黑树的定义：
 * 1、节点是红色或者是黑色
 * 2、根节点是黑色
 * 3、每个叶节点是黑色
 * 4、每个红色节点的两个子节点都是黑色的（也就是说不存在两个连续的红色节点）
 * 5、从任一个节点到其每个叶节的所有路径都包含相同数量的黑色节点
 */
public class RedBlackTree<T extends Comparable<T>> {

    private RBNode<T> root;

    public boolean add(T t) {
        RBNode node = new RBNode(t, null);
        addNode(node);
        return Boolean.TRUE;
    }

    public boolean delete(T t) {
        if (Objects.isNull(root)) {
            return Boolean.FALSE;
        }
        RBNode<T> current = root;
        while (Objects.nonNull(current)) {
            int cmp = t.compareTo(current.getData());
            if (cmp == 0) {
                break;
            } else if (cmp < 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }
        //找到不删除的结点
        if (Objects.isNull(current)) {
            return Boolean.FALSE;
        }

        if (Objects.isNull(current.getRight())) {
            //如果删除的结点没有右子树的话,该结点直接删除
            remove(current);
        } else {
            //有右子树，找到右子树的最小值结点
            RBNode smallNode = getSmallNode(current.getRight());
            swap(current, smallNode); //交换数值,即smallNode现在的值就是要删除的值,此时smallNode成为要删除的点
            remove(smallNode);
        }

        return Boolean.TRUE;
    }

    public void remove(RBNode<T> t) {
        RBNode<T> child = t.getLeft() != null ? t.getLeft() : t.getRight();
        if (Objects.isNull(t.getParent())) {//删除结点为根结点
            root = child;
            if (Objects.nonNull(child)) { //如果还有子结点，将子结点置为黑色
                setNodeBlack(child);
            }
            remarkDeleted(t);//标记删除结点
            return;
        }

        RBNode<T> ancestor = getParent(t);
        //剔除掉删除结点
        if (t == ancestor.getLeft()) { //删除结点是在父结点的左边
            ancestor.setLeft(child);
        } else {  //删除结点是在父结点的右边
            ancestor.setRight(child);
        }
        if (isRed(t)) {//删除结点是红色
            remarkDeleted(t);//标记删除结点
            return;
        }
        //删除结点是黑色，要调整
        if (Objects.nonNull(child) && isRed(child)) { //代替结点child为红色，把它置为黑色
            setNodeBlack(child);
        } else if (Objects.isNull(child)) {//child为null, 构造一个黑色结点，设置父结点为ancestor,T为null，便于后面统一处理
            RBNode<T> node = new RBNode<T>(null, false, ancestor, null, null);
            child = node;
            if (ancestor.getLeft() == null) {
                ancestor.setLeft(child);
            } else {
                ancestor.setRight(child);
            }
            removeFixUp(child);
        } else {
            removeFixUp(child);
        }

    }

    public void removeFixUp(RBNode<T> a) {
        RBNode<T> current = a;
        RBNode<T> parent = null;
        RBNode<T> sibling = null;

        while (Objects.nonNull(current.getParent()) && !isRed(current)) { //case1:新结点为根结点，不需要做调整
            parent = getParent(current);
            sibling = getBrother(current);
            if (isRed(sibling)) { //case2:兄弟结点为红色,根据情况左旋或右旋，调整为兄弟结点为黑色，以便进一步处理
                setNodeRed(parent);
                setNodeBlack(sibling);
                if (isLeftNode(current)) {
                    leftRotate(parent);
                } else {
                    rightRotate(parent);
                }
            } else if (isBlack(sibling.getLeft()) && isBlack(sibling.getRight())) { //case3:兄弟结点的左右结点都为黑色
                setNodeRed(sibling); //兄弟置为红色
                current = parent;//调整结点变为父结点
            } else { //兄弟结点的左右结点至少有一个是红色结点
                if(isLeftNode(current)) {//调整结点是左结点
                    if (isRed(sibling.getRight())) { //case5:兄弟结点的右结点是红色，对父结点做左旋
                        sibling.setColor(parent.getColor());
                        setNodeBlack(parent);
                        setNodeBlack(sibling.getRight());
                        leftRotate(parent);
                        current = root;
                    } else { //case4:兄弟结点的左结点是红色，对兄弟结点右旋,下一步就是case5
                        setNodeBlack(sibling.getLeft());
                        setNodeRed(sibling);
                        rightRotate(sibling);
                    }
                } else { //调整结点是右结点
                    if (isRed(sibling.getLeft())) { //case5:兄弟结点的左结点是红色，对父结点做右旋
                        setNodeBlack(sibling.getLeft());
                        setNodeRed(sibling);
                        rightRotate(sibling);
                        current = root;
                    } else {//case5:兄弟结点的右结点是红色，对兄弟结点左旋，下一步就是case5
                        setNodeRed(sibling);
                        setNodeBlack(sibling.getRight());
                        leftRotate(sibling);
                    }
                }

            }
        }

        setNodeBlack(current);
        if (Objects.isNull(a.getData())) { //点a的数据data为null,
            if (isLeftNode(a)) { //如果是父结点的左孩子，父结点的左孩子为null
                getParent(a).setLeft(null);
            } else {
                getParent(a).setRight(null);
            }
            remarkDeleted(a);//标记结点删除
        }

    }

    /**标记结点删除，以便gc*/
    public void remarkDeleted(RBNode<T> t) {
        if (Objects.isNull(t)) {
            return;
        }
        t.setData(null);
        t.setLeft(null);
        t.setRight(null);
        t.setParent(null);
    }

    /**交换数值，即交换结点中的data*/
    public void swap(RBNode<T> a, RBNode<T> b) {
        if (Objects.isNull(a) || Objects.isNull(b)) {
            return;
        }
        T value = a.getData();
        a.setData(b.getData());
        b.setData(value);
    }

    /**找到该结点所有的树最小结点*/
    public RBNode<T> getSmallNode(RBNode<T> t) {
        if (Objects.isNull(t)) {
            return t;
        }
        while (Objects.nonNull(t.getLeft())) { //循环走左结点，左结点比较小
            t = t.getLeft();
        }
        return t;
    }

    public RBNode<T> find(T t) {
        RBNode<T> current = root;
        while (current != null) {
            int cmp = t.compareTo(current.getData());
            if (cmp == 0) {
                return current;
            } else if (cmp < 0) {
                current = current.getLeft();
            } else {
                current = current.getRight();
            }
        }

        return null;
    }

    public void addNode(RBNode<T> a) {
        RBNode<T> current = this.root;
        RBNode<T> parent = null;
        int cmp = 0;
        while (null != current) {//查找插入结点的位置
            cmp = a.getData().compareTo(current.getData());
            if (cmp == 0)  {
                return;
            }
            parent = current;
            current = cmp < 0 ? current.getLeft() : current.getRight();
        }
        if (Objects.isNull(parent)) {//没有结点时，root为结点
            setNodeBlack(a);
            root = a;
            return;
        }
        //找到位置，插入结点
        if (cmp < 0) {
            parent.setLeft(a);
        } else {
            parent.setRight(a);
        }
        //将树重新调整成红黑树
        insertFixUp(a);
    }

    public void insertFixUp(RBNode a) {
        RBNode current = a;
        RBNode parent, ancestor, uncle;

        while (Objects.nonNull(parent = getParent(current)) && isRed(parent)) { //父结点为null或父结点颜色为黑，就不用调整了，跳出循环
            ancestor = getParent(parent);
            uncle = getUncle(current);
            if (Objects.nonNull(uncle) && isRed(uncle)) { //叔结点也为红，将父、叔结点都变为黑，祖父结点变为红
                setNodeBlack(parent);
                setNodeBlack(uncle);
                setNodeRed(ancestor);
                current = ancestor;  //祖父结点继续做调整
            } else { //叔结点为黑
                if (parent == ancestor.getLeft()) { //插入结点在祖父结点左边，最终是右旋
                    boolean isRight = current == parent.getRight();
                    if (isRight) {//插入结点在父结点右边
                        leftRotate(parent); //先左旋
                    }
                    rightRotate(ancestor);
                    setNodeBlack(isRight ? current : parent);//如果插入结点在父结点右边,插入结点会成为原来的父结点parent和原来的祖父ancestor的父结点
                    setNodeRed(ancestor);
                    current = null; //跳出循环
                } else { //插入结点在祖父结点右边，最终是左旋
                    boolean isLeft = current == parent.getLeft();
                    if (isLeft) {//插入结点在父结点左边
                        rightRotate(parent); //先右旋
                    }
                    leftRotate(ancestor);
                    setNodeBlack(isLeft ? current : parent);//如果插入结点在父结点左边,插入结点会成为原来的父结点parent和原来的祖父ancestor的父结点
                    setNodeRed(ancestor);
                    current = null; //跳出循环
                }
            }
        }
        setNodeBlack(root);//有可能根结点为红点，直接置为黑，因为根据不会影响性质5，根据根结点不会影响下面节点计算到任意叶节点的黑色结点个数
        root.setParent(null);
    }

    static class RBNode<T extends Comparable<T>> {
        /**父结点*/
        private RBNode<T> parent;
        /**左结点*/
        private RBNode<T> left;
        /**右结点*/
        private RBNode<T> right;
        /**数据*/
        private T data;
        /**颜色(true = red, false = black)*/
        private boolean color;

        public RBNode(T data, boolean isRed, RBNode parent, RBNode left, RBNode right) {
            this.data = data;
            color = isRed;
            this.parent = parent;
            this.left = left;
            this.right = right;
        }

        public RBNode(T data, RBNode parent) {
            this(data, Boolean.TRUE, parent, null, null);
        }

        public RBNode<T> getParent() {
            return parent;
        }

        public void setParent(RBNode<T> parent) {
            this.parent = parent;
        }

        public RBNode<T> getLeft() {
            return left;
        }

        public void setLeft(RBNode<T> left) {
            this.left = left;
            if (Objects.nonNull(left)) {
                left.parent = this;
            }
        }

        public RBNode<T> getRight() {
            return right;
        }

        public void setRight(RBNode<T> right) {
            this.right = right;
            if (Objects.nonNull(right)) {
                right.setParent(this);
            }
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }

        public boolean getColor() {
            return color;
        }

        public void setColor(boolean color) {
            this.color = color;
        }
    }

    /**
     * 左旋示意图：对节点x进行左旋
     *     p                       p
     *    /                       /
     *   x                       y
     *  / \                     / \
     * lx  y      ----->       x  ry
     *    / \                 / \
     *   ly ry               lx ly
     * 左旋做了三件事：
     * 1. 将y的左子节点赋给x的右子节点,并将x赋给y左子节点的父节点(y左子节点非空时)
     * 2. 将x的父节点p(非空时)赋给y的父节点，同时更新p的子节点为y(左或右)
     * 3. 将y的左子节点设为x，将x的父节点设为y
     */
    private void leftRotate(RBNode x) {
        if (Objects.isNull(x) || Objects.isNull(x.right)) {
            return;
        }
        RBNode y = x.right;
        x.setRight(y.left);
        y.parent = x.parent;


        if (Objects.isNull(y.parent)) {
            this.root = y;
        } else {
            if (isLeftNode(x)) {
                x.parent.setLeft(y);
            } else {
                x.parent.setRight(y);
            }
        }
        y.setLeft(x);
    }
    /**
     * 左旋示意图：对节点y进行右旋
     *        p                   p
     *       /                   /
     *      y                   x
     *     / \                 / \
     *    x  ry   ----->      lx  y
     *   / \                     / \
     * lx  rx                   rx ry
     * 右旋做了三件事：
     * 1. 将x的右子节点赋给y的左子节点,并将y赋给x右子节点的父节点(x右子节点非空时)
     * 2. 将y的父节点p(非空时)赋给x的父节点，同时更新p的子节点为x(左或右)
     * 3. 将x的右子节点设为y，将y的父节点设为x
     */
    public void rightRotate(RBNode<T> y) {
        if (Objects.isNull(y) || Objects.isNull(y.left)) {
            return;
        }
        RBNode x = y.left;
        y.setLeft(x.getRight());
        x.parent = y.parent;

        if (Objects.isNull(x.parent)) {
            this.root = x;
        } else if (isLeftNode(y)) {
            y.parent.setLeft(x);
        } else {
            y.parent.setRight(x);
        }
        x.setRight(y);
    }

    private boolean isLeftNode(RBNode<T> child) {
        if (Objects.isNull(child) || Objects.isNull(getParent(child))) {
            return Boolean.FALSE;
        }
        if (child == getParent(child).getLeft()) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    private boolean isRed(RBNode<T> a) {
        if (Objects.isNull(a)) {
            return Boolean.FALSE;
        }
        return a.getColor() == true;
    }

    private boolean isBlack(RBNode<T> a) {
        if (Objects.isNull(a)) {
            return Boolean.TRUE;
        }
        return a.getColor() == false;
    }

    private RBNode getParent(RBNode<T> a) {
        return Objects.isNull(a) ? null : a.parent;
    }

    private RBNode getUncle(RBNode a) {
        RBNode parent;
        if (Objects.isNull(parent = getParent(a))) {
            return null;
        }
        RBNode ancestor;
        if (Objects.isNull(ancestor = getParent(parent))) {
            return null;
        }
        if (parent == ancestor.getLeft()) {
            return ancestor.getRight();
        } else {
            return ancestor.getLeft();
        }
    }

    private void setNodeRed(RBNode a) {
        if (Objects.nonNull(a)) {
            a.setColor(Boolean.TRUE);
        }
    }

    private void setNodeBlack(RBNode b) {
        if (Objects.nonNull(b)) {
            b.setColor(Boolean.FALSE);
        }
    }

    public void printfTree() {
        Queue<RBNode<T>> rbNodeQueue1 = new LinkedList();
        Queue<RBNode<T>> rbNodeQueue2 = new LinkedList<>();
        rbNodeQueue1.offer(root);
        RBNode current = null;
        boolean firstQueue = true;


        while (!rbNodeQueue1.isEmpty() || !rbNodeQueue2.isEmpty()) {
            current = firstQueue ? rbNodeQueue1.poll() : rbNodeQueue2.poll();
            if (Objects.nonNull(current)) {
                StringBuilder str = new StringBuilder();
                str.append(current.getData() + "(");
                str.append(current.getColor() ? "R)" : "B)" );
                if (Objects.nonNull(current.getParent())) {
                    str.append("[" + current.getParent().getData() + "]  ");
                }
                System.out.print(str.toString());
                if (Objects.nonNull(current.getLeft())) {
                    (firstQueue ? rbNodeQueue2 : rbNodeQueue1).offer(current.getLeft());
                }
                if (Objects.nonNull(current.getRight())) {
                    (firstQueue ? rbNodeQueue2 : rbNodeQueue1).offer(current.getRight());
                }
            } else {
                System.out.println();
                firstQueue = !firstQueue;
            }
        }
        System.out.println();
    }

    public static void main(String[] args) {
//        RedBlackTree<String> bst = new RedBlackTree<String>();
//        bst.add("d");
//        bst.add("d");
//        bst.add("c");
//        bst.add("c");
//        bst.add("b");
//        bst.add("f");
//
//        bst.add("a");
//        bst.add("e");
////
//        bst.add("g");
//        bst.add("h");
        RedBlackTree<Integer> bst = new RedBlackTree<>();
        bst.add(27);
        bst.add(15);
        bst.add(7);
        bst.add(18);
        bst.add(40);
        bst.add(16);
        bst.add(17);
        bst.add(100);
        bst.add(120);
        bst.add(200);
        bst.add(1);
        bst.add(180);
        bst.add(220);
        bst.add(240);
        bst.add(280);
        bst.add(400);
        bst.add(600);
        bst.add(800);
        bst.add(700);
        bst.add(750);
        bst.add(900);
        bst.add(790);

        bst.printfTree();

        bst.delete(700);
        bst.printfTree();
        bst.delete(400);
        bst.printfTree();
        bst.delete(900);
        bst.printfTree();
        bst.delete(220);
        bst.printfTree();


    }

    private RBNode<T> getBrother(RBNode<T> a) {
        if (Objects.isNull(a)) {
            return null;
        }
        RBNode<T> parent = getParent(a);
        if (Objects.isNull(parent)) {
            return null;
        }
        if (a == parent.getRight()) {
            return parent.getLeft();
        } else {
            return parent.getRight();
        }
    }

}
