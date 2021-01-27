#ifndef LOBBY_H
#define LOBBY_H

#include <stdlib.h>
#include <stdio.h>

/* struktura lobby */
typedef struct the_lobby {
	int size;			//veliksot
	int *socket_IDs;	//pole socketu v lobby
} lobby;

/* vytvoreni lobby */
void lobby_create(lobby **thelobby);

/* pridani hrace do lobby */
void lobby_add_player(lobby **thelobby, int socket_ID);

/* odstraneni hrace z lobby */
void lobby_remove_player(lobby **thelobby, int socket_ID);

/* vypis lobby */
void print_lobby(lobby *thelobby);

#endif