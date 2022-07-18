import java.io.File
import java.io.FileWriter
import java.time.LocalTime
import kotlin.math.*

fun main() {
	smartSearch(1, 511)
	parseIntoCSV()
}

val primes = listOf(
	2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101,
	103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199,
	211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307, 311, 313, 317,
	331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443,
	449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577,
	587, 593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701,
	709, 719, 727, 733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839,
	853, 857, 859, 863, 877, 881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983,
	991, 997
)
val primeWeights = primes.map { ln(it.toDouble()) }
fun getU(n: Int): List<Pair<Int, Int>> {
	val nFactors = factorise(n)
	return if (nFactors.size == 2) {
		listOf(Pair(2, nFactors.last() - 1))
	} else {
		val a = generateAllFactorSequences(n)
		println("$n ${a.size}")
		a.minByOrNull { x ->
			run {
				var weight = 0.0
				for (i in x.indices) {
					weight += (x[i] - 1) * primeWeights[i]
				}
				weight
			}
		}!!.mapIndexed { index, i -> Pair(primes[index], i - 1) }
	}
}

fun getSequence(): List<Int> {
	return File("src/main/resources/rawSequence.txt").readLines().map { it.toInt() }
}

fun smartSearch(startingN: Int = 1, endingN: Int = -1, path: String = "src/main/resources/Un.txt") {
	var log = ""
	var n = startingN
	while (n <= endingN || endingN == -1) {
		val record = "$n\t\t${beatifyIndexForm(getU(n))}"
		log += "$record\n"
		println(record.padEnd(50, ' ') + LocalTime.now())
		n++
	}
	File(path).writeText(log)
}

// Looks for successive values for Un via the use of rules and bruteforce
fun search(startingN: Int) {
	var previousNumbers = mutableListOf<Int>()
	var previousFactorNumbers = mutableListOf<Int>()
	val maxListSize = 30_000_000
	// These lists keep track of numbers factored as factoring is inefficient

	var n = startingN
	while (true) {
		// Rules
		val nFactorNumber = countFactors(n)
		if (nFactorNumber <= 2) {
			// RULE #1 | for prime n: Un = 2^{n-1}
			printWithTimestamp("$n  #1  2^${n - 1}")
			n++
			continue
		} else if (nFactorNumber == 4) {
			// RULE #2 | for n with 4 factors (1, n, a, b), Un = 2^{b-1} * 3^{a-1}  (b > a)
			// EXCEPTION | for cubes of primes this is not the case as a = b^2,
			// therefore there are other possible sequences
			val iFactors = factorise(n).drop(2)
			if (iFactors[0] * iFactors[0] * iFactors[0] != n) {
				printWithTimestamp("$n  #2  2^${iFactors[1] - 1} * 3^${iFactors[0] - 1}")
				n++
				continue
			}
		} else if (nFactorNumber == 3) {
			// RULE #3 | for n being a square of a prime with factors (1, a, n) (a is prime) the Un = 2^{a-1} * 3^{a-1}
			val iFactor = factorise(n).drop(2)[0] - 1
			printWithTimestamp("$n  #3  2^$iFactor * 3^$iFactor")
			n++
			continue
		}

		// Bruteforce
		// Checks every even number until Un is found
		var num = 0
		do {
			num += 2
			val index = previousNumbers.indexOf(num)
			var factors: Int
			if (index == -1) {
				factors = countFactors(num)
				previousNumbers.add(num)
				previousFactorNumbers.add(factors)
			} else {
				factors = previousFactorNumbers[index]
			}
			// If the number has been checked the number of factors is retrieved
			// If the number is unchecked it adds it the list
		} while (factors != n)

		printWithTimestamp("$n      $num")
		// Prints brute-forced number

		if (previousNumbers.size >= maxListSize) {
			previousNumbers = previousNumbers.takeLast(maxListSize).toMutableList()
			previousFactorNumbers = previousFactorNumbers.takeLast(maxListSize).toMutableList()
		}
		// If the size of the previousNumbers is too large
		// the first maxListSize elements are removed

		n++
	}
}

fun countFactors(n: Int): Int {
	if (n == 1) {
		return 1
	}
	var factors = 2
	// 2 representing 1 and n
	val upperBound = floor(sqrt(n.toDouble())).toInt()
	// The largest factor of n is always <= sqrt(n)
	for (i in 2..upperBound) {
		if (n % i == 0) {
			factors += if (i * i == n) 1 else 2
			// if i is the sqrt(n) it only counts as 1 factor
		}
	}
	return factors
}

fun factorise(n: Int): List<Int> {
	val factors = mutableListOf(1)
	if (n == 1) {
		return factors
	}
	factors.add(n)
	val upperBound = ceil(sqrt(n.toDouble())).toInt()
	for (i in 2..upperBound) {
		if (n % i == 0 && !factors.contains(i)) {
			factors.add(i)
			if (i * i != n) {
				factors.add(n / i)
			}
		}
	}
	return factors
}

// https://www.vogella.com/tutorials/JavaAlgorithmsPrimeFactorization/article.html
fun primeFactors(number: Int): List<Int> {
	if (number == 1) {
		return listOf(1)
	}
	var n = number
	val factors = mutableListOf<Int>()
	var i = 2
	while (i <= n / i) {
		while (n % i == 0) {
			factors.add(i)
			n /= i
		}
		i++
	}
	if (n > 1) {
		factors.add(n)
	}
	return factors
}

// Parses a list of factors into index form
// E.g. indexForm([2, 2, 2, 3, 5, 5]) => [(2, 3), (3, 1), (5, 2)]
fun indexForm(factors: List<Int>): List<Pair<Int, Int>> {
	val indexPairs = mutableListOf<Pair<Int, Int>>()
	var lastFactor = 0
	factors.forEach {
		if (it != lastFactor) {
			lastFactor = it
			indexPairs.add(Pair(it, 1))
		} else {
			indexPairs[indexPairs.size - 1] = Pair(it, indexPairs.last().second + 1)
		}
	}
	return indexPairs.toList()
}

fun beatifyIndexForm(factors: List<Pair<Int, Int>>): String {
	return factors.joinToString(" * ") { "${it.first}^${it.second}" }
}

// Produces every set of number whose product is n
// It takes the prime factors, then reduces them using the function below
// Then those new sets are further
fun generateAllFactorSequences(n: Int): List<List<Int>> {
	val factorSequences = HashSet<List<Int>>()
	val primeFactors = primeFactors(n)
	// The first set is the prime factorisation (not in index form)
	// E.g. for n=24, primeFactors(n) => [2, 2, 2, 3]

	var lastSet = mutableListOf(primeFactors)
	var nextSet = mutableListOf<List<Int>>()
	// The lastSet refers to the set which will have every subset 'reduced'
	// The nextSet is where these 'reduced' sets are stored
	// They are kept separate from factorSequences to avoid duplicate sets
	for (i in 1 until primeFactors.size - 1) {
		for (set in lastSet) {
			nextSet.addAll(multiplyOutSet(set))
			// This where sets are 'reduced'
		}

		factorSequences.addAll(lastSet.toMutableList())
		lastSet = nextSet.toMutableList()
		nextSet = mutableListOf()
		// The lastSet is moved to factorSequences
		// The nextSet is moved to lastSet to be reduced next loop
		// The nextSet is cleared
	}
	factorSequences.addAll(lastSet.toMutableList())
	factorSequences.add(listOf(n))
	// The last batch is transferred
	// [n] is added as the product of that list is n

	return factorSequences.map { x ->
		run {
			val sequence = x.toMutableList()
			sequence.sortDescending()
			sequence.toList()
		}
	}.distinct().toList()
	// Factors put in descending order and removes duplicates
}

// Given a set of numbers each number is multiplied with another to provide a new set
// E.g. 2 2 3 7 =>
//      4 3 7
//      2 6 7
//      etc...
// (It reduces the set to each set of size n-1 which multiply to the product of the original set)
fun multiplyOutSet(set: List<Int>): List<List<Int>> {
	val sets = mutableListOf<MutableList<Int>>()
	for (i in 0 until set.size - 1) {
		// Once you reach the last element all 'new' sets are duplicates
		// Hence the loop only repeats n-1 times
		val subset = set.toMutableList()
		subset.removeAt(i)
		for (j in 0 until subset.size - i) {
			val subSubset = subset.toMutableList()
			subSubset[j + i] = subSubset[j + i] * set[i]
			sets.add(subSubset)
			// Multiplies set[i] to every factor after it
			// This avoids duplicates as multiplication is commutative
		}
	}
	return sets
}

// Prints with a timestamp attached
fun printWithTimestamp(str: String) {
	println("${str.padEnd(30, ' ')}${LocalTime.now()}")
}

// THIS FUNCTION IS HORRIBLY WRITTEN - I'm not bothered
// Takes the console output of the search and parses it into a csv,
// which can then be imported to replace the values in the spreadsheet
fun parseIntoCSV(rawTextPath: String = "src/main/resources/Un.txt", path: String = "src/main/resources/un_spreadsheet.csv") {
	val lines = File(rawTextPath).readLines().map {
		it.split("\t\t").joinToString(",=") + "," + it.split("\t\t")[1]
	}
	File(path).writeText(lines.joinToString("\n"))
}
