#include <sys/types.h>
#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/wait.h>

int main() {
    int i;
    pid_t pid;

    for (i = 0; i < 10; i++) {
        pid = fork();
        if (pid < 0) {
            printf("fork failed");
            return 1;
        } else if (pid == 0) {
            printf("I'm the child number %d (pid %d)\n", i, getpid());
            exit(0);
        }else{
 wait(NULL);
        }
    }

    printf("Parent terminates (pid %d)\n", getppid());
    return 0;
}