#ifndef GAME_H
#define GAME_H

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "user.h"
#include "usefc.h"
#include "logger.h"

typedef struct the_game game;
typedef struct the_games games;
struct the_game {
	char *name_1;
	char *name_2;
	char *now_playing;
	int game_ID;
	
};
struct the_games {
	int games_count;
	game **games;
};


void games_create(games **all_games);
void game_create(game **gm, char *name_1, char *name_2, char *now_playing);
void game_add(games **all_games, char *name_1, char *name_2, char *now_playing);
void game_remove(users *usrs, games **all_games, int game_ID, logger **log);
game *find_game_by_name(games *all_games, char *name);
void game_end(users *usrs, games **all_games, int game_ID, int status, int status_opponent, int current_player_socket_ID, int second_player_socket_ID, logger **log);

#endif