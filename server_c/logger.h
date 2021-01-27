#ifndef LOGGER_H
#define LOGGER_H

#include <stdlib.h>

/* struktura ukladajici informace o zaslanych/prijatych zpravach */
typedef struct the_logger {
	int bytes_out;  //from server to client
	int bytes_in;   //from client to server
} logger;

/* vytvoreni loggeru */
void logger_create(logger **log);

/* resetovani loggeru - nastavi bytes_out a bytes_in na 0 */
void logger_reset(logger **log);

/* prida byty out */
void logger_add_bytes_out(logger **log, int bytes);

/* prida byty in */
void logger_add_bytes_in(logger **log, int bytes);

/* vypise byty in */
int logger_get_bytes_in(logger *log);

/* vypise byty out */
int logger_get_bytes_out(logger *log);

#endif