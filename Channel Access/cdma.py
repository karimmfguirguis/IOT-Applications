#Team 5: George Eskandar - Karim Guirguis - Kirolos Abdou - Ismail Mehrez

#!/usr/bin/python
import RPi.GPIO as GPIO
import time
import serial
from multiprocessing import Process

#----------------------------------#
#		  GPIO Setup			   #
#----------------------------------#

GPIO.setmode(GPIO.BCM)		# UseBCM numbering
GPIO.setwarnings(False)		# Disable Warnings
GPIO.setup(23, GPIO.OUT)	#Sender 1
GPIO.setup(24, GPIO.OUT)	#Sender 2
GPIO.setup(17, GPIO.IN)		#Receiver

#----------------------------------#
#		  Global Variables		   #
#----------------------------------#
NUM_BITS = 8
CHIP_TIME= 0.1
BIT_TIME= 3*(CHIP_TIME)


#----------------------------------#
#			Functions			   #
#----------------------------------#

#Convert from string to ASCII 
def string2bits(str	= ''):
	global NUM_BITS
	return [bin(ord(chr))[2:].zfill(NUM_BITS) for chr in str]

#Convert from ASCII to string
def bits2string(bitString=None):
	return ''.join([chr(int(bits, 2)) for bits in bitString])

#Send a chipping sequence to designated pin number (only called from sender 1)
def sendSeqOne(chipSeq, pinNumber):
	for chip in chipSeq:
		t1 = time.time()
		if chip=='1':
			GPIO.output(pinNumber, GPIO.HIGH)
		if chip=='0':
			GPIO.output(pinNumber, GPIO.LOW)
		#wait till the next chip slot to sync with the other sender	
		while time.time() < (t1 + CHIP_TIME):
				pass
				
#Send a chipping sequence to designated pin number (only called from sender 2)		
def sendSeqTwo(chipSeq, pinNumber):
	for chip in chipSeq:
		t1 = time.time()
		if chip=='1':
			GPIO.output(pinNumber, GPIO.HIGH)
		if chip=='0':
			GPIO.output(pinNumber, GPIO.LOW)
		#wait till the next chip slot to sync with the other sender
		while time.time() < (t1 + CHIP_TIME):
				pass
				
#read the digital value from /dev/ttyACM0 
def readValue():
	ser= serial.Serial(port='/dev/ttyACM0', baudrate=115200)
	# Receives analog readings from Arduino at 100 Hz
	line = ser.readline()
	line = line.rstrip()
	ser.close()
	#map the received value to -2,0,+2
	if  (int(line) > 0) and (int(line) < 300):
		return 2
	elif  (int(line) >= 300) and (int(line) < 800):
		return 0
	else:
		return -2		
		
#Sender One Implementation		
def senderOne(message, pinNumber):
	chipSeq = "11"
	notchipSeq = "00"
	strBytes = string2bits(message)
	#Sending Time Slot
	while True:
		for byte in strBytes: 
			for bit in byte:
				#bit starting time
				t1 = time.time()		
				if bit == '1':
					sendSeqOne(chipSeq, 23)
				else:
					sendSeqOne(notchipSeq, 23)
				GPIO.output(pinNumber, GPIO.LOW)
				#Wait till next bit time Slot
				while time.time() < (t1+ BIT_TIME):
					pass
				
#Sender Two Implementation	
def senderTwo(message, pinNumber):
	chipSeq = "10"
	notchipSeq = "01"
	strBytes = string2bits(message)
	#Sending Time Slot
	while True:
		for byte in strBytes: 
			for bit in byte:
				#bit starting time
				t1 = time.time()		
				if bit == '1':
					sendSeqTwo(chipSeq, 24)
				else:
					sendSeqTwo(notchipSeq, 24)
				GPIO.output(pinNumber, GPIO.LOW)
				#Wait till next bit time Slot
				while time.time() < (t1+ BIT_TIME):
					pass
				
#Receiver Implementation
def receiver():
	#list of ascii codes of the characters
	messageOne = []
	messageTwo = []
	
	while True:
		#ascii code of the characters
		charOne = ''
		charTwo = ''
		#receive byte from each sender
		for bitsCount in range(NUM_BITS):
			#bit starting time
			t1= time.time()
			chip1 = readValue()				#readValue returns a mapped int value -2/0/2
			#wait till next chip slot
			while time.time() < (t1 + CHIP_TIME):
				pass
			chip2 = readValue()				#readValue returns a mapped int value -2/0/2
			#wait till next chip slot
			while time.time() < (t1 + (2*CHIP_TIME)):
				pass
			
			#calculate the dot product of the received seq and the chipping sequences
			dotProduct1 = (chip1*1) + (chip2*1)
			dotProduct2 = (chip1*1) + (chip2*-1)
			if dotProduct1 > 0:
				charOne += '1'
			else:
				charOne += '0'
			if dotProduct2 > 0:
				charTwo += '1'
			else:
				charTwo += '0'
			
			#Wait till next bit time Slot
			while time.time() < (t1 + BIT_TIME):
				pass		
		messageOne.append(charOne)
		messageTwo.append(charTwo)
		
		print bits2string(messageOne)
		print bits2string(messageTwo)
		#check end of message '!'
		if charOne == '00100001':
			messageOne = []
		if charTwo == '00100001':	
			messageTwo = []
			
			
if __name__ == '__main__':
	#Clear GPIO pins
	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	time.sleep(1)
	#Create Sender & Receiver Processes
	p1 = Process(target=senderOne, args= ('HELLO FROM SENDER 1!', 23))
	p2 = Process(target=senderTwo, args= ('hello from sender 2!', 24))
	p3 = Process(target=receiver)
	#Start The process
	tRec = time.time()
	p1.start()
	p2.start()
	#Delay the start of the receiver process by half chip time 
	while time.time() < (tRec + CHIP_TIME/2):
		pass
	p3.start()
	#Join the processes 
	p1.join()
	p2.join()
	p3.join()	
