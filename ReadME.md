# Online Voting System
##Autores  
* Duarte Dias 2018293526 duartedias@student.uc.pt
* Gabriel Fernandes 2018288117 gabrielf@student.dei.uc.pt

# Notes
This only contains the backend to the program.

# Como correr o Programa
Inserir os 4 jar ficheiros numa pasta(console, rmiserver, server e terminal);
Criar um pasta chamada db na mesma pasta onde estão os outros 4 ficheiros.

## Descrição do Parametros
ServerIP -> IP do servidor de RMI;
ServerPort -> Port do servidor de RMI;
BackupIP -> IP do servidor backup de RMI;
BackupPort -> Port do servidor backup de RMI;
ServerName -> Nome do servidor RMI;
TimeoutTime -> Time until the The server reports an error connecting;
MulitcastTerminalNumber -> Numero Total de Terminais de voto (usado na mesa de voto)
TableName -> Nome da mesa de voto;
MulticastDiscoveryIP -> IP usado na multicast table;
MulticastDiscoveryPort -> Port utilizado na multicast table para discovery;
MulticastrequestHandlerPort -> Port utilizado na multicast table para requestHandler;
MulticastTerminalNumber -> Numero de terminais Multicast;
nMessagesTimeout -> NUmero de mensagens que terminal le ate que reenvia mensagem ao servidor;
TerminalNumber -> Numero de identificação do terminal;

### rmiserver 
Parametros 
ServerIP, ServerPort, BackupIP, BackupPort, ServerName;


### console 
Parametros
ServerIP, ServerPort, BackupIP, BackupPort, ServerName, TimeoutTime;

### server 
Parametros
MulticastDiscoveryIP, MulticastDiscoveryPort, MulticastrequestHandlerPort, ServerIP, ServerPort, BackupIP, BackupPort, ServerName, MulticastTerminalNumber, TableName, TimeoutTime;

### terminal
Parametros
TerminalNumber , MulticastDiscoveryIP, MulticastDiscoveryIP, MulticastDiscoveryPort, MulticastrequestHandlerPort, nMessagesTimeout, TimeoutTime;


## Example usage (on the same machine)
ServerName										SV
ServerIP										localhost
ServerPort 									 	3200
BackupIP 										localhost
BackupPort 										4200
nTables                                         1
TableNames                                      DEI
MulticastDiscoveryIP 							224.3.2.1
MulticastDiscoveryPort 							4321
MulticastrequestHandlerPort 					43210
MulticastTerminalNumber 						3
MulticastTerminalStartingIP 					224.3.2.3
TimeoutTime 					                10
Configuration                                   3
nMessagesTimeout                                10


java -jar rmiserver.jar localhost 3200 localhost 4200 SV
java -jar rmiserver.jar localhost 4200 localhost 3200 SV
java -jar console.jar localhost 3200 localhost 4200 SV 10
java -jar server.jar 224.3.2.1 4321 43210 localhost 3200 localhost 4200 SV 3 DEI 10
java -jar terminal.jar 0 224.3.2.1 224.3.2.1 4321 43210 10 10
java -jar terminal.jar 1 224.3.2.1 224.3.2.1 4321 43210 10 10
java -jar terminal.jar 2 224.3.2.1 224.3.2.1 4321 43210 10 10


## Contributors
* [Gabriel Fernandes](https://github.com/gabrielmendesfernandes)
