all: server client

client:
	cd client_java && make

server:
	cd server_c && make

clean: clear

clear:
	cd client_java && make clear
