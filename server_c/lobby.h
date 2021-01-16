#ifndef LOBBY_H
#define LOBBY_H

#include <stdlib.h>
#include <stdio.h>

typedef struct the_lobby lobby;
struct the_lobby {
	int size;
	int *socket_IDs;
};

void lobby_create(lobby **thelobby);
void lobby_add_player(lobby **thelobby, int socket_ID);
void lobby_remove_player(lobby **thelobby, int socket_ID);

#endif