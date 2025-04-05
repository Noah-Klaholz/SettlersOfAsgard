# README - Settlers of Asgard

## Überblick

Settlers of Asgard ist ein strategisches Multiplayer-Wirtschaftsspiel für 3-4 Spieler, 
in dem es darum geht, durch geschicktes Kaufen von Feldern, den Bau und das Upgraden von Strukturen
sowie die Nutzung von Artefakten und Göttersegen möglichst viele Runen zu sammeln. 
Der Spieler mit den meisten Runen nach 5 Spielrunden gewinnt das Spiel.

## Links

Genauere Dokumentation der voraussichtlichen Spielregeln: [Spielregeln](docs/Spielbeschreibungen/GameDesign.pdf).

Netzwerkprotokoll: [Netzwerkprotokoll](docs/Netzwerkprotokoll_v1.2.0.pdf).

Github Kollaboratoren: [Kollaboratoren](docs/Contributors/CONTRIBUTORS.txt).

Eine Übersicht über den ständig aktualisierten Projektplan, Kalender und Aufgabenzuteilung findet sich hier:
[Projektmanagement](https://tungsten-carrot-2b4.notion.site/1ad104ac2da581e0bd69d7f92ebc897b?v=1ad104ac2da581f5ba1b000c74035432&pvs=4)

Das ständig aktualisierte Projekttagebuch findet sich hier:
[Tagebuch](https://tungsten-carrot-2b4.notion.site/Projekttagebuch-1ad104ac2da58189ad61c4600e771cbd?pvs=4)
## Starten
Via gradle die jar Files builden (sofern nicht bereits vorhanden):
```shell
./gradlew jar
```
Im Terminal: 
```shell
Server: java -jar /build/libs/SettlersOfAsgard.jar server <listenport>
Client: java -jar /build/libs/SettlersOfAsgard.jar client <server-ip> <server-port>
```
Beachte: listenport und server-port müssen identisch sein.
Die server-ip ist entweder die lokale IP-Adresse des Servers im Netzwerk oder "localhost".