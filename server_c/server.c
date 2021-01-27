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


/*maximalni pocet uzivatelu - defaultne neni omezeno */
int MAX_USERS = 0;

/*uzivatele*/
users *theusers;

/*lobby*/
lobby *thelobby;

/*hry*/
games *thegames;

/*logger - pocita kolik bylo poslano/prijato bytu*/
logger *thelogger;

/*pozastaveni jadra*/
pthread_rwlock_t lock;

/* vypise uzivatele, hry a lobby */
void print_all_vars(){
    print_all_users(theusers);
    print_all_games(thegames);
    print_lobby(thelobby);
}

/* prihlasi uzivatele
    @param int socket - socket uzivatele
    @param char *name - jmeno uzivatele
 */
void login(int socket, char *name) {
    int i;
    game *tmp_game;
    
    //kontrola poctu uzivatelu
    if (MAX_USERS > 0 && theusers -> user_count == MAX_USERS) {
        printf("Couldn't login user: %s. Maximum of logged in players reached.\n", name);
        printf("Sent message: logi-nackfull\n");
		send_message(socket, "logi-nackfull\n", &thelogger);
        return;
    }

    //dostupnost jmena
	if(user_get_user_by_name(theusers, name) != NULL){
        printf("Username: %s already taken.\n", name);
        printf("Sent message: logi-nackname\n");
		send_message(socket, "logi-nackname\n", &thelogger);
        return;        
    }
	user_add(&theusers, name, socket, &thelogger);

    printf("User :%s logged in.\n", name);
    printf("Sent message: logi-ack\n");
	send_message(socket, "logi-ack\n", &thelogger);

    //reconnect
    tmp_game = find_game_by_name(thegames, name);
    if(tmp_game != NULL){            
        int *my_user_health;
        int *second_user_health;
        int now_playing_id;
        char message_reconn[20];

        if(strcmp(tmp_game->name_1, name)==0){
            second_user_health = &(tmp_game -> health_2);
            my_user_health = &(tmp_game -> health_1);
        }else{
            second_user_health = &(tmp_game -> health_1);
            my_user_health = &(tmp_game -> health_2);
        }

        now_playing_id = strcmp(tmp_game->now_playing_name, name)==0 ? 1 : 0;

        printf("user %s is in a game(%d)!\n", name, tmp_game->game_ID);
        sprintf(message_reconn, "game-reconnected-%d-%d-%d\n", *my_user_health, *second_user_health, now_playing_id);         //game-reconnected-myhealth-enemyhealth-nowplaying(0/1)
        send_message(socket, &message_reconn[0], &thelogger);   
    }
}

/* odhlaseni uzivatele */
void logout(int socket) {
    if(user_get_user_by_socket_ID(theusers, socket) != NULL){
        print_all_vars();
        user_remove(&theusers, &thelobby, socket, &thelogger);
    }
}

/* pridani uzivatele do lobby
    @param int socket - socket uzivatele
 */
void join_lobby(int socket) {
	user *my_user = NULL;
	user *second_user = NULL;
	lobby_add_player(&thelobby, socket);
    
    if(user_get_connected(theusers, socket))
        send_message(socket, "lobby-ack\n", &thelogger);

	if (thelobby -> size >= 2) {
		int socket_ID_1 = socket;
		int socket_ID_2;
		do {
			socket_ID_2 = thelobby -> socket_IDs[rand() % (thelobby -> size)];
		} 
		while(socket_ID_2 == socket_ID_1);								

		lobby_remove_player(&thelobby, socket_ID_1);
		lobby_remove_player(&thelobby, socket_ID_2);																			

		my_user = user_get_user_by_socket_ID(theusers, socket_ID_1);
		second_user = user_get_user_by_socket_ID(theusers, socket_ID_2);

        printf("socket_ID_1:%d | socket_ID_2:%d\n",socket_ID_1,socket_ID_2);
		game_add(&thegames, my_user -> name, second_user -> name, my_user -> name);
		
        if(my_user->connected)
		    send_message(socket_ID_1, "game-started-1\n", &thelogger);

        if(second_user->connected)            
		    send_message(socket_ID_2, "game-started-0\n", &thelogger);
        
	}
	return;
}

/* 
    provede poskozeni
    @param int socket - socket uzivatele, ktery posila poskozeni
    @param char *msg - poskozeni
 */
void processDMG(int socket, char *msg){
    int dmg = atoi(msg+2);
    printf("recvd dmg %s %d",msg, dmg);
    user *my_user = NULL;
    user *second_user = NULL;
    game *thegame = NULL;
    char message_1[20];
    char message_2[20];
    int *my_user_health;
    int *second_user_health;
    
	my_user = user_get_user_by_socket_ID(theusers, socket);
    thegame = find_game_by_name(thegames, my_user->name);    
    
    if(strcmp(thegame->name_1, my_user->name)==0){
        second_user = user_get_user_by_name(theusers, thegame->name_2);
        second_user_health = &(thegame -> health_2);
        my_user_health = &(thegame -> health_1);
    }else{
        second_user = user_get_user_by_name(theusers, thegame->name_1);
        second_user_health = &(thegame -> health_1);
        my_user_health = &(thegame -> health_2);
    }

    if(!my_user || !second_user){
        printf("cant find user in processDMG.\n");
        if(my_user == NULL && second_user != NULL){
            printf("user with socket %d is disconnected, wait for him\n", socket);
            if(second_user->connected)
                send_message(second_user->socket, "game-userdsc\n", &thelogger);
        }
        if(my_user != NULL && second_user == NULL){
            printf("second user is disconnected, wait for him\n");
            if(my_user->connected)
                send_message(my_user->socket, "game-userdsc\n", &thelogger);
        }
        return;
    }

    *second_user_health = *second_user_health - dmg;
    thegame->now_playing_name = second_user->name;

    //pokud oponent jeste ma dost zdravi, presuneme se na dalsi tah, pokud ne hra konci
    if(*second_user_health > 0){
        sprintf(message_1, "game-update-%d-%d\n", *my_user_health, *second_user_health);
        if(my_user->connected)
            send_message(my_user->socket, &message_1[0], &thelogger);

        sprintf(message_2, "game-update-%d-%d\n", *second_user_health, *my_user_health);
        if(second_user->connected)
            send_message(second_user->socket, &message_2[0], &thelogger);
    }else{
        printf("game finished\n");
        game_remove(&thegames, thegame->game_ID);
        
        if(my_user->connected)
            send_message(my_user->socket, "game-finish-1\n", &thelogger);        //winner
        if(second_user->connected)
            send_message(second_user->socket, "game-finish-0\n", &thelogger);    //looser
    }
}

/* vyhodnocuje prijatou zpravu
    @param int socket - socket klienta, ktery poslal zpravu
    @param char *msg - poslana zprava
 */
int parse_msg(int socket, char *msg) {
    long int type;
    char *name, *room, t[2], *place, *x, *y;
    t[0] = msg[0];
    t[1] = msg[1];
    type = strtol(t, NULL, 10);

  thelogger->bytes_in += strlen(msg);
    switch (type) {
        case 1: //login
            pthread_rwlock_rdlock(&lock);
            printf("Received login request: %s\n", msg);
            login(socket, msg + 2);
            pthread_rwlock_unlock(&lock);
            return 1;
        case 2: //logout
            pthread_rwlock_rdlock(&lock);
            printf("Received logout request: %s\n", msg);
            logout(socket);
            pthread_rwlock_unlock(&lock);
            return 2;
        case 3: //pridani uzivatele do lobby
            pthread_rwlock_rdlock(&lock);         
            printf("Received joinLobby request: %s\n", msg);
            join_lobby(socket);   
            pthread_rwlock_unlock(&lock);
            return 3;
        case 4: //provedeni poskozeni
            pthread_rwlock_rdlock(&lock);         
            printf("Received processDMG request: %s\n", msg);
            processDMG(socket, msg);   
            pthread_rwlock_unlock(&lock);
            return 4;
        case 10:    //odpoved na reconnect klienta
            pthread_rwlock_rdlock(&lock);         
            printf("Received game reconnected response.\n"); 
            pthread_rwlock_unlock(&lock);
            return 10;
        case 11:    //odpoved na start hry
            pthread_rwlock_rdlock(&lock);         
            printf("Received game started response.\n");
            pthread_rwlock_unlock(&lock);
            return 11;
        case 13:    //odpoved na ping klienta
            pthread_rwlock_rdlock(&lock);
            //printf("Received ping message, sending response.\n");
            if(user_get_connected(theusers, socket))
                send_message(socket, "alive\n", &thelogger);
            pthread_rwlock_unlock(&lock);
            return 13;   
        default:    //nevyhodnocena zprava - zaviram spojeni
            printf("%s\n", msg);
            return 0;
    }
}

/* spravuje, co se ma dit behem spojeni
    @param void *arg - socket klienta
 */
void *connection_handler(void *arg) {
    int client_sock, val, size_rec, res;
    char msg[200], msg_size[3];
    client_sock = *(int *) arg;

    while (1) {
        memset(msg, '\0', sizeof(msg));
        memset(msg_size, '\0', sizeof(msg_size));
        val = recv(client_sock, msg_size, 3, 0);

        size_rec = strtol(msg_size, NULL, 10);

        if (size_rec > 0) {
            val = recv(client_sock, msg, size_rec, 0);
            thelogger -> bytes_in += (3);
        }
        if (val < 0) {
            continue;
        }
        
        if(val == 0) {
            printf("Connection closed.\n");
            user_set_connected(&theusers, client_sock, 0);
            logout(client_sock);
            close(client_sock);
            free(arg);
            break;
        }
		
        res = parse_msg(client_sock, msg);

        if (res == 2) {
            user_set_connected(&theusers, client_sock, 0);
            close(client_sock);
            free(arg);
            break;
        }

        if (res == 0) {
            printf("Message not recognized\n");
            user_set_connected(&theusers, client_sock, 0);
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

/* hlavni metoda programu */
int main(int argc, char *argv[]) {
    int port;
    char *ip = NULL;

    struct timeval tmvl;
    tmvl.tv_sec = 5;
    tmvl.tv_usec = 0;

    if (argc < 3) {
        printf("Not enough arguments.\n");
        printf("First argument is IP address, 0 for any\n");
        printf("Second argument is Port\n");
        printf("Third argument - number of users (optimal)\n");
        return EXIT_FAILURE;
    }
    if (argc > 4) {
        printf("Too many arguments.\n");
        return EXIT_FAILURE;
    } else {
        if(argc == 4)
            MAX_USERS = atoi(argv[3]);
        else
            MAX_USERS = 0;

        port = atoi(argv[2]);
        if (port < 0 || port > 65535) {
            printf("Invalid port. Port has to be in <0,65535>\n");
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

    users_create(&theusers);		//struktura obsahujici pole struktur uzivatelu
	lobby_create(&thelobby);	    //ukazatel na strukturu lobby
	games_create(&thegames);	    //struktura obsahujici pole struktur her
	logger_create(&thelogger);      //logger

    server_socket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);    //0 - udp | IPPROTO_TCP - tcp
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