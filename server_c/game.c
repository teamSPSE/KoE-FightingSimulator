#include "game.h"
#include "user.h"
#include "usefc.h"
#include "logger.h"

/*
 * add player to array 
 * @param all_games - array of all games
 */
void games_create(games **all_games) {
	int max_games = 16;
	(*all_games) = calloc(1, sizeof(games));
	(*all_games) -> games_count = 0;
	(*all_games) -> games = calloc(1, max_games * sizeof(game));	
}

/*
 * create game
 * @param gm - game 
 * @param name_1 - name of player 1
 * @param name_2 - name of player 2
 * @param now_playing - name of player who is on turn
 */
void game_create(game **thegame, char *name_1, char *name_2, char *now_playing_name) {
	(*thegame) = calloc(1, sizeof(game));
	(*thegame) -> name_1 = name_1;
	(*thegame) -> name_2 = name_2;
	(*thegame) -> now_playing_name = now_playing_name;
	(*thegame) -> health_1 = 100;
	(*thegame) -> health_2 = 100;
}

/*
 * add game into array
 * @param all_games - array of all games 
 * @param name_1 - name of player 1
 * @param name_2 - name of player 2
 * @param now_playing - name of player who is on turn
 */
void game_add(games **all_games, char *name_1, char *name_2, char *now_playing_name) {
	(*all_games) -> games_count++;
	//printf("Games count: %d\n", (*all_games) -> games_count);
	(*all_games) -> games = realloc((*all_games) -> games, (*all_games) -> games_count * sizeof(game));
	game *game = NULL;
	game_create(&game, name_1, name_2, now_playing_name);
	(*all_games) -> games[((*all_games) -> games_count) - 1] = game;
	(*all_games) -> games[((*all_games) -> games_count) - 1] -> game_ID = ((*all_games) -> games_count) - 1;
}

/*
 * remove game from array
 * @param cls - array of clients 
 * @param all_games - array of all games
 * @param name_2 - structures to save info about server
 * @param game_ID - ID of game
 */
void game_remove(games **all_games, int game_ID) {
	int i;
	int count = (*all_games) -> games_count;
	int index;
	int update_game_ID = 0;
	for (i = 0; i < count; i++) {
		if (i == game_ID) {					
			if (i < (count - 1)) {
				free((*all_games) -> games[i]);
				(*all_games) -> games[i] = (*all_games) -> games[((*all_games) -> games_count) - 1];		
				update_game_ID = 1;
			}
											
			(*all_games) -> games_count--;	
			if (((*all_games) -> games_count) > 0) {		
				(*all_games) -> games = realloc((*all_games) -> games, (*all_games) -> games_count * sizeof(game));

				index = i;			
				(*all_games) -> games[i] -> game_ID = index;
			}
			break;
		}
	}	
}

/*
 * find game by name of player
 * @param all_games - array of all games
 * @param name - name of player in game
 * @return game if found or NULL
 */
game *find_game_by_name(games *all_games, char *name) {
	int i;
	for (i = 0; i < all_games -> games_count; i++) {
		if (strcmp(name, all_games -> games[i] -> name_1) == 0 || strcmp(name, all_games -> games[i] -> name_2) == 0) {
			return all_games -> games[i];
		}  
	}
	return NULL;
}

void print_all_games(games *all_games){
	int count = all_games->games_count;
	int i;

	printf("\nprinting all games:\n");
	for(i = 0; i < count; i++){
		printf("[%d] gameID:%d name1:%s name2:%s\n", i, all_games->games[i]->game_ID, all_games->games[i]->name_1, all_games->games[i]->name_2);
	}
}