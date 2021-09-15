package dataStrctImpl.myJava.dataStructures.interfaces.impl;

import edu.yu.cs.com1320.project.Stack;

public class StackImpl<T> implements Stack<T> {

    private T[] stack;
    private int top = -1;
    private int count = 0;

    public StackImpl() {
        Object[] array = new Object[5];
        this.stack = castToTArray(array);
    }

    /**
     * @param element object to add to the Stack
     */
    @Override
    public void push(T element) {
        if (element == null){
            throw new IllegalArgumentException();
        }
        if (top == this.stack.length -1){
            reStack();
        }
        top++;
        this.stack[top] = element;
        count++;
    }

    /**
     * removes and returns element at the top of the stack
     * @return element at the top of the stack, null if the stack is empty
     */
    @Override
    public T pop() {
        if(peek() != null) {
            T element = peek();
            this.stack[top] = null;
            top--;
            count--;
            return element;
        }
        return null;
    }

    /**
     *
     * @return the element at the top of the stack without removing it, or null if stack is empty
     */
    @Override
    public T peek() {
        if (isEmpty()){
            return null;
        }
        return this.stack[top];
    }

    /**
     *
     * @return how many elements are currently in the stack
     */
    @Override
    public int size() {
        return this.count;
    }

    private T[] stackDoubler(){
        Object[] doubled = new Object[this.stack.length * 2];
        return castToTArray(doubled);
    }

    private void reStack(){
        if (!isEmpty()){
            T temp = pop();
            if (!isEmpty()){
                reStack();
            }
            if (isEmpty()){
                this.stack = stackDoubler();
            }
            push(temp);
        }
    }

    private boolean isEmpty(){
        return count == 0;
    }

    @SuppressWarnings("unchecked")
    private T[] castToTArray (java.lang.Object[] o){
        return (T[]) o;
    }
}
