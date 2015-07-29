To compile both files you have to use following command in the terminal:
"javac Spellcheck.java WrongWordGenerator.java"

And then to pipe the WrongWordGenerator output in Spellcheck input and launch them :

"java WrongWordGenerator | java Spellcheck"


Time complexity :

The complexity maximal of the Spellcheck program is O(5n / 26) where n is the length of the dictionary.
This is because the dictionary is split in 26 parts (1/letter) => O(n/26) and if the word is starting by a vowel the algorithm will browse each vowel's sub dictionary => O(5n/26).
The algorithm is under O(n).

