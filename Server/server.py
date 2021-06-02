#!/usr/bin/python           # This is server.py file

import os
import time
import socket               # Import socket module
import thread
import json
import MySQLdb
import keys
import select


def new_client_conn(c,addr):
	# try:
	while True:
		msg = c.recv(2024)							#client recieving the message
		print ("Client says:"+msg)					#printing the message
		output=keys.process_input(msg,c)			
		if(output=="yo"):
			break
		c.send(output)
	# except:
	# 	print "connection closed!"
	# 	c.close()    # Close the connection

def new_server_conn(c,addr):
	id=1;
	last_updated="2017-04-11 21:09:46"
	try:
		while True:
			output,last_updated=keys.search_for_datas(id,last_updated)
			print last_updated
			if output!="":
				c.send(output+"\n")
			time.sleep(3)						#waiting for sometime before search
			# msg = c.recv(2024)IF
			# print (msg)
	except:
		c.close()    # Close the connection

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)         # Create a socket object
host = "lakshman"#socket.gethostname() # Get local machine name
port = 1997        # Reserve a port for your service.
s.bind((host, port))        # Bind to the port
print host
s.listen(5)                 # Now wait for client connection.
while True:
	# print s
	# print s.fileno()
	# print socket.fromfd(s.fileno(),socket.AF_INET, socket.SOCK_STREAM)
	c, addr = s.accept()     # Establish connection with client.
	print 'Got connection from', addr
	try:
		thread.start_new_thread( new_client_conn, (c,addr) )   #starts a new thread for every connection that adds
		# thread.start_new_thread( new_server_conn, (c,addr) )
	except:
		print "Error: unable to start thread"
                 

 # https://github.com/codepath/android_guides/wiki/Sending-and-Receiving-Data-with-Sockets
 # http://android-er.blogspot.in/2011/01/simple-communication-using.html
 # http://stackoverflow.com/questions/38095293/android-storing-socket-io-object-for-multiple-activities
 # http://stackoverflow.com/questions/2765636/sockets-threads-and-services-in-android-how-to-make-them-work-together
 # http://stackoverflow.com/questions/21194762/what-is-the-different-between-handler-runnable-and-threads

 