import random
import sys

from .classical import ClassicalGuesser
from .quantum import QuantumGuesser

def main(bits):
	maxAnswer = 2 ** bits

	welcomeMessage = \
		'CS 5250 - Advanced Computer Architecture: Final Project\n' + \
		'Jeremy C. Anunwah\n\n' + \
		'Welcome to Number Guesser!\n\n' + \
		'The objective is to correctly guess my number in the fewest attempts.\n' + \
		f'This number is a random value between 1 and {maxAnswer}.\n\n' + \
		'There are three players: you (a human), a classical computer, and a quantum computer.\n' + \
		'The classical computer will use the best strategy available: a sequential search.\n' + \
		'The quantum computer will demonstrate Grover\'s algorithm.\n' + \
		'You may use whatever approach you choose, and you are given occasional hints.\n\n' + \
		'Who will find the answer fastest? Let\'s find out!\n'

	print(welcomeMessage)

	random.seed()

	answer = getAnswer(maxAnswer)

	# print(f'Answer: {answer}\n')

	classicalGuesser = ClassicalGuesser()
	quantumGuesser = QuantumGuesser(bits, answer)

	humanGetsHint = False
	humanTries = 0
	classicalTries = 0
	quantumTries = 0

	print('It is your turn.')

	while True:
		guess = getHumanGuess(maxAnswer)
		correct = guess == answer
		humanTries += 1

		if correct:
			print('Your guess is correct!')
			break
		else:
			print('Your guess is incorrect.')

			if humanGetsHint:
				hint = 'lower' if answer < guess else 'higher'
				print(f'Hint: {hint}')

			humanGetsHint = not humanGetsHint

	print('\nIt is the classical computer\'s turn.')

	while True:
		classicalTries += 1
		if classicalGuesser.guess() == answer:
			break

	print('The classic computer\'s turn is complete.')
	print('\nIt is the quantum computer\'s turn.')

	while True:
		quantumTries += 1

		if quantumGuesser.isReady():
			break

	quantumGuess = quantumGuesser.guess()
	quantumCorrect = 'correctly' if quantumGuess == answer else 'incorrectly'

	print(f'The quantum computer\'s turn is complete. The guess is {quantumGuess}\n')

	print(f'The correct answer was {answer}.')
	print(f'You guessed correctly (with hints) in {humanTries} steps.')
	print(f'The classical computer guessed correctly in {classicalTries} steps.')
	print(f'The quantum computer guessed {quantumCorrect} in {quantumTries} steps.\n')
	print('Thank you for playing! Try again with a different number of bits!\n')

def getAnswer(maxAnswer):
	return random.randrange(1, maxAnswer + 1)

def getHumanGuess(maxAnswer):
	response = None

	while response is None:
		try:
			response = int(input('Enter your guess: '))
		except ValueError:
			print('Invalid guess.')
			response = None

	return response

if len(sys.argv) != 2:
	print('Usage: python -m game [2, ..., 14]')
else:
	main(int(sys.argv[1]))
