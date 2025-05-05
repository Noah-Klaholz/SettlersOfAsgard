# README - Settlers of Asgard

## Überblick

Settlers of Asgard ist ein strategisches Multiplayer-Wirtschaftsspiel für 3-4 Spieler, 
in dem es darum geht, durch geschicktes Kaufen von Feldern, den Bau und das Upgraden von Strukturen
sowie die Nutzung von Artefakten und Göttersegen möglichst viele Runen zu sammeln. 
Der Spieler mit den meisten Runen nach 5 Spielrunden gewinnt das Spiel.

[Website](https://settlersofasgard.netlify.app)

## Dokumente

Alle relevanten Dokumente sind im Ordner `docs` zu finden.
Für die wichtigsten Dokumente sind hier Links angegeben:

## Links

Genauere Dokumentation der Spielregeln: [Spielregeln](docs/Spielbeschreibungen/GameDesign.pdf).
Dokumentation des genauen Spielablaufs: [Manual](docs/Spielbeschreibungen/GameManual.md).

Netzwerkprotokoll: [Netzwerkprotokoll](docs/Networking/Netzwerkprotokoll.pdf).

Github Kollaboratoren: [Kollaboratoren](docs/Contributors/CONTRIBUTORS.txt).

Eine Übersicht über den ständig aktualisierten Projektplan, Kalender und Aufgabenzuteilung findet sich hier:
[Projektmanagement](https://tungsten-carrot-2b4.notion.site/1ad104ac2da581e0bd69d7f92ebc897b?v=1ad104ac2da581f5ba1b000c74035432&pvs=4)

Das ständig aktualisierte Projekttagebuch findet sich hier:
[Tagebuch](https://tungsten-carrot-2b4.notion.site/Projekttagebuch-1ad104ac2da58189ad61c4600e771cbd?pvs=4)

Eine PDF Übersicht über den Projektverlauf finden Sie hier:
- [Projektplan](docs/Projektplanung/ProjektPlan.pdf).
- [Projekttagebuch](docs/Projektplanung/Projekttagebuch).

## Starten
Via gradle das Projekt builden:
```shell
./gradlew build-cs108
```
Im Terminal zuerst den Server, dann den Client starten: 
```shell
Server: java -jar /build/libs/SettlersOfAsgard.jar server <listenport>
Client: java -jar /build/libs/SettlersOfAsgard.jar client <server-ip> <server-port>
```
Beachte: listenport und server-port müssen identisch sein.
Die server-ip ist entweder die lokale IP-Adresse des Servers im Netzwerk oder "localhost", wenn beides auf der gleichen Maschine läuft.

Eine Anleitung, wie das Spiel zu spielen ist finden Sie hier:
[Spielanleitung](docs/Spielbeschreibungen/GameManual.pdf).