#Team 5: George Eskandar - Karim Guirguis - Kirolos Abdou - Ismail Mehrez

#!/usr/bin/python
import serial
import RPi.GPIO as GPIO
import time
from multiprocessing import Process


# Use BCM numbering
GPIO.setmode(GPIO.BCM)
GPIO.setup(23, GPIO.OUT)
GPIO.setup(24, GPIO.OUT)
TIME= 0.1

#sender function
def sender():
    while True:
		#record the starting time of sending 
        t1 = time.time()
        GPIO.output(23, GPIO.LOW)
        GPIO.output(24, GPIO.LOW)
        print "OFF OFF: "
		#wait till next time slot
        while time.time() < (t1 + TIME):
            pass

        t1 = time.time()
        GPIO.output(23, GPIO.HIGH)
        GPIO.output(24, GPIO.LOW)
        print "ON OFF: "
        while time.time() < (t1 + TIME):
            pass

        t1 = time.time()
        GPIO.output(23, GPIO.LOW)
        GPIO.output(24, GPIO.HIGH)
        print "OFF ON: "
        while time.time() < (t1 + TIME):
            pass

        t1 = time.time()
        GPIO.output(23, GPIO.HIGH)
        GPIO.output(24, GPIO.HIGH)
        print "ON ON: "
        while time.time() < (t1 + TIME):
            pass


def receiver():
	line= ''
	while True:
		ser = serial.Serial(port='/dev/ttyACM0', baudrate=115200)
		t1 = time.time()
		# Receives analog readings from Arduino at 100 Hz
		line = ser.readline()
		line = line.rstrip()
		print line
		ser.close()
		while time.time() < (t1 + TIME):
			 pass
	



if __name__ == '__main__':
	# Clear GPIO pins
	GPIO.output(23, GPIO.LOW)
	GPIO.output(24, GPIO.LOW)
	# Create Sender & Receiver Processes
	p1 = Process(target=sender)
	p2 = Process(target=receiver)
	# Start The process
	p1.start()
	# Delay the start of the receiver process by half bit time
	time.sleep(TIME/2)
	p2.start()
	# Join the processes
	p1.join()
	p2.join()

