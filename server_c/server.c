#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <stdlib.h>
#include <pthread.h>
#include <time.h>
#include <errno.h>
#include <arpa/inet.h>


#include "user.h"
#include "lobby.h"
#include "consts.h"
#include "game.h"
#include "logger.h"
#include "usefc.h"



int MAX_USERS = MAX_USERSC;
users *theusers;
lobby *thelobby;
games *thegames;
logger *thelogger;

pthread_rwlock_t lock;

void login(int socket, char *name) {
    int i;
    if (theusers -> user_count == MAX_USERS) {
        printf("Couldn't login user: %s. Maximum of logged in players reached.\n", name);
        printf("Sent message: logi-nackfull\n");
		send_message(socket, "logi-nackfull\n", &thelogger);
        return;
    }
	if(user_get_user_by_name(theusers, name)){
        printf("Username: %s already taken.\n", name);
        printf("Sent message: logi-nackname\n");
		send_message(socket, "logi-nackname\n", &thelogger);
        return;        
    }
	user_add(&theusers, name, socket, &thelogger);

    printf("User :%s logged in.\n", name);
    printf("Sent message: logi-ack\n");
	send_message(socket, "logi-ack\n", &thelogger);
}

void logout(int socket) {
    user_remove(&theusers, &thelobby, socket, &thelogger);
}

void want_play(users *theusers, lobby **thelobby, games **thegames, int socket, logger **thelogger) {
	user *my_user = NULL;
	user *second_user = NULL;
	lobby_add_player(thelobby, socket);

	if (((*thelobby) -> size) >= 2) {
		int socket_ID_1 = socket;
		int socket_ID_2;
		do {
			socket_ID_2 = (*thelobby) -> socket_IDs[rand() % ((*thelobby) -> size)];
		} 
		while(socket_ID_2 == socket_ID_1);								

		lobby_remove_player(thelobby, socket_ID_1);
		lobby_remove_player(thelobby, socket_ID_2);								
		printf("in\n");
		char message_1[100] = "game-started";
		char message_2[100] = "game-started";
		char *now_playing = "test";												
printf("in2 usrs:%d\n",theusers->user_count);
		my_user = user_get_user_by_socket_ID(theusers, socket_ID_1);
		second_user = user_get_user_by_socket_ID(theusers, socket_ID_2);
printf("in3, myusr:%d, sec_usr:%d\n", my_user->socket, second_user->socket);
		game_add(thegames, my_user -> name, second_user -> name, now_playing);
		printf("in4\n");
		send_message(socket_ID_1, &message_1[0], thelogger);
		send_message(socket_ID_2, &message_2[0], thelogger);
	}
	return;
}

int parse_msg(int socket, char *msg) {
    long int type;
    char *name, *room, t[2], *place, *x, *y;
    t[0] = msg[0];
    t[1] = msg[1];
    type = strtol(t, NULL, 10);

  thelogger->bytes_in += strlen(msg);
    switch (type) {
        case 1:
            pthread_rwlock_rdlock(&lock);
            login(socket, msg + 2);
            printf("Received login request: %s\n", msg);
            pthread_rwlock_unlock(&lock);
            return 1;
        case 2:
            pthread_rwlock_rdlock(&lock);
            logout(socket);
            printf("Received logout request: %s\n", msg);
            pthread_rwlock_unlock(&lock);
            return 2;
        case 3:
            pthread_rwlock_rdlock(&lock);
            want_play(theusers, &thelobby, &thegames, socket, &thelogger);
            printf("Received joinLobby request: %s\n", msg);
            pthread_rwlock_unlock(&lock);
            return 3;
        default:
            printf("%s\n", msg);
            return 0;
    }
}

void *connection_handler(void *arg) {
    int client_sock, val, size_rec, res;
    char msg[200], msg_size[3];
    client_sock = *(int *) arg;
    int missed_ping = 0;

	printf("users:%d\n",theusers->user_count);

    while (1) {
        memset(msg, '\0', sizeof(msg));
        memset(msg_size, '\0', sizeof(msg_size));
        val = recv(client_sock, msg_size, 3, 0);

        while (val < 0 && missed_ping < 12) {

            if (missed_ping == 1) {
                printf("Sending lost con notify.\n");
            }
			send_message(client_sock, "ping\n", &thelogger);
            printf("Timeout. Sending ping to socket: %d\n", client_sock);
            val = recv(client_sock, msg_size, 3, 0);

            missed_ping++;
        }

        if (missed_ping == 12) {
            logout(client_sock);
            printf("Closing connection. Socket: %d\n", client_sock);
            free(arg);
            return 0;
        }

        missed_ping = 0;

        size_rec = strtol(msg_size, NULL, 10);

        if (size_rec > 0) {
            val = recv(client_sock, msg, size_rec, 0);
            thelogger -> bytes_in += (3);
        }
        if (val < 0) {
            continue;
        }
		
        res = parse_msg(client_sock, msg);

        if (res == 2) {
            close(client_sock);
            free(arg);
            break;
        }

        if(val == 0) {
            printf("Connection closed.\n");
            logout(client_sock);
            close(client_sock);
            free(arg);
            break;
        }

        if (res == 0) {
            printf("Message not recognized\n");
            logout(client_sock);
            close(client_sock);
            free(arg);
            break;
        }
    }
    printf("Bytes in:%d\n", thelogger -> bytes_in);
    printf("Bytes out:%d\n", thelogger -> bytes_out);
    return 0;
}

int main(int argc, char *argv[]) {
    int port;
    char *ip = NULL;

    struct timeval tmvl;
    tmvl.tv_sec = 5;
    tmvl.tv_usec = 0;

    if (argc < 4) {
        printf("Not enough arguments.\n");
        printf("First argument is IP address, 0 for any\n");
        printf("Second argument is Port\n");
        printf("Third argument - number of users\n");
        return EXIT_FAILURE;
    }
    if (argc > 4) {
        printf("Too many arguments.\n");
        return EXIT_FAILURE;
    } else {
        MAX_USERS = atoi(argv[3]);
        port = atoi(argv[2]);
        if (MAX_USERS == 0) {
            printf("Invalid max users param.\n");
            return EXIT_FAILURE;
        }
        if (port < 0 || port > 65535) {
            printf("Invalid port.\n");
            return EXIT_FAILURE;
        }
    }

    ip = malloc(strlen(argv[1]) * sizeof(char));
    strcpy(ip, argv[1]);
    int server_socket, client_socket, ret_val, *th_socket;
    struct sockaddr_in my_addr;
    struct sockaddr_in remote_addr;
    socklen_t remote_addr_len;
    pthread_t thread_id;

    if (pthread_rwlock_init(&lock, NULL) != 0) {
        printf("Cant initialize rw_lock server can't start.\n");
        exit(2);
    }

    users_create(&theusers);		//struktura obsahujici pole struktur
	lobby_create(&thelobby);	//ukazatel na strukturu
	games_create(&thegames);	//as users
	logger_create(&thelogger);

    server_socket = socket(AF_INET, SOCK_STREAM, 0);
    memset(&my_addr, 0, sizeof(struct sockaddr_in));
    my_addr.sin_family = AF_INET;
    my_addr.sin_port = htons(port);

    if (!strcmp(argv[1], "0")) {
        my_addr.sin_addr.s_addr = INADDR_ANY;
    } else {
        int a = is_valid_ip(argv[1]);
        if (a) {
            my_addr.sin_addr.s_addr = inet_addr(ip);
        } else {
            printf("Invalid IP\n");
            return EXIT_FAILURE;
        }
    }

    int param = 1;

    ret_val = setsockopt(server_socket, SOL_SOCKET, SO_REUSEADDR, (const char *) &param, sizeof(int));

    if (ret_val == -1) {
        printf("setsockopt ERR\n");
    }

    ret_val = bind(server_socket, (struct sockaddr *) &my_addr, sizeof(struct sockaddr_in));

    if (ret_val == 0) {
        printf("Bind - OK\n");
    } else {
        printf("Bind - ERR\n");
        return -1;
    }

    ret_val = listen(server_socket, 5);

    if (ret_val == 0) {
        printf("Listen - OK\n");
    } else {
        printf("Listen - ER\n");
    }

    while (1) {
        client_socket = accept(server_socket, (struct sockaddr *) &remote_addr, &remote_addr_len);
        setsockopt(client_socket, SOL_SOCKET, SO_RCVTIMEO, (struct timeval *) &tmvl, sizeof(struct timeval));
        if (client_socket > 0) {
            th_socket = malloc(sizeof(int));
            *th_socket = client_socket;
            printf("[%d] New connection.\n",client_socket);
            pthread_create(&thread_id, NULL, (void *) &connection_handler, (void *) th_socket);
        } else {
            printf("Fatal ERROR\n");
            return -1;
        }
    }

    return 0;
}