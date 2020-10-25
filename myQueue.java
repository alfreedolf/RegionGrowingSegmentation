/**
 * Queue class represents a queue of objects in which elements are removed in the same 
 * order they were entered.This is often referred to as first-in-first-out (FIFO).
 *
 * @author Yasser EL-Manzalawy <ymelamanz@yahoo.com>
 *
 *  This software is provided as is, without representation as to its
 *  fitness for any purpose, and without warranty of any kind, either
 *  express or implied, including without limitation the implied
 *  warranties of merchantability and fitness for a particular purpose.
 *  The author shall not be liable for any damages, including special, 
 *  indirect,incidental, or consequential damages, with respect to any claim
 *  arising out of or in connection with the use of the software, even
 *  if they have been or are hereafter advised of the possibility of
 *  such damages.
 */
 
import java.util.LinkedList;

public class myQueue
{

	private LinkedList<Object> items;

	/**
	 * Creats an empty queue
	 */
	 
	public myQueue ()
	{
		items = new LinkedList<Object>();
	}
	
	/**
	 * Inserts a new element at the rear of the queue.
	 * @param element element to be inserted.
	 */
	
	public Object enqueue (Object element)
	{
		items.add (element);
		return element;
	}
	
	/**
	 * Removes the element at the top of the queue.
	 * @return the removed element.
	 * @throws EmptyQueueException if the queue is empty.
	 */

	public Object dequeue () 
	{
		if (items.size()== 0)	
			throw new EmptyQueueException() ;
		return items.removeFirst();		
	}

	/**
	 * Inspects the element at the top of the queue without removing it.
	 * @return the element at the top of the queue.
	 * @throws EmptyQueueException if the queue is empty.
	 */
	
	public Object front () 
	{
		if (items.size()== 0)	
			throw new EmptyQueueException() ;
		return items.getFirst();	
	}
	
	/**
	 * @return the number of elements at the queue.
	 */

	public int size()
	{
		return items.size();
	}

	/**
	 * @return true if the queue is empty.
	 */	

	public boolean empty()
	{
		return (size()==0);
	}

	/**
	 * Removes all elements at the queue.
	 */	

	public void clear ()
	{
		items.clear();
	}    

}