package com.bw.hawksword.parser;

import java.util.ArrayList;



/**
 * A utility class to perform operation on Arrays, Lists, etc.
 * @author adyarshyam@gmail.com
 *
 */
public class Lists {

	public static <E> ArrayList<E> newArrayList( E[] elements) {
		ArrayList<E> arrayList = new ArrayList<E>(elements.length);
		for (E element : elements) {
			arrayList.add(element);
		}
		return arrayList;
	}
}
