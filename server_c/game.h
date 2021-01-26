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
	char *name_1;				//jmeno prvniho hrace
	char *name_2;				//jmeno druheho hrace
	char *now_playing_name;		//jmeno hrace ktery prave hraje
	int health_1;				//zdravi prvniho hrace
	int health_2;				//zdravi prvniho hrace
	int game_ID;				//id hry
	
};
struct the_games {
	int games_count;
	game **games;
};


void games_create(games **all_games);
void game_create(game **thegame, char *name_1, char *name_2, char *now_playing_name);
void game_add(games **all_games, char *name_1, char *name_2, char *now_playing_name);
void game_remove(games **all_games, int game_ID);
game *find_game_by_name(games *all_games, char *name);
void print_all_games(games *all_games);

#endif