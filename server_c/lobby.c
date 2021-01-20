#include <stdlib.h>
#include <stdio.h>
#include "lobby.h"

void lobby_create(lobby **thelobby) {
	(*thelobby) = calloc(1, sizeof(thelobby));
	(*thelobby) -> size = 0;
	(*thelobby) -> socket_IDs = calloc(1, sizeof(int));
}

/*
 * add player to array 
 * @param wanna_play - array of clients who wants to play a game
 * @param socket_ID - socket ID of logging client
 */
void lobby_add_player(lobby **thelobby, int socket_ID) {
	(*thelobby) -> size++;
	//printf("socket_ID %d want to play a game\n", socket_ID);
	(*thelobby) -> socket_IDs = realloc((*thelobby) -> socket_IDs, (*thelobby) -> size * sizeof(int));
	(*thelobby) -> socket_IDs[((*thelobby) -> size) - 1] = socket_ID;
	//printf("%d client/s want to play a game\n", (*thelobby) -> size);
}

/*
 * remove player from array 
 * @param wanna_play - array of clients who wants to play a game
 * @param socket_ID - socket ID of logging client
 */
void lobby_remove_player(lobby **thelobby, int socket_ID) {
	int i;
	int socket;
	int count = (*thelobby) -> size;
	for(i = 0; i < count; i++) {
		socket = (*thelobby) -> socket_IDs[i];
		if (socket == socket_ID) {
			(*thelobby) -> size--;			
			if (i < (count - 1)) {
				(*thelobby) -> socket_IDs[i] = (*thelobby) -> socket_IDs[((*thelobby) -> size)];								
			}	
			(*thelobby) -> socket_IDs = realloc((*thelobby) -> socket_IDs, (*thelobby) -> size * sizeof(lobby));
			//printf("socket ID %d removed from queue\n", socket_ID);
			//printf("%d client/s want to play a game\n", (*thelobby) -> size);
			return;
		}
	}
}