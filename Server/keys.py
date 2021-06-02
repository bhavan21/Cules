import socket               # Import socket module
import thread
import json
import MySQLdb
import functions

def connect():
	hostname = 'localhost'
	username = 'root'
	password = 'password'
	database = 'main'
	myConnection = MySQLdb.connect( host=hostname, user=username, passwd=password, db=database )
	return myConnection



def process_input(msg,c):
	data=json.loads(msg)
	out=""
	print data['key']
	#logging into the app
	if data['key']=="login":
		# {"key":"login","username":"lakshman","password":"lakshman@157"}
		myConnection=connect()
		id=functions.login_check( myConnection,data['username'],data['password'])
		myConnection.close()
		print id
		out=id+"\n"
	#sending the message and storing in the database
	if data['key']=="insert":
		# {"key":"insert","id":"2","chatid":"1","message":"Hi "}
		myConnection=connect()
		a=functions.insert_message(myConnection,data['id'],data['chatid'],data['message'])
		myConnection.close()
		out=a+"\n"
	#registring to the app 
	if data['key']=="register":
		# {"key":"register","firstname":"Lakshman","lastname":"Kalidindi","age":"19","username":"lakshman","password":"lakshman@157"}
		myConnection=connect()
		a=functions.register(myConnection,data['firstname'],data['lastname'],data['age'],data['username'],data['password'])
		myConnection.close()
		out=a+"\n"
	#searching for the people. Shows the people who are regstered only
	if data['key']=="search":
		# {"key":"search","id":"1","search_word":""}
		myConnection=connect()
		a=functions.search_people(myConnection,data['id'],data['search_word'])
		myConnection.close()
		out=a+"\n"
	if data['key']=="last_seen":
		# {"key":"last_seen","id":"1","timestamp":"2017-04-15 10:34:35"}
		myConnection=connect()
		a=functions.update_last_seen(myConnection,data['id'],data['timestamp'])
		myConnection.close()
		out=a+"\n"
	#sending the friend request
	if data['key']=="friend_request":
		# {"key":"friend_request","id":"20","other_id":"1"}
		myConnection=connect()
		a=functions.friend_request(myConnection,data['id'],data['other_id'])
		myConnection.close()
		out=a+"\n"
	#fetchng the friends list
	if data['key']=="friend_list":
		# {"key":"friend_list","id":"1"}
		myConnection=connect()
		a=functions.friend_list(myConnection,data['id'])
		myConnection.close()
		out=a+"\n"
	#fetching the friends request list
	if data['key']=="friend_request_list":
		# {"key":"friend_request_list","id":"5"}
		myConnection=connect()
		a=functions.friend_request_list(myConnection,data['id'])
		myConnection.close()
		out=a+"\n"
	#accepting the friend request
	if data['key']=="accept_request":
	# {"key":"accept_request","id":"19","other_id":"1"}
		myConnection=connect()
		a=functions.accept_request(myConnection,data['id'],data['other_id'])
		myConnection.close()
		out=a+"\n"
	#fetch messages from all chats once logged in
	if data['key']=="fetch_all_chats":
		# {"key":"fetch_all_chats","id":"1","threshhold":"2017-04-10 14:56:46"}
		myConnection=connect()
		a=functions.fetch_all_chats(myConnection,data['id'],data['threshhold'])
		myConnection.close()
		print a
		out=a+"\n"
	#fetch messages from a chat
	if data['key']=="fetch_a_chat":
		# {"key":"fetch_a_chat","id":"1","chatid":"1","threshhold":"2017-04-10 14:56:46"}
		myConnection=connect()
		a=functions.fetch_a_chat(myConnection,data['id'],data['chatid'],data['threshhold'])
		myConnection.close()
		out=a+"\n"
	#get the profile of a person
	if data['key']=="fetch_profile":
		# {"key":"fetch_profile","id":"5"}
		 # {"key":"chat_details","chatid":"1","is_group":0,"latest_msg":"Hi Bhavan","sender":"akhil","time_of_latest_msg":"2017-04-16 07:53:35"}
		myConnection=connect()
		a=functions.fetch_profile(myConnection,data['id'])
		myConnection.close()
		out=a+"\n"
		# {"key":"background","id":2}
	
	if data['key']=="background":
		id=data['id']
		found=False
		# print socket_list[id]
		# if socket_list[id]==0:
		for user in functions.socket_list:
			if user['id']==id:
				found=True
				user['sockets'].append(c)
		if not found:
			element={}
			element['id']=id
			element['sockets']=[]
			element['sockets'].append(c)
			functions.socket_list.append(element)
		# else:
		# 	socket_list[id].append(c)
		print functions.socket_list
		out="yo"
	return out

def search_for_datas(id,last_updated):
	return functions.search_for_datas(myConnection,id,last_updated)