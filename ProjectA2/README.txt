For the normal communication with server, there is a jar file called  ProhectA2.jar. In the console, just need use the command java -jar ProjectA2.jar -ip dimefox.eng.unimelb.edu.au -port 8001 -studentId 655251. Then the client would communicate with the server.

I am opting for the bonus.
I have completed Man-in-the-middle attack. There is a jar file called MitM.jar. In order to perform the Man-in-the-middle attack. Firstly, you should start the  proxy server by the command: java -jar MitM.jar -ip dimefox.eng.unimelb.edu.au -port 8001 -studentId 655251. Then you should start the client program by the command: java -jar ProjectA2.jar -ip 127.0.0.1 -port 7899 -studentId 655251. Then you could see how the attack happeds.

I have also tried the DES. I refered some code from https://dl.dropboxusercontent.com/u/31222469/blog/crypto/DES.java. I did not complete all the des. There are single functions that may be correct.