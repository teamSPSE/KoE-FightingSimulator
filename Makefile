all: server client

client:
	cd client_java && mingw32-make

server:
	cd server_c && mingw32-make

clean: clear

clear:
	cd client_java && mingw32-make clear
