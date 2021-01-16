#include <stdio.h>
#include "logger.h"

void logger_create(logger **log){
    (*log) = calloc(1, sizeof(logger));
    if(!(*log)){
        printf("Logger creation err!\n");
        return;
    }
	(*log) -> bytes_out = 0;
	(*log) -> bytes_out = 0;
	return;
}
void logger_reset(logger **log){
    (*log) -> bytes_out = 0;
    (*log) -> bytes_in = 0;
}
void logger_add_bytes_out(logger **log, int bytes){
    (*log) -> bytes_out += bytes;
}
void logger_add_bytes_in(logger **log, int bytes){
    (*log) -> bytes_in += bytes;
}
int logger_get_bytes_in(logger *log){
    return log -> bytes_in;
}
int logger_get_bytes_out(logger *log){
    return log -> bytes_out;
}