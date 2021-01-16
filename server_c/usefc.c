
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include "usefc.h"
#include "logger.h"

void send_message(int user_socket, char *message, logger **info) {	
	printf("%d Writed message: %s\n", user_socket, message);
	send(user_socket, message, strlen(message) * sizeof(char), 0);	
	(*info) -> bytes_out += strlen(message) + 1;
	return;
}

int valid_digit(char *ip_str) {
    while (*ip_str) {
        if (*ip_str >= '0' && *ip_str <= '9')
            ++ip_str;
        else
            return 0;
    }
    return 1;
}

int is_valid_ip(char *ip_str) {
    int i, num, dots = 0;
    char *ptr;
    if (ip_str == NULL)
        return 0;
    ptr = strtok(ip_str, ".");
    if (ptr == NULL)
        return 0;
    while (ptr) {
        if (!valid_digit(ptr)) {
            return 0;
        }
        num = atoi(ptr);
        if (num >= 0 && num <= 255) {
            ptr = strtok(NULL, ".");
            if (ptr != NULL)
                ++dots;
        } else {
            return 0;
        }
    }
    if (dots != 3)
        return 0;
    return 1;
}