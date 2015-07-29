import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class WrongWordGenerator {

	private static final String dictionaryPath = "/usr/share/dict/words";
	// Using a map allows us to group the original dictionary by the first
	// letter.
	private static Map<Character, LinkedHashSet<String>> dictionary = new HashMap<Character, LinkedHashSet<String>>();
	private Set<Character> vowels = new HashSet<Character>(5);
	private boolean isVowelChanged;
	private boolean isPairCreated;

	private static WrongWordGenerator instance = null;

	public static WrongWordGenerator getInstance() {
		if (instance == null) {
			instance = new WrongWordGenerator();
		}
		return instance;
	}

	public WrongWordGenerator() {
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

	private String duplicateLetter(String word) {
		String newWord = word;
		if (!isVowelChanged) {
			int index = (int) (Math.random() * (word.length()));
			if (index == 0) {
				newWord = newWord.substring(0, 1)
						+ newWord.substring(index, newWord.length());
			} else {
				newWord = newWord.substring(0, index - 1)
						+ newWord.charAt(index - 1)
						+ newWord.substring(index - 1, newWord.length());
			}
			isPairCreated = true;
		}
		return newWord;
	}

	private String changeVowel(String word) {
		String newWord = word;
		Object[] vowelsArray = (vowels.toArray());
		int index = (int) (Math.random() * (vowelsArray.length));

		if (!isPairCreated) {
			for (Character c : newWord.toCharArray()) {
				if (vowels.contains(Character.toLowerCase(c))) {
					char replacingVowel = (Character) (vowelsArray[index]);
					while (replacingVowel == Character.toLowerCase(c)) {
						index = (int) (Math.random() * (vowelsArray.length));
						replacingVowel = (Character) (vowelsArray[index]);
					}
					newWord = newWord.replace(Character.toLowerCase(c), replacingVowel);
					isVowelChanged = true;
				}
				break;
			}
		}
		return newWord;
	}

	private String randomUpper(String word) {
		String newWord = word;
		int index = (int) (Math.random() * (newWord.length()));
		if (index == 0) {
			newWord = Character.toUpperCase(newWord.charAt(index))
					+ newWord.substring(index, newWord.length());
		} else {
			newWord = newWord.substring(0, index - 1)
					+ Character.toUpperCase(newWord.charAt(index - 1))
					+ newWord.substring(index, newWord.length());
		}
		return newWord;
	}

	private String corruptWord(String word) {
		isVowelChanged = false;
		isPairCreated = false;
		String corruptedWord = word;
		int numberModif = 1 + (int) (Math.random() * ((word.length() - 1) + 1));
		for (int i = 0; i < numberModif; i++) {
			int typeModif = (int) (Math.random() * (2));
			switch (typeModif) {
			case 0:
				corruptedWord = duplicateLetter(corruptedWord);
				break;
			case 1:
				corruptedWord = changeVowel(corruptedWord);
				break;
			default:
				break;
			}
		}
        //The case modification is separated to avoid creating new letters by
        //combining the 3 possible modifications multiple times.
		for (int i = 0; i < numberModif; i++) {
			corruptedWord = randomUpper(corruptedWord);
		}
		return corruptedWord;
	}

	public static void main(String[] args) {
		WrongWordGenerator.getInstance();
		while (true) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			int randomDictIndex = (int) (Math.random() * (26));
			char key = (char) (97 + randomDictIndex);
			int dictionarySize = dictionary.get(key).size();
			int randomDictWord = (int) (Math.random() * (dictionarySize));
			Object[] words = dictionary.get(key).toArray();
			String word = (String) words[randomDictWord];
			System.out.println(WrongWordGenerator.getInstance().corruptWord(word));
		}
	}

}

