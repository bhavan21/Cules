# Import modules

import socket               
import thread
import ldap
import json
import MySQLdb
import images
socket_list=[]

#sending real time messages to the people in chat group
def send_to_activelist(msg,list,excep):
	for user in socket_list:
		if user['id']!=excep:									#condition says that send the message to all people except himself
			if list.count((user['id'],))==1:					#condition checks if he is in the list
				for conn in user['sockets']:					#checking if it is active or not
					try:
						conn.send(msg+"\n")						#sending the message
					except:
						user['sockets'].remove(conn)			#message not reached implies he is inactive so remove the sockets
						if  len(user['sockets'])==0:
							socket_list.remove(user)			#remove the user from socket_list
	print socket_list

# {"username":"bhavan21","password":"bhavan"}
def login_check( conn,a,b):
	cur = conn.cursor()
	ldap_server="cs252lab.cse.iitb.ac.in"
	base_dn = "dc=cs252lab,dc=cse,dc=iitb,dc=ac,dc=in"
	arbit_dn = "cn="+a+","+base_dn
	connect = ldap.open(ldap_server)
	try:
		#if authentication successful, get the full user data
		connect.bind_s(arbit_dn,b)
		element={}
		element['status']=True
		try:
			rows_count=cur.execute( "SELECT id FROM user_details WHERE username=%s AND password=%s ;", (a,b))
			id=(cur.fetchone()[0])
			element['id']=id
			user=json.loads(fetch_profile(conn,id))
			element['name']=user['firstname']+" "+user['lastname']
			#return "True"
		except Exception as e:
			cur.execute( "INSERT INTO user_details (firstname,lastname,age,username,password) VALUES(%s,%s,%s,%s,%s);",(a,"SR4",18,a,b))
			conn.commit()
			rows_count=cur.execute( "SELECT id FROM user_details WHERE username=%s AND password=%s ;", (a,b))
			id=(cur.fetchone()[0])
			element['id']=id
			user=json.loads(fetch_profile(conn,id))
			element['name']=user['firstname']+" "+user['lastname']
		json_data=json.dumps(element)
		connect.unbind_s()
		return json_data
		#result = connect.search_s(base_dn,ldap.SCOPE_SUBTREE,search_filter)
		# return all user data results
		#connect.unbindw_s()
		#print result
	except ldap.LDAPError:
		connect.unbind_s()
		print "authentication error"
		rows_count=cur.execute( "SELECT id FROM user_details WHERE username=%s AND password=%s ;", (a,b))
		element={}
		if rows_count==0:
			element['status']=False
			element['id']=0
		else:
			element['status']=True
			id=(cur.fetchone()[0])
			element['id']=id
			user=json.loads(fetch_profile(conn,id))
			element['name']=user['firstname']+" "+user['lastname']
		json_data=json.dumps(element)
		return json_data

#storing messages in the server and sending the message
def insert_message(conn,id,chatid,message):
	cur = conn.cursor()
	# try:
	cur.execute( "INSERT INTO messages (chat_id,sender,message) VALUES(%s,%s,%s);",(chatid,id,message))
	conn.commit()
	cur.execute( "UPDATE chat_details SET latest_msg=%s,sender=%s,time_of_latest_msg=NOW() WHERE chat_id=%s;",(message,id,chatid))
	conn.commit()
	cur.execute("SELECT NOW() as updated_time")
	output={}
	output['time_of_msg']=str(cur.fetchone()[0])
	output['status']="True"
	json_data=json.dumps(output)
	X={}
	X['key']="input_msg"
	X['sender']=id
	user=json.loads(fetch_profile(conn,id))
	X['sender_name']=user['firstname']+" "+user['lastname']
	X['chatid']=chatid
	chat=json.loads(fetch_chat_detail(conn,chatid))
	if chat['is_group']==1:
		X['name']=chat['name']
	if chat['is_group']==0:
		X['name']=X['sender_name']
	X['message']=message
	X['time_of_msg']=output['time_of_msg']
	cur.execute("SELECT id FROM user_chatid_relations WHERE chatid=%s",(chatid,))
	list=cur.fetchall()
	print list.count((2,))
	# socket_list[0]['sockets'][0].send(json.dumps(X)+"\n")
	send_to_activelist(json.dumps(X),list,id)
	return json_data
	# except:
	# 	return "False"

#restring the new user
def register(conn,firstname,lastname,age,username,password):
	cur = conn.cursor()
	try:
		cur.execute( "INSERT INTO user_details (firstname,lastname,age,username,password) VALUES(%s,%s,%s,%s,%s);",(firstname,lastname,age,username,password))
		conn.commit()
		return "True"
	except Exception as e:
		return "False"
def search_people(conn,id,search_word):
	chat_id=-1
	cur = conn.cursor()
	cur.execute( "SELECT id,firstname,lastname FROM user_details WHERE firstname LIKE %s OR lastname LIKE %s;",(search_word+"%",search_word+"%"))
	data = []
	list=cur.fetchall()
	for user,firstname,lastname in list:
		cur.execute
		element={}
		element['id']=user
		element['firstname']=firstname
		element['lastname']=lastname
		element['is_group']=0
		element['status'],element['chatid']=friendship_status(conn,id,user)
		data.append(element)
	output={}
	output['persons_list']=data
	json_data=json.dumps(output);
	return json_data
#checking the friendship status
def friendship_status(conn,id,user):
	chat_id=0
	status=0
	cur = conn.cursor()
	rows_count1=cur.execute("SELECT * FROM friends WHERE user1=%s AND user2=%s;",(id,user))   #checking the friend requests list
	rows_count2=cur.execute("SELECT chat_id FROM friends WHERE user1=%s AND user2=%s;",(user,id)) #checking the friends list
	list=cur.fetchall()
	if (rows_count1==0 and rows_count2==0):		#Not Friends:-(
		status=1
	if rows_count1==1 and rows_count2==0:	#Fetcher sent friend request
		status=2
	if rows_count1==0 and rows_count2==1:		#Fetcher has to accept request
		status=3
	if rows_count1==1 and rows_count2==1:		#Friends
		status=4
		if(len(list[0])!=0):
			chat_id=list[0][0]
			print chat_id
	return status,chat_id

#sending the friend request
def friend_request(conn,id,other_id):
	cur = conn.cursor()
	try:
		cur.execute( "INSERT INTO friends (user1,user2) VALUES(%s,%s);",(id,other_id))  #sending to the other person that he wants to be friends and in his view he is something like a friend but can't send messages
		conn.commit()
		return "True"
	except Exception as e:
		return "False"

#getting friends list of the user
def friend_list(conn,id):
	cur = conn.cursor()
	cur.execute("SELECT A.user1 FROM friends A INNER JOIN friends B WHERE A.user2=B.user1 AND A.user1=B.user2 AND A.user2=%s;",(id)) #checking if they are friends 
	list_ids=cur.fetchall()                             #list of id's of the friends
	data = []
	for user in list_ids:
		cur.execute( "SELECT id,firstname,lastname,age,username FROM user_details WHERE id=%s;",(user))  #fetching the details of the friends
		for user_id,firstname,lastname,age,username in cur.fetchall():
			element={}
			element['id']=user_id
			element['firstname']=firstname
			element['lastname']=lastname
			element['username']=username
			element['age']=age
			data.append(element)													#adding the data into element
	json_data=json.dumps(data);
	return json_data

#friends request list of the given user
def friend_request_list(conn,id):
	cur = conn.cursor()
	cur.execute("SELECT user1 FROM friends WHERE user2=%s;",(id))			#list of friends and friend requests for given id
	list_ids=cur.fetchall()

	print str(list_ids)
	data = []
	for user in list_ids:
		rows_count= cur.execute("SELECT * FROM friends WHERE user1=%s AND user2=%s; ",(id,user))   #filtering out friends from above list
		print rows_count,id,user
		if rows_count==0:
			cur.execute( "SELECT id,firstname,lastname,age,username FROM user_details WHERE id=%s;",(user)) #if the person is not in the friend request list fetch data
			for user_id,firstname,lastname,age,username in cur.fetchall():
				element={}
				element['id']=user_id
				element['firstname']=firstname
				element['lastname']=lastname
				element['username']=username
				element['age']=age
				data.append(element)
	json_data=json.dumps(data);
	return json_data

#accepting the friends request
def accept_request(conn,id,other_id):
	cur = conn.cursor()
	try:
		chatid=cur.execute( "INSERT INTO chat_details (is_group) VALUES(%s);",("0"))      
		l=cur.lastrowid
		print l
		cur.execute( "INSERT INTO friends (user1,user2,chat_id) VALUES(%s,%s,%s);",(id,other_id,l))
		cur.execute("UPDATE friends SET chat_id=%s WHERE user1=%s AND user2=%s;",(l,other_id,id))
		cur.execute("INSERT INTO user_chatid_relations (id,chatid) VALUES(%s,%s);",(id,l))
		cur.execute("INSERT INTO user_chatid_relations (id,chatid) VALUES(%s,%s);",(other_id,l))
		conn.commit()
		element={}
		element['is_true']="True"
		element['chat_id']=l
		return json.dumps(element)
	except Exception as e:
		return "False"


def fetch_all_chats(conn,id,time,c):
	cur = conn.cursor()
	cur.execute("SELECT chatid FROM user_chatid_relations WHERE id=%s; ",(id,))
	list_ids=cur.fetchall()
	data = []
	for chatid in list_ids:
		cur.execute( "SELECT name,is_group,latest_msg,sender,time_of_latest_msg FROM chat_details WHERE chat_id=%s AND time_of_latest_msg > CONVERT(%s,DATETIME);",(chatid,time)) #getting the chat group name and knowing wheather it is a group chat or personal chat
		list=cur.fetchall()
		for name,is_group,latest_msg,sender,time_of_latest_msg in list:
			element={}
			element['chatid']=chatid[0]
			cur.execute( "SELECT user2 FROM friends WHERE user1=%s AND chat_id=%s;",(id,chatid))
			if is_group==0:
				user=json.loads(fetch_profile(conn,cur.fetchone()[0]))
				element['name']=user['firstname']+" "+user['lastname']
			else:
				element['name']=name
			element['is_group']=is_group
			element['latest_msg']=latest_msg
			element['sender']=sender
			element['time_of_latest_msg']=str(time_of_latest_msg)
			user=json.loads(fetch_profile(conn,sender))
			element['sender_name']=user['firstname']+" "+user['lastname']
			data.append(element)
	cur.execute("SELECT NOW() as updated_time")
	output={}
	output['up_time']=str(cur.fetchone()[0])
	output['chatlist']=data
	json_data=json.dumps(output);
	return json_data

#Recieving the message in the chat
#assumed user is member of chat
def fetch_a_chat(conn,id,chatid,time):	
	up_time=""
	cur = conn.cursor()
	cur.execute("SELECT message,sender,time_of_msg FROM messages WHERE chat_id=%s AND time_of_msg >CONVERT(%s,DATETIME); ",(chatid,time)) #getting the message ,sender and time of sending
	data = []
	for message,sender,time_of_msg in cur.fetchall():
		element={}
		element['message']=message
		element['sender']=sender
		element['time_of_msg']=str(time_of_msg)
		user=json.loads(fetch_profile(conn,sender))										#fetching the profile of the sender
		element['sender_name']=user['firstname']+" "+user['lastname']					#sender is printed on the top of his message
		data.append(element)
	cur.execute("SELECT NOW() as updated_time")											#just to take the updated messages but not all messages
	output={}
	output['up_time']=str(cur.fetchone()[0])
	output['messages']=data 															#adding the data into output
	json_data=json.dumps(output)														#taking data into json format
	print json_data
	return json_data

#getting the user profile
def fetch_profile(conn,id):
	cur = conn.cursor()
	cur.execute("SELECT firstname,lastname,age FROM user_details WHERE id=%s;",(id,))    #getting the user name and age
	for firstname,lastname,age in cur.fetchall():										 	
		element={}
		element['firstname']=firstname
		element['lastname']=lastname
		element['age']=age																 #adding the data into element
		json_data=json.dumps(element);													 #taking data into json format
		print json_data
		return json_data
#getting the chat group details
def fetch_chat_detail(conn,chatid):
	cur = conn.cursor()
	cur.execute("SELECT name,is_group FROM chat_details WHERE chat_id=%s;",(chatid,))   #getting the chat group name and knowing wheather it is a group chat or personal chat
	for name,is_group in cur.fetchall():
		element={}
		element['name']=name
		element['is_group']=is_group													#adding the data into element
		json_data=json.dumps(element);													#taking data into json format
		print json_data
		return json_data

def search_for_datas(conn,id,last_updated):
	up_time=""
	cur = conn.cursor()
	cur.execute("SELECT chatid FROM user_chatid_relations WHERE id=%s; ",(id,))
	list_ids=cur.fetchall()
	data = []
	for chatid in list_ids:
		cur.execute( "SELECT name,is_group,latest_msg,sender,time_of_latest_msg,NOW() as updated_time FROM chat_details WHERE chat_id=%s AND time_of_latest_msg>CONVERT(%s,DATETIME);",(chatid,last_updated))
		for name,is_group,latest_msg,sender,time_of_latest_msg,updated_time in cur.fetchall():
			element={}
			element['chatid']=chatid
			element['name']=name
			element['is_group']=is_group
			element['latest_msg']=latest_msg
			element['sender']=sender
			element['time_of_latest_msg']=str(time_of_latest_msg)
			data.append(element)
			up_time=str(updated_time)
	json_data=json.dumps(data);
	return json_data,up_time
