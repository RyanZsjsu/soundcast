package com.ahmilio.turtle.soundcast;

import java.io.Serializable;
import java.util.ArrayList;

// Describes a queue
public class PlayQueue<U> implements Iterable<U>, Serializable {
    private Node<U> head;
    private Node<U> tail;
    private int size;
    
    // Describes a linked list node
    private class Node<E> {
        private E data;
        private Node<E> next;
        
        public Node(E data){ this(data,null); }	// 1-arg ctor
        public Node(E data, Node<E> next){		// 2-arg ctor
            this.data = data;
            this.next = next;
        }
        
        // string representation of node
        public String toString(){ return data.toString(); }
    }
    
    // no-args ctor
    public PlayQueue(){};
    
    // n-args ctor
    public PlayQueue(U... args){
    	for (int i = 0; i < args.length; i++)
    		this.enqueue(args[i]);
    }
    
    // clears the list of all entries
    public void clear(){ head = tail = null; size = 0; }
    
    // determines whether list is empty
    public boolean isEmpty(){ return head == null; }
    
    // returns the size of the list
    public int size(){ return size; }
    
    // finds and returns the index of an element in the queue
    // returns -1 if element is not in queue
    public int indexOf(U element){
    	int index = -1;
    	boolean found = false;
    	for (U e : this){
    		index++;
    		if (found = e.equals(element))
    			break;
    	}
    	return found ? index : -1;
    }
    
    // checks if queue contains element in O(n) time
    public boolean contains(U element){
    	for (U e : this)
    		if (element.equals(e))
    			return true;
    	return false;
    }
    
    // inserts an element at the end of the queue
    public boolean enqueue(U element){
        if (isEmpty())
            head = tail = new Node<U>(element);
        else {
            tail.next = new Node<U>(element);
            tail = tail.next;
        }
        size++;
        return true;
    }
    
    // remove element at certain index
    public U remove(int index){
    	Node<U> rem = null;
    	if (index == 0){
    		rem = head;
    		head = head.next;
    	}
    	else if (index >= 0 && index < size){
        	Node<U> cur = head;
        	for (int i = 1; i < index; i++, cur = cur.next);
        	rem = cur.next;
        	cur.next = rem.next;
    	}
    	return rem.data;
    }
    
    // inserts a compatible queue at the end of this queue
    public boolean enqueue(PlayQueue<U> that){
    	for (U e : that)
    		this.enqueue(e);
    	return true;
    }
    
    // deletes the element at the start of the queue
    public U dequeue(){
    	if (isEmpty())
    		return null;
    	Node<U> rem = head;
        rem = head;
        head = head.next;
        if (tail == rem) // if list previously had one element
        	tail = tail.next;
        size--;
        return rem.data;
    }
    
    // returns an array of all the elements in the queue
    public ArrayList<U> toArray(){
    	ArrayList<U> arr = new ArrayList<>(size);
    	int i = 0;
    	for (U e : this)
    		arr.add(e);
    	return arr;
    }
    
    // returns an iterator for the object
    public java.util.Iterator<U> iterator()
    { return new LinkedListIterator(); }

    // Describes an iterator for the class
    private class LinkedListIterator implements java.util.Iterator<U>{
        private Node<U> cur = null;

        public boolean hasNext()
        { return cur != tail; }

        public U next() {
            if (cur == null)
                cur = head;
            else if (cur.next == null) 
                throw new java.util.NoSuchElementException();
            else
                cur = cur.next;
            return cur.data;
        }

        public void remove(){ return; }
    }
    
    // string representation of queue
    public String toString(){
        String out = "[";
        Node<U> cur = head;
        while (cur != null){
            out += cur.toString();
            if (cur.next != null)
            	out += ", ";
            cur = cur.next;
        }
        return out + "]";
    }
}
