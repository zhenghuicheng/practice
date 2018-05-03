package lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author cheng
 * @note 自旋锁
 */
public class CLHLock implements Lock{

    private ThreadLocal<Node> currentNode;
    private ThreadLocal<Node> preNode;
    private AtomicReference<Node> tail = new AtomicReference<>(new Node());
    private boolean status = false;

    public CLHLock() {
        tail = new AtomicReference<Node>(new Node());
        currentNode = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return new Node();
            }
        };
        preNode = new ThreadLocal<Node>() {
            protected Node initialValue() {
                return null;
            }
        };
    }

    @Override
    public void lock() {
        Node node = new Node();
        currentNode.set(node);
        node.setLock(true);
        Node pred = tail.getAndSet(node);
        preNode.set(pred);
        while (pred.lock) {
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return false;
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        Node node = currentNode.get();
        node.lock = false;
        currentNode.set(null);
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    static class Node {
        //必须定义成volatile, 不然更改后其它线程不知已经更改
        private volatile boolean lock = false;

        public boolean isLock() {
            return lock;
        }

        public void setLock(boolean lock) {
            this.lock = lock;
        }
    }

}
