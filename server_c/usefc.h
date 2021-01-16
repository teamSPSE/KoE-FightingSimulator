#ifndef USEFC_H
#define USEFC_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "logger.h"

void send_message(int user_socket, char *message, logger **info);
int valid_digit(char *ip_str);
int is_valid_ip(char *ip_str);

#endif