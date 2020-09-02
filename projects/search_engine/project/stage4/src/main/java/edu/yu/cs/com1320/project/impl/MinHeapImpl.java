package edu.yu.cs.com1320.project.impl;

import edu.yu.cs.com1320.project.MinHeap;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class MinHeapImpl<E extends Comparable<E>> extends MinHeap<E> {

    public MinHeapImpl() {
        this.elements = castToElementArray(new Comparable[5]);
        this.count = 0;
        this.elementsToArrayIndex = new HashMap<>();
    }

    @Override
    public void insert(E x) {
        if (x == null){
            throw new IllegalArgumentException();
        }
        // double size of array if necessary
        if (this.count >= this.elements.length - 1){
            this.doubleArraySize();
        }
        //add x to the bottom of the heap
        this.elements[++this.count] = x;
        this.elementsToArrayIndex.put(x, this.count);
        //percolate it up to maintain heap order property
        this.upHeap(this.count);
        assignIndices();
    }

    @Override
    public void reHeapify(E element) {
        int i = getArrayIndex(element);
        if (i <= 0 ) {
            throw new NoSuchElementException(i + " is out of heap");
        }
        if (isLeaf(i)) {
            if(hasParent(i)) {
                if (isGreater(getParent(i), i)) {
                    this.upHeap(i);
                }
            }
        }
        if (hasLeftChild(i) && hasRightChild(i)){
            if (isGreater(i, getLeftChild(i)) && isGreater(i, getRightChild(i))) {
                this.downHeap(i);
            }
            else {
                this.upHeap(i);
            }
        }
        if (hasLeftChild(i)){
            if (isGreater(i, getLeftChild(i))) {
                this.downHeap(i);
            }
        }
        else {
            this.upHeap(i);
        }
        assignIndices();
    }

    @Override
    public E removeMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Heap is empty");
        }
        E min = this.elements[1];
        //swap root with last, decrement count
        this.swap(1, this.count--);
        //move new root down as needed
        this.downHeap(1);
        this.elements[this.count + 1] = null; //null it to prepare for GC
        this.elementsToArrayIndex.remove(min);
        return min;
    }

    @Override
    protected int getArrayIndex(E element) {
        if (!this.elementsToArrayIndex.containsKey(element)) {
            return -1;
        }
        return this.elementsToArrayIndex.get(element);
    }

    @Override
    protected void doubleArraySize(){
        E[] doubled = castToElementArray(new Comparable[this.elements.length * 2]);
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i] != null){
                doubled[i] = castToElement(this.elements[i]);
            }
            else{
                doubled[i] = null;
            }
        }
        this.elements = doubled;
    }

    @Override
    protected  boolean isGreater(int i, int j) {
        if (this.elements[j] == null) {
            return false;
        }
        return this.elements[i].compareTo(this.elements[j]) > 0;
    }

    /**
     *while the key at index k is less than its
     *parent's key, swap its contents with its parent’s
     */
    @Override
    protected  void upHeap(int k) {
        while (k > 1 && this.isGreater(k / 2, k)) {
            this.swap(k, k / 2);
            k = k / 2;
        }
        assignIndices();
    }

    /**
     * move an element down the heap until it is less than
     * both its children or is at the bottom of the heap
     */
    @Override
    protected  void downHeap(int k) {
        while (2 * k <= this.count) {
            //identify which of the 2 children are smaller
            int j = 2 * k;
            if (j < this.count && this.isGreater(j, j + 1)) {
                j++;
            }
            //if the current value is < the smaller child, we're done
            if (!this.isGreater(k, j)) {
                break;
            }
            //if not, swap and continue testing
            this.swap(k, j);
            k = j;
        }
        assignIndices();
    }

    private void assignIndices(){
        for (int j = 1; j <= this.count; j++) {
            this.elementsToArrayIndex.put(this.elements[j], j);
        }
    }

    @SuppressWarnings("unchecked")
    private E castToElement(Object o){
        return (E) o;
    }

    @SuppressWarnings("unchecked")
    private E[] castToElementArray(Object[] o){
        return (E[]) o;
    }

    private boolean isLeaf(int i){
        return i >= (this.count / 2) && i <= this.count;
    }

    private int getLeftChild(int i){
        return i*2;
    }

    private int getRightChild(int i){
        return (2*i)+1;
    }

    private int getParent(int i){
        return i/2;
    }

    private boolean hasParent(int i){
        if (i/2 >= 1){
            return this.elements[i/2] != null;
        }
        return false;
    }

    private boolean hasLeftChild(int i){
        if (i*2 >= this.elements.length) return false;

        return this.elements[2*i] != null;
    }

    private boolean hasRightChild(int i){
        if ((i*2)+1 >= this.elements.length)
            return false;

        return this.elements[(2*i + 1)] != null;
    }
}