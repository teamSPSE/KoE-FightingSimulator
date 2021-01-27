#ifndef USEFC_H
#define USEFC_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "logger.h"

/**
 * uzitecne funkce
 */

/* posle zpravu klientovi s danny socketem */
void send_message(int user_socket, char *message, logger **info);

/* overi jestli je validni cislo */
int valid_digit(char *ip_str);

/* overu validni ip ve formatu a.b.c.d */
int is_valid_ip(char *ip_str);

#endif