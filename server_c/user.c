#include <stdlib.h>
#include <string.h>
#include "user.h"
#include "lobby.h"
#include "logger.h"
#include "usefc.h"

void users_create(users **usrs) {
	(*usrs) = calloc(1, sizeof(users));
    if(!(*usrs)){
        printf("Users creation err in users_create!\n");
        return;
    }
	(*usrs) -> user_count = 0;
	(*usrs) -> users = calloc(1, sizeof(user));
    if(!(*usrs) -> users){
        printf("User memory alocation err in users_create!\n");
        return;
    }
	return;
}

void user_create(user **usr, char *name, int socket_ID) {
	(*usr) = calloc(1, sizeof(user));
    if(!(*usr)){
        printf("User creation err in user_create!\n");
        return;
    }
	(*usr) -> name = calloc(1, strlen(name) * sizeof(char));
    if(!(*usr) -> name){
        printf("Name memory alocation err in user_create!\n");
        return;
    }
	strcpy((*usr) -> name, name);
	(*usr) -> socket = socket_ID;
	(*usr) -> health = 100;
	return;
}

void user_add(users **usrs, char *name, int socket_ID, logger **log) {
	(*usrs) -> user_count++;

	printf("Users count: %d\n", (*usrs) -> user_count);
	(*usrs) -> users = realloc((*usrs) -> users, (*usrs) -> user_count * sizeof(user));
    if(!(*usrs) -> users){
        printf("Users memory realocation err in user_add!\n");
        return;
    }
	user *usr = NULL;
	user_create(&usr, name, socket_ID);
    if (!usr) {
        printf("Error, couldn't create new user.\n");
        printf("Sent message: logi-nack\n");
		send_message(socket_ID, "logi-nack\n", log);
        return;
    }
	(*usrs) -> users[((*usrs) -> user_count) - 1] = usr;
	return;
}

void user_remove(users **usrs, lobby **thelobby, int socket_ID, logger **log) {
	int i;
	int count = (*usrs) -> user_count;
	int socket;	
	for (i = 0; i < count; i++) {
		socket = (*usrs) -> users[i] -> socket;
		if (socket == socket_ID) {
			lobby_remove_player(thelobby, socket_ID);
			(*usrs) -> user_count--;			
			if (i < (count - 1)) {
				free((*usrs) -> users[i]);
				(*usrs) -> users[i] = (*usrs) -> users[((*usrs) -> user_count)];								
			}
			(*usrs) -> users[((*usrs) -> user_count)] = NULL;			
			(*usrs) -> users = realloc((*usrs) -> users, (*usrs) -> user_count * sizeof(user));
            if(!(*usrs) -> users){
                printf("Users memory realocation err in user_remove!\n");
				printf("Sent message: logo-nack\n");
                return;
            }            
            printf("User %s logged out\n", (*usrs) -> users[i] -> name);
		    send_message(socket_ID, "logo-ack\n", log);
			printf("Actually logged users: %d\n", (*usrs) -> user_count);	
			return;
		}
	}    
    printf("Failed to log out user [%d]\n", socket_ID);
    printf("Sent message: logo-nack\n");
	send_message(socket_ID, "logo-nack\n", log);
	return;
}

user *user_get_user_by_name(users *usrs, char *name) {
	int i;
	int count = usrs -> user_count;	
	char *nam;
	for (i = 0; i < count; i++) {
		nam = usrs -> users[i] -> name;
		if (strcmp(nam, name) == 0) {
			return usrs -> users[i];
		}
	}
	return NULL;
}

user *user_get_user_by_socket_ID(users *usrs, int socket_ID) {
	int i;
	int count = usrs -> user_count;
	int socket;	
	for (i = 0; i < count; i++) {
		socket = usrs -> users[i] -> socket;
		if (socket == socket_ID) {
			return usrs -> users[i];
		}
	}
	return NULL;
}