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
	int game_ID;
	
};
struct the_games {
	int games_count;
	game **games;
};


void games_create(games **all_games);
void game_create(game **all_games, char *name_1, char *name_2);
void game_add(games **all_games, char *name_1, char *name_2);
void game_remove(games **all_games, int game_ID);
game *find_game_by_name(games *all_games, char *name);
void print_all_games(games *all_games);

#endif