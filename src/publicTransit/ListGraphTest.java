package publicTransit;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Test;

public class ListGraphTest {
	
	private Item a;
	private Item b;
	private Item c;
	private Item d;
	private Item e;
	private Item f;
	List<Item> itemList;
	ListGraph<Item> map;
	
	@org.junit.Before
    public void setup() {
		map = new ListGraph(); 
		a = new Item("a");
		b = new Item("b");
		c = new Item("c");
		d = new Item("d");
		e = new Item("e");
		f = new Item("f");
		map.add(a);
		map.add(b);
		map.add(c);
		map.add(d);
		map.add(e);
		map.add(f);
		map.connect(a, b, "atob", 2);
		map.connect(a, c, "atoc", 4);
		map.connect(a, d, "atod", 6);
		map.connect(b, c, "btoc", 1);
		map.connect(c, d, "ctod", 1);
		map.connect(d, e, "dtoe", 1);
		map.connect(a, e, "atoe", 6);
		map.connect(a, f, "atof", 5);
    }
    
    // Tests the connect method. 
    @org.junit.Test(expected = NoSuchElementException.class)
    public void connectOne() throws Exception {
    	Item x = new Item("x");
    	map.connect(x,  a, "name", 2);
        fail("Should have thrown an NoSuchElementException"); 
    } 
    
    // Tests the connect method. 
    @org.junit.Test(expected = IllegalStateException.class)
    public void connectTwo() throws Exception {
    	map.connect(a,  f, "atof", 5);
        fail("Should have thrown an IllegalStateException"); 
    } 
    
    // Tests the getEdgebetween method. 
    @Test
    public void getEdgeBetween() throws Exception {
    	assertTrue(map.getEdgeBetween(b,  c).getName().equals("btoc") && map.getEdgeBetween(b,  c).getWeight() == 1);
    } 
    
    // Tests the getPath method. 
    @Test
    public void getPath() throws Exception {
    	List<Item> items = new ArrayList<>();
    	for (@SuppressWarnings("rawtypes") Edge edge: map.getPath(a, e)) {
    		if (!items.contains(edge.getSource())) {
    			Item source = (Item) edge.getSource();
    			items.add(source);
    		}
    		if (!items.contains(edge.getDestination())) {
    			Item source = (Item) edge.getDestination();
    			items.add(source);
    		}
    	}
    	assertTrue(items.get(0).equals(a) && items.get(1).equals(e) &&
    			items.size() == 2);
    } 
    
    // Tests the getDijkstraPath method. 
	@Test
	public void getDijkstraPathTestOne() throws Exception {
		List<Edge<Item>> itemList = map.getDijkstraPath(e, a);
		assertTrue(itemList.get(0).getSource().equals(a) && itemList.get(1).getSource().equals(b) && itemList.get(2).getSource().equals(c) && 
				itemList.get(3).getSource().equals(d) && itemList.get(3).getDestination().equals(e) && itemList.size() == 4);
	}
	
    // Tests the getDijkstraPath method. 
	@Test
	public void getDijkstraPathTestTwo() throws Exception {
		List<Edge<Item>> itemList = map.getDijkstraPath(f, a);
		assertTrue(itemList.get(0).getSource().equals(a) && itemList.get(0).getDestination().equals(f) && itemList.size() == 1);
	}
	
	// Tests the disconnect method. 
    @org.junit.Test(expected = NoSuchElementException.class)
	public void disconnectOne() throws Exception {
    	Item h = new Item("h");
    	Item j = new Item("j");
    	map.disconnect(h,  j);
        fail("Should have thrown an NoSuchElementException");
	}
    
	// Tests the disconnect method. 
    @org.junit.Test(expected = IllegalStateException.class)
	public void disconnectTwo() throws Exception {
    	Item h = new Item("h");
    	Item j = new Item("j");
    	map.add(h);
    	map.add(j);
    	map.disconnect(h,  j);
        fail("Should have thrown an IllegalStateException");
	}
    
	// Tests the disconnect method. 
    @Test
	public void disconnectThree() throws Exception {
    	map.disconnect(a,  b);
    	assertNull(map.getEdgeBetween(a, b));
	}
    
    @org.junit.Test(expected = NoSuchElementException.class)
	public void changeConnection() throws Exception {
    	Item h = new Item("h");
    	Item j = new Item("j");
    	map.changeConnection(h, j, 2, "change");
        fail("Should have thrown an NoSuchElementException");
	}
    
    @org.junit.Test(expected = IllegalStateException.class)
	public void changeConnection2() throws Exception {
    	Item h = new Item("h");
    	Item j = new Item("j");
    	map.add(h);
    	map.add(j);
    	map.changeConnection(h, j, 2, "change");
        fail("Should have thrown an IllegalStateException");
	}
    
    @Test
	public void changeConnection3() throws Exception {
    	map.changeConnection(a, b, 100, "change");
    	assertTrue(map.getEdgeBetween(a, b).getWeight() == 100 && map.getEdgeBetween(a, b).getName().equals("change"));
	}
}

class Item {
	
	private String name;

	public Item(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}