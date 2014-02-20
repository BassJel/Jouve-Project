package com.doculibre.constellio.utils.comparators;

import java.util.Comparator;

import com.doculibre.analyzer.AccentApostropheCleaner;

public class AccentLessStringComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		String o1WoutAccents = AccentApostropheCleaner.removeAccents(o1);
		o1WoutAccents = o1WoutAccents.toLowerCase();
		String o2WoutAccents = AccentApostropheCleaner.removeAccents(o2);
		o2WoutAccents = o2WoutAccents.toLowerCase();
		return o1WoutAccents.compareTo(o2WoutAccents);
	}

}
