import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Spellcheck {

	private static final String dictionaryPath = "/usr/share/dict/words";
	// Using a map allows us to group the original dictionary by the first
	// letter.
	private Map<Character, LinkedHashSet<String>> dictionary = new HashMap<Character, LinkedHashSet<String>>();
	private Set<Character> vowels = new HashSet<Character>(5);
	// Indicates the number of differences between the input word and the tested
	// dictionary word.
	private int differences;
	private int minDifferences;

	// Singleton istance.
	private static Spellcheck instance = null;

	public static Spellcheck getInstance() {
		if (instance == null) {
			instance = new Spellcheck();
		}
		return instance;
	}

	// Initialize vowel set and load dictionary.
	private Spellcheck() {
		Collections.addAll(vowels, 'a', 'e', 'i', 'o', 'u');
		try {
			loadDictionary();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadDictionary() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(dictionaryPath));
		String line;

		// Use ASCII code to put each letter of the alphabet as a map key.
		for (int i = 0; i < 26; i++) {
			dictionary.put(((char) (i + 97)), new LinkedHashSet<String>());
		}

		// Fill the map values without case-sensitivity.
		while ((line = br.readLine()) != null) {
			dictionary.get(Character.toLowerCase(line.charAt(0))).add(line);
		}
		br.close();
	}

	// As no English word is using three identical letters in a row, correctSyntax replaces
	// every triplet or more by a pair.
	private String correctSyntax(String word) {
		boolean isDouble = false;
		char lastChar = ' ';
		String cleanWord = "";
		for (char c : word.toCharArray()) {
			if (Character.toLowerCase(lastChar) == Character.toLowerCase(c)	&& isDouble)
				lastChar = c;
			else if (Character.toLowerCase(lastChar) == Character.toLowerCase(c)) {
				isDouble = true;
				cleanWord += c;
				lastChar = c;
			} else {
				isDouble = false;
				cleanWord += c;
				lastChar = c;
			}
		}
		return cleanWord;
	}

	// Browse the given dictionary and keep the best match between the input
	// word and all dictionary words.
	private String findBestMatch(String word, LinkedHashSet<String> dictionary) {
		String bestMatch = null;
		int errorsMin = -1;
		for (String currentDictWord : dictionary) {
			// Any dictionary word longer than the input word suggest an error
			// case we dont deal with.
			if (currentDictWord.length() <= word.length()) {
				boolean match = matches(word, currentDictWord);
				// Replace the old bestmatch by the new if it's better. A match
				// is based on the number of differences between the two words.
				if (match == true && (errorsMin == -1 || differences < errorsMin)) {
					bestMatch = currentDictWord;
					errorsMin = differences;
					minDifferences = differences;
				}
			}
		}
		return bestMatch;
	}

	// Evaluates the differences between the two words considering every given
	// combinations of conflicts.
	public boolean matches(String word, String dictWord) {
		int wordLength = word.length();
		int dictWordLength = dictWord.length();
		// Correspond to the given error cases to treat in the exercise.
		int correctableErrors = 0;
		int pairCounter = 0;
		differences = 0;
		int i;
		char previousDictWordChar = ' ';

		for (i = 0; i < wordLength; i++) {
			// Allows to count only one difference if the character is an
			// uppercase and not a vowel nor a part of a pair.
			boolean upper = false;
			char wordChar = word.charAt(i);
			char dictWordChar;
			if (i - pairCounter < dictWordLength) {
				// Ignores the second character of a pair if pair is detected as
				// a difference.
				dictWordChar = dictWord.charAt(i - pairCounter);
			} else {
				// Allows to continue even if the dictionnary word is too small.
				dictWordChar = dictWord.charAt(dictWordLength - 1);
			}
			if (wordChar != dictWordChar) {
				// Checks separately if the character is uppercase because it
				// can be both upper and vowel or part of a pair.
				if (Character.toUpperCase(dictWordChar) == Character.toUpperCase(wordChar)) {
					correctableErrors++;
					differences++;
					upper = true;
				}
				// Checks if the character is a vowel without case
				// sensitivity.
				if (vowels.contains(Character.toLowerCase(dictWordChar))
						&& vowels.contains(Character.toLowerCase(wordChar))
						&& Character.toLowerCase(dictWordChar) 
                          != Character.toLowerCase(wordChar)) {
					if (i > 0
							&& Character.toLowerCase(wordChar) 
                              == Character.toLowerCase(word.charAt(i - 1))
							&& Character.toLowerCase(wordChar) 
                              == Character.toLowerCase(previousDictWordChar)
							&& wordLength - i 
                              != dictWordLength	- (i - pairCounter)) {
						correctableErrors++;
						differences++;
						pairCounter++;
					} else {
						correctableErrors++;
						differences++;
					}

					// Checks if the current difference is caused by a pair of
					// the same letter.
				} else if (i > 0
						&& Character.toLowerCase(wordChar) == Character.toLowerCase(word.charAt(i - 1))
						&& upper == false) {
					pairCounter++;
					if (i >= pairCounter && dictWordLength > i - pairCounter) {
						if (Character.toLowerCase(wordChar) == Character.toLowerCase(dictWord.charAt(i - pairCounter))) {
							correctableErrors++;
							differences++;
						}
					}
				} else if (upper == false) {
					// If the difference is not managed.
					differences++;
				}
			}
			previousDictWordChar = dictWordChar;
		}
		// Counts additional letters between a possible match and the actual
		// dictionary word as differences.
		if (dictWordLength >= i - pairCounter) {
			while (dictWordLength > i - pairCounter) {
				differences++;
				pairCounter--;
			}
		}
		return correctableErrors == differences;
	}

	// Reduces the complexity by using only the needed dictionary part.
	private String correct(String word) {
		// If the first letter is a vowel the matching word could be found in
		// each word of the dictionary starting with a vowel.
		if (vowels.contains(Character.toLowerCase(word.charAt(0)))) {
			int bestDifferences = -1;
			String bestMatch = null;
			for (Character c : vowels) {
				String correctedWord = findBestMatch(word, dictionary.get(c));
				// Uses the same system as in findBestMatch to keep only the
				// best match.
				if (correctedWord != null
						&& (bestDifferences == -1 || minDifferences < bestDifferences))
					bestMatch = correctedWord;
			}
			return bestMatch;
			// Otherwise the only dictionary part to consider is the one of the
			// first letter of our input word.
		} else {
			return findBestMatch(word,
					dictionary.get(Character.toLowerCase(word.charAt(0))));
		}
	}

	public static void main(String[] args) {

		while (true) {
			System.out.print(">");
			Scanner user_input = new Scanner(System.in);
			String word = user_input.next();
			System.out.println(word);

			String correctedWord = Spellcheck.getInstance().correctSyntax(word);
			correctedWord = Spellcheck.getInstance().correct(correctedWord);

			if (correctedWord == null) {
				System.out.println("NO SUGGESTIONS");
			} else
				System.out.println(correctedWord);
		}
	}

}

