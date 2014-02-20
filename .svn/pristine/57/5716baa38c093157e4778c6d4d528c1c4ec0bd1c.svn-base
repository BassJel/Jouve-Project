package com.doculibre.analyzer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

public class ApostropheFilter extends TokenFilter {


	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);


	private static List<String> aposs = Arrays.asList(new String[] { "l'", "d'", "m'", "t'", "qu'",
			"n'", "s'", "j'", "l’", "l’", "m’", "t’", "qu’", "n’", "s’", "j’", "c'", "c’" });


	public ApostropheFilter(TokenStream in) {
		super(in);
	}

	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			String term = termAtt.toString();

			// Check the exclusion table
			if (!keywordAttr.isKeyword()) {
				String s = removeApostrophe(term);
				// If not stemmed, don't waste the time adjusting the token.
				if ((s != null) && !s.equals(term))
					termAtt.setEmpty().append(s);
			}
			return true;
		}
		else {
			return false;
		}
	}


	public final static String removeApostrophe(String text) {

		for (String apos : aposs) {
			if (text.contains(apos)) {
				text = text.replaceAll(apos, "");
			}
		}
		return text;
	}
}
