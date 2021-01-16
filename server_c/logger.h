#ifndef LOGGER_H
#define LOGGER_H

#include <stdlib.h>

typedef struct the_logger {
	int bytes_out;  //from server to client
	int bytes_in;   //from client to server
} logger;

void logger_create(logger **log);
void logger_reset(logger **log);
void logger_add_bytes_out(logger **log, int bytes);
void logger_add_bytes_in(logger **log, int bytes);
int logger_get_bytes_in(logger *log);
int logger_get_bytes_out(logger *log);

#endif