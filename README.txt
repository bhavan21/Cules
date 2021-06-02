Libraries used:
1) MySql
2) ldap,
3) socket required to run the server.
android app can directly opened in the android studios and build.
make file to run server is not requried. To run the server just run the server.py file.
Ip address is hardcoded in android...it should be changed to server Ip address , port number for making it working
Client is Targerian (Android app)

For MySql , we've used our own password and login_id . so it has to be changed(keys.py file under function connect()) if one needs to get the server working.
And Moreover one has to create main sql database on sql server  and import the main.sql file provided in the server file.



