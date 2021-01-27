#ifndef USER_H
#define USER_H

#include "lobby.h"
#include "logger.h"
#include <stdio.h>

/*
struktura uzivatele
name = jmeno uzivatele
socket = socket uzivatele
connected = bool jestli je pripojen
*/
typedef struct theuser{
    char *name;
    int socket;
    int connected;
} user;

/*
struktura uzivatelu
user_count = pocet uzivatelu
user **users = pole uzivatelu
*/
typedef struct theusers{
    int user_count;
    user **users;
} users;

/* vytvori uzivatele */
void users_create(users **usrs);

/* vytvori jednoho uzivatele */
void user_create(user **usr, char *name, int socket_ID);

/* prida uzivatele do pole uzivatelu */
void user_add(users **usrs, char *name, int socket_ID, logger **log);

/* ostrani uzivatele */
void user_remove(users **usrs, lobby **thelobby, int socket_ID, logger **log);

/* najde uzivatele podle jmena */
user *user_get_user_by_name(users *usrs, char *name);

/* najde uzivatele podle socketu */
user *user_get_user_by_socket_ID(users *usrs, int socket_ID);

/* vypise vsechny uzivatele */
void print_all_users(users *usrs);

/* get a set metoda atributu connected struktury user */
void user_set_connected(users **usrs, int socket, int connected);
int user_get_connected(users *usrs, int socket);
#endif
