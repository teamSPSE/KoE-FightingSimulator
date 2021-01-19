#ifndef USER_H
#define USER_H

#include "lobby.h"
#include "logger.h"
#include <stdio.h>

typedef struct theuser{
    char *name;
    int socket;
    int health;
} user;
typedef struct theusers{
    int user_count;
    user **users;
} users;

void users_create(users **usrs);
void user_create(user **usr, char *name, int socket_ID);
void user_add(users **usrs, char *name, int socket_ID, logger **log);
void user_remove(users **usrs, lobby **thelobby, int socket_ID, logger **log);
user *user_get_user_by_name(users *usrs, char *name);
user *user_get_user_by_socket_ID(users *usrs, int socket_ID);
#endif
