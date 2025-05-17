import time

class ClassicalGuesser:
	def __init__(self):
		self._nextGuess = 1

	def guess(self):
		guess = self._nextGuess
		self._nextGuess += 1

		time.sleep(0.005)

		return guess
