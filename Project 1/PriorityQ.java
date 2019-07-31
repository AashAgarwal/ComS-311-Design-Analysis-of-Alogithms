/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author agarwal
 */
import java.util.Scanner;

class Method
{
    String s;
    int p;

    public Method(String s, int p)
    {
        this.s = s;
        this.p = p;
    }

    public String toString()
    {
        return "Value : "+ s +"\nKey : "+ p;
    }
}

class PriorityQ
{
    private Method[] A;
    private int i;
    private int Max=700000;

    public PriorityQ()
    {
        this.Max = Max + 1;
        A = new Method[this.Max];
        i = 0;
    }

    public boolean isEmpty()
    {
        return i == 0;
    }

    public String returnMax()
    {
        String item;
        if (isEmpty() ) {
            System.out.println("A is empty");
            return null;
        }
        item = extractMax();
        return item;
    }

    public int getKey(int r)
    {
        Method m = A[r];
        int key = m.p;
        return key;
    }

    public String getValue(int r)
    {
        Method m = A[r];
        String key = m.s;
        return key;
    }

    public void add(String s, int p)
    {
        Method S = new Method(s, p);

        A[++i] = S;
        int pos = i;
        while (pos != 1 && S.p > A[pos/2].p)
        {
            A[pos] = A[pos/2];
            pos /=2;
        }
        A[pos] = S;
    }

    public void remove(int r){
        Method temp;
        if(i == 0){
            System.out.println("The priority queue is empty!! can not remove.");
        }
        temp = A[r];

        A[r] = A[i-1];
        i--;

        heapify(i,r);


    }

    void heapify( int n, int p)
    {

        int largest = p; // Initialize largest as root
        int l = 2*p ; // left = 2*i + 1
        int r = 2*p + 1; // right = 2*i + 2


        if (l <= n && A[l].p > A[largest].p){
            largest = l;

        }

        // If right child is larger than largest so far
        if (r <= n && A[r].p > A[largest].p){
            largest = r;

        }
        if (largest != p)
        {
            Method swap = A[p];
            A[p] = A[largest];
            A[largest] = swap;

            // Recursively heapify the affected sub-tree
            heapify( n, largest);
        }
    }

    public void decrementPriority(int r,int k){

        A[r].p=A[r].p - k;
        heapify(i,r);

    }

    public int[] priorityArray(){

        int[] B = new int[i+1];

        for (int k=1;k<=i;k++){
            B[k] = A[k].p;
        }

        return B;
    }
    
    public String extractMax()
    {
        int parent, child;
        Method item, temp;
        if (isEmpty() )
        {
            System.out.println("A is empty");
            return null;
        }

        item = A[1];
        temp = A[i--];

        parent = 1;
        child = 2;
        while (child <= i)
        {
            if (child < i && A[child].p < A[child + 1].p)
                child++;
            if (temp.p >= A[child].p)
                break;

            A[parent] = A[child];
            parent = child;
            child *= 2;
        }
        A[parent] = temp;
        return item.s;
    }
}
