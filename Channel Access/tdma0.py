#!/usr/bin/python
import RPi.GPIO as GPIO
import time
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
BIT_TIME= 0.05
SLOT_TIME= BIT_TIME*(NUM_BITS+2)

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

#Send a byte to designated pin number
def sendByte(byte2Send, pinNumber):
	for bit in byte2Send:
		if bit=='1':
			GPIO.output(pinNumber, GPIO.HIGH)
		if bit=='0':
			GPIO.output(pinNumber, GPIO.LOW)
		time.sleep(BIT_TIME)

#Sender One Implementation		
def senderOne(message, pinNumber):
	strBytes = string2bits(message)
	#Sending Time Slot
	while True:
		for byte in strBytes: 
			t1 = time.time()
			sendByte(byte,pinNumber)
			GPIO.output(pinNumber, GPIO.LOW)
			#Waiting Time Slot
			while time.time() < (t1+ 2*SLOT_TIME):
				pass
				
#Sender Two Implementation	
def senderTwo(message, pinNumber):
	strBytes = string2bits(message)
	while True:
		#Sending Time Slot
		for byte in strBytes:
			t1 = time.time()
			while time.time() <= (t1+ SLOT_TIME):
				pass
			sendByte(byte,pinNumber)
			GPIO.output(pinNumber, GPIO.LOW)
			while time.time() < (t1 + 2*SLOT_TIME):
				pass

#Receiver Implementation
def receiver():
	stringBits = []
	while True:
		t1= time.time()
		receivedBits = ''
		#receive byte
		for bitsCount in range(NUM_BITS):
			value = not GPIO.input(17)
			if value:
				receivedBits = receivedBits +'1'
			else:
				receivedBits = receivedBits +'0'
			time.sleep(BIT_TIME)			
		stringBits.append(receivedBits)
		print bits2string(stringBits)
		#waiting gap time
		while time.time() < (t1 + SLOT_TIME):
			pass		
			
			
if __name__ == '__main__':
	#Clear GPIO pins
	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	#Create Sender & Receiver Processes
	p1 = Process(target=senderOne, args= ('HELLO FROM SENDER 1!', 23))
	p2 = Process(target=senderTwo, args= ('hello from sender 2!', 24))
	p3 = Process(target=receiver)
	#Start The process
	p1.start()
	p2.start()
	#Delay the start of the receiver process by half bit time 
	time.sleep(BIT_TIME/2)
	p3.start()
	#Join the processes 
	p1.join()
	p2.join()
	p3.join()	
