package ch.lambdaj;

import static ch.lambdaj.Lambda.*;
import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.junit.*;

import ch.lambdaj.function.closure.*;
import ch.lambdaj.mock.*;

/**
 * @author Mario Fusco
 */
public class ClosureTest {
	
	public Integer add(Integer val1, Integer val2) {
		return val1 + val2;
	}

	public int nonCommutativeDoOnInt(int val1, int val2, int val3) {
		return (val1 - val2) * val3;
	}
	
	public static interface NonCommutativeDoer {
		int nonCommutativeDoOnInt(int val1, int val2, int val3);
	}

	@Test
	public void testSystemOut() {
		Closure1<String> println = closure(String.class); { of(System.out).println(arg(String.class)); } 
		println.each("mickey mouse", "donald duck", "uncle scrooge");
	}
	
	@Test
	public void testOnList() {
		StringWriter sw = new StringWriter();
		Closure writer = closure(); { of(sw).write(arg(String.class)); } 
		writer.each(asList("first", "second", "third"));
		assertEquals("firstsecondthird", sw.toString());
	}

	@Test
	public void testOnArray() {
		StringWriter sw = new StringWriter();
		Closure writer = closure(); { of(sw).append(arg(String.class)); } 
		writer.each("first", "second", "third");
		assertEquals("firstsecondthird", sw.toString());
		
		writer.apply("forth");
		assertEquals("firstsecondthirdforth", sw.toString());
		
		try {
			writer.apply(3);
			fail("An invocation with the wrong parameter type must fail");
		} catch (WrongClosureInvocationException wcie) { }
		
		try {
			writer.apply("fifth", "sixth");
			fail("An invocation with the wrong parameter number must fail");
		} catch (WrongClosureInvocationException wcie) { }
	}

	@Test
	public void testTypedOnList() {
		StringWriter sw = new StringWriter();
		Closure1<String> writer = closure(String.class); { of(sw).append(arg(String.class)); } 
		writer.each(asList("first", "second", "third"));
		assertEquals("firstsecondthird", sw.toString());
		writer.apply("forth");
		assertEquals("firstsecondthirdforth", sw.toString());
	}

	@Test
	public void testTypedOnArray() {
		StringBuilder sb = new StringBuilder();
		Closure1<String> appender = closure(String.class); { 
			try {
				of(sb, Appendable.class).append(arg(String.class));
			} catch (IOException e) { } 
		} 
		appender.each("first", "second", "third");
		assertEquals("firstsecondthird", sb.toString());
	}
	
	@Test
	public void testAddOnInteger() {
		Closure2<Integer, Integer> adder = closure(Integer.class, Integer.class); { 
			of(this).add(arg(Integer.class), arg(Integer.class)); 
		} 
		int sum = (Integer)adder.apply(2, 3);
		assertEquals(2 + 3, sum);
	}

	@Test
	public void testDo3OnInt() {
		Closure3<Integer, Integer, Integer> adder = closure(Integer.class, Integer.class, Integer.class); { 
			of(this).nonCommutativeDoOnInt(arg(Integer.class), arg(Integer.class), arg(Integer.class)); 
		} 
		int result = (Integer)adder.apply(5, 2, 4);
		assertEquals((5 - 2) * 4, result);
	}

	@Test
	public void testDo2OnInt() {
		Closure2<Integer, Integer> adder = closure(Integer.class, Integer.class); { 
			of(this).nonCommutativeDoOnInt(arg(Integer.class), 2, arg(Integer.class)); 
		} 
		int result = (Integer)adder.apply(5, 4);
		assertEquals((5 - 2) * 4, result);
	}

	@Test
	public void testCurry() {
		Closure3<Integer, Integer, Integer> closure3 = closure(Integer.class, Integer.class, Integer.class); { 
			of(this).nonCommutativeDoOnInt(arg(Integer.class), arg(Integer.class), arg(Integer.class)); 
		} 
		int result = (Integer)closure3.apply(5, 2, 4);
		assertEquals((5 - 2) * 4, result);
		
		Closure2<Integer, Integer> closure2 = closure3.curry2(2);
		result = (Integer)closure2.apply(7, 3);
		assertEquals((7 - 2) * 3, result);
		
		Closure1<Integer> closure1 = closure2.curry2(5);
		result = (Integer)closure1.apply(4);
		assertEquals((4 - 2) * 5, result);
		
		Closure0 closure0 = closure1.curry(9);
		result = (Integer)closure0.apply();
		assertEquals((9 - 2) * 5, result);
	}
	
	@Test
	public void testWrongCurry() {
		Closure closure = closure(); { of(this).nonCommutativeDoOnInt(arg(Integer.class), arg(Integer.class), arg(Integer.class)); } 
		try {
			closure.curry(3, 4);
			fail("Curry on wrong argument position must fail");
		} catch (IllegalArgumentException iae) { }
	}

	@Test
	public void testAs() {
		Closure3<Integer, Integer, Integer> closure3 = closure(Integer.class, Integer.class, Integer.class); { 
			of(this).nonCommutativeDoOnInt(arg(Integer.class), arg(Integer.class), arg(Integer.class)); 
		}
		NonCommutativeDoer doer = closure3.as(NonCommutativeDoer.class);
		int result = doer.nonCommutativeDoOnInt(5, 2, 4);
		assertEquals((5 - 2) * 4, result);
	}

	@Test
	public void testWrongAs() {
		Closure closure = closure(); { of(this).nonCommutativeDoOnInt(arg(Integer.class), arg(Integer.class), arg(Integer.class)); } 
		try {
			closure.as(String.class);
			fail("Closure cast on concrete class must fail");
		} catch (IllegalArgumentException iae) { }
		try {
			closure.as(Iterator.class);
			fail("Closure cast on interface having more than one method  must fail");
		} catch (IllegalArgumentException iae) { }
	}

	@Test
	public void testClosureOnNonFinalArgument() {
		Person me = new Person("Mario", "Fusco");
		Closure2<Person, Integer> ageSetter = closure(Person.class, Integer.class); { 
			of(this).setAgeOnPerson(arg(Person.class), arg(Integer.class)); 
		}
		ageSetter.apply(me, 35);
		assertEquals(35, me.getAge());
		
		Closure1<Integer> ageSetterOnMyself = ageSetter.curry1(me);
		ageSetterOnMyself.apply(36);
		assertEquals(36, me.getAge());
	}
	
	public void setAgeOnPerson(Person person, int age) {
		person.setAge(age);
	}
}
