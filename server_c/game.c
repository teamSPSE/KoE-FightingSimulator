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
void game_create(game **gm, char *name_1, char *name_2, char *now_playing) {
	(*gm) = calloc(1, sizeof(game));
	(*gm) -> name_1 = name_1;
	(*gm) -> name_2 = name_2;
	(*gm) -> now_playing = now_playing;
}

/*
 * add game into array
 * @param all_games - array of all games 
 * @param name_1 - name of player 1
 * @param name_2 - name of player 2
 * @param now_playing - name of player who is on turn
 */
void game_add(games **all_games, char *name_1, char *name_2, char *now_playing) {printf("3.1\n");
	(*all_games) -> games_count++;printf("3.1\n");
	printf("Games count: %d\n", (*all_games) -> games_count);printf("3.1\n");
	(*all_games) -> games = realloc((*all_games) -> games, (*all_games) -> games_count * sizeof(game));printf("3.1\n");
	game *game = NULL;printf("3.1\n");
	game_create(&game, name_1, name_2, now_playing);
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
void game_remove(users *usrs, games **all_games, int game_ID, logger **log) {
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
	
	if (update_game_ID == 1) {
		char message_1[30];
		sprintf(message_1, "update_game_ID;%d;\n", index);
		char message_2[30];
		sprintf(message_2, "update_game_ID;%d;\n", index);

		user *user_1 = user_get_user_by_name(usrs, (*all_games) -> games[index] -> name_1);
		printf("sending msg to %s: %s\n", user_1 -> name, &message_1[0]);
		send_message(user_1 -> socket, &message_1[0], log);

		user *user_2 = user_get_user_by_name(usrs, (*all_games) -> games[index] -> name_2);
		printf("sending msg to %s: %s\n", user_2 -> name, &message_2[0]);
		send_message(user_2 -> socket, &message_2[0], log);
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

/*
 * end game 
 * @param cls - array of clients 
 * @param all_games - array of all games
 * @param game_ID - ID of game
 * @param status - status of first player
 * @param status_opponent - status of opponent
 * @param current_player_socket_ID - socket ID of current player
 * @param second_player_socket_ID - socket ID of opponent
 * @param info - structures to save log info
 */
void game_end(users *usrs, games **all_games, int game_ID, int status, int status_opponent, int current_player_socket_ID, int second_player_socket_ID, logger **log) {
	switch(status_opponent) {
		case 0: 			
			break;

		case 1:				
			break;
	}
	
	user *cl_1 = user_get_user_by_socket_ID(usrs, current_player_socket_ID);
	user *cl_2 = user_get_user_by_socket_ID(usrs, second_player_socket_ID);
	game_remove(usrs, all_games, game_ID, log);
	return;
}