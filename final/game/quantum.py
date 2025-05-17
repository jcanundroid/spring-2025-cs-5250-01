import math
import time

from qiskit import QuantumCircuit, transpile
from qiskit.circuit.library import GroverOperator, MCMT, ZGate

from qiskit_ibm_runtime import SamplerV2
from qiskit_ibm_runtime.fake_provider import FakeMelbourneV2

class QuantumGuesser:
	def __init__(self, bits, answer):
		# the power of Grover's quantum algorithm: quadratic runtime
		self._optimalSteps = math.floor((math.pi / 4) * ((2 ** bits) ** 0.5))

		self._bits = bits
		self._currentStep = 0
		self._buildGroverFlipperCircuit(answer)
		self._buildCompleteGroverCircuit()

	def isReady(self):
		self._currentStep += 1
		return self._currentStep >= self._optimalSteps

	def guess(self):
		# observe the state, which should be the answer
		self._completeCircuit.measure_all()

		with open('circuit-complete.txt', 'w') as circuitFile:
			print(self._completeCircuit.draw('text'), file = circuitFile)

		print('(Simulating the quantum computer\'s guess...)')

		# run the simulation
		backend = FakeMelbourneV2()
		transpiled = transpile(self._completeCircuit, backend)
		sampler = SamplerV2(backend)
		job = sampler.run([transpiled], shots = 1)
		results = job.result()[0].data.meas.get_counts()

		return int(list(results)[0], 2)

	def _buildGroverFlipperCircuit(self, answer):
		# encode the answer as a LSB bit string
		formatString = f'{{answer:{self._bits:03d}b}}'
		encodedAnswer = formatString.format(answer = answer)
		encodedAnswer = list(int(i) for i in reversed(encodedAnswer))

		# get the bit positions corresponding to bit values of zero
		zeroIndices = [i for i in range(self._bits) if encodedAnswer[i] == 0]

		circuit = QuantumCircuit(self._bits)

		# negate zeroed bits
		circuit.x(zeroIndices)

		# feed all bits to a control Z gates applied to all bits
		# that is, given the correct answer state has Z applied
		circuit.compose(MCMT(ZGate(), self._bits - 1, 1), inplace = True)

		# negate zeroed bits again back to original values
		circuit.x(zeroIndices)

		with open('circuit-blackbox.txt', 'w') as circuitFile:
			print(circuit.draw('text'), file = circuitFile)

		self._flipperCircuit = circuit

	def _buildCompleteGroverCircuit(self):
		# expand the flipper circuit to include the amplification step
		flipperAmplifierCircuit = GroverOperator(self._flipperCircuit)

		# start the final circuit with a superposition of all states:
		# a Hadamard gate applied to all zero states
		circuit = QuantumCircuit(flipperAmplifierCircuit.num_qubits)
		circuit.h(range(flipperAmplifierCircuit.num_qubits))

		# repeat the flipper amplifier circuit the optimal number of steps
		circuit.compose(flipperAmplifierCircuit.power(self._optimalSteps), inplace = True)

		self._completeCircuit = circuit
