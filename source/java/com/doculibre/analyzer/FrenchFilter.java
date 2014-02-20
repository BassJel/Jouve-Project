package com.doculibre.analyzer;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;

public class FrenchFilter extends TokenFilter {


	private Set exclusions = null;


	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
	private final KeywordAttribute keywordAttr = addAttribute(KeywordAttribute.class);


	public FrenchFilter(TokenStream in) {
		super(in);

	}


	/**
	 * @return Returns true for the next token in the stream, or false at EOS
	 */
	@Override
	public boolean incrementToken() throws IOException {
		if (input.incrementToken()) {
			String term = termAtt.toString();

			// Check the exclusion table
			if (!keywordAttr.isKeyword() && (exclusions == null || !exclusions.contains(term))) {
				String s = AccentApostropheCleaner.removeAccents(term);

				s = AccentApostropheCleaner.removePluriel(s);
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

}
