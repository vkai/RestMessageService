# Rest Message Service

To build:

Simply run 'gradle build'. This will compile the code, handle all dependencies, 
create distribution packages, and run tests.

To run:

Run java -jar RestMessageService-0.0.1.jar with the jar file produced in 
build/libs. The service can also be run with 'gradle run', or with the 
RestMessageService executable produced in the tar dist at build/distributions.

To use:

The service can be reached at http://localhost:8080 with these operations: 
POST /chat               creates a new message with {username,text,(timeout)}
GET  /chat/{id}          gets the username,text,expiration of message with id
GET  /chats/{username}   gets the unexpired messages for username

