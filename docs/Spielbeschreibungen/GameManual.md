# Settlers Of Asgard - Game Manual
In diesem Dokument wird beschrieben, wie man Schritt für Schitt das Spiel Settlers Of Asgard starten kann und eine Anleitung gegeben, wie eine Spielrunde abläuft.
Ebenso gibt es zusätzliche Erläuterungen zu den Entitäten, die speziell für dieses Spiel designed wurden.

## Starten des Spiels
### Start des Programms

Um ein Spiel zu starten muss ein Server gestartet werden und pro Spieler ein Client.
Als erstes mit `cd src/main/java` ins Hauptverzeichnis wechseln (falls noch nicht schon drin).
Dann ein Terminal öffnen und `./gradlew build-cs108` eingeben. Anschließend ins Verzeichnis build/libs mit
`cd build/libs` wechseln. Dort befindet sich nun eine Datei namens `settlersOfAsgard.jar`. Zum Starten des Servers:
- Eingabe im Terminal: `java -jar settlersOfAsgard.jar server <port>`.
- Der Port ist dabei eine Zahl > 1024, die wählbar ist. Ein Beispiel wäre 9000.
  Nun sollte der Server laufen. In einem neuen Terminal wieder ins Verzeichnis build/libs wechseln (s.o.) und den Client
  wie folgt starten:
- Eingabe im Terminal: `java -jar settlersOfAsgard.jar client <ip-adresse>:<port>`
- Zu beachten ist dass der Port die gleiche Zahl ist wie der beim Server angegebene Port und das die IP-adresse mit
  der der Maschine auf welcher der Server läuft übereinstimmt. Wenn Server und Client auf dem gleichen Rechner laufen, reicht es
  aus anstatt der IP-adresse der eigenen Maschine `localhost`zu verwenden.
### Lobby und Spielstart
Jeder Spieler befindet sich nach dem Start des Programms und der Verbindung zum Server nun auf der Startseite des Spiels. Als nächstes kann das Feld "**Start Game**" angeklickt werden, welches den Spieler zum nächsten Screen bringt.
Nun muss von einem Spieler eine Lobby erstellt werden:
- Im unteren **Eingabefeld**, unter dem Lobby-Menü, kann der Name der neuen Lobby angegeben werden.
- Es neben dem Eingabefeld ausgewählt werden, für wie viele Spieler die Lobby gedacht ist.
- Wenn beides festgelegt ist, wird die Lobby kreiert indem man auf "**create lobby**" direkt daneben drückt.
  Sobald eine Lobby erstellt wurde, können alle anderen Spieler dieser Lobby beitreten:
- Im Lobby-Menü kann eine bereits bestehende Lobby, wie etwa die, die gerade erstellt wurde, ausgewählt werden.
- Falls eine Lobby nicht angezeigt wird, obwohl diese erstellt wurde, kann der "**Refresh**"-Button über dem Menü verwendet werden, um die Lobbies neu zu laden.
- Wenn die gewünschte Lobby sichtbar ist, muss man lediglich diese auswählen und dann unter dem Menü auf "**join lobby**" klicken.

Sobald alle Spieler, die zusammen in einer Lobby spielen wollen, dieser beigetreten sind, kann der Host, also derjenige, der die Lobby erstellt hat, das Spiel starten:
- Der Host hat ein spezielles **Host-Menü** unterhalb vom Chat, in dem er auf das Feld "**Start Game**" drücken muss.
- Nun wird bei allen Spielern in der Lobby der Game-Screen geladen.

## Durchlauf einer Spielrunde
Die Spieler sind nacheinander dran. Sobald ein Spieler dran ist, het er verschiedene Möglichkeiten, vorzugehen um so viele Runen wie möglich bekommen zu können:
- Der Spieler kann **Felder** kaufen. Dazu muss auf ein freies Feld doppeld geklickt werden. Solange der Spieler genug Runen hat, um das Feld zu kaufen, gelangt dieses dadurch sofort in seinen Besitz. Felder im Besitz des Spielers oder anderer Spieler werden farblich gekennzeichnet. Man kann pro Player-Round ein Maximum an 3 Feldern kaufen.
- Nun kann der Spieler auch **Strukturen** kaufen. Diese befinden sich im unteren Menü in Form von Karten. Um eine zu kaufen muss der Spieler auf eine der Strukturen klicken und dann auf ein freies Feld klicken, dass er selbst besitzt. Das funktioniert natürlich nur, solange der Spieler genug Runen hat, um die Struktur zu bezahlen und auf dem Feld noch keine andere Struktur steht.
- Wenn man durch den Kauf eines Feldes ein **Artefakt** gefunden hat, wird dieses im unteren Menü auf einer der drei ersten Karten angezeigt. Dieses kann wie eine Struktur angeklickt werden. Um es zu verwenden, muss man dann ein belegtes Feld eines Gegners oder eines im eigenen Besitz anklicken, je nach Effekt des Artefaktes, um es zu verwenden.
- Wenn der Spieler frühzeitig seine Runde beenden möchte, kann er auch "end turn" klicken.

Über das Feld "Resource Overview" können jederzeit die Runen und Effekte aller Spieler eingesehen werden, durch die man seinen eigenen Fortschritt mit dem der anderen Spieler vergleichen kann.
Nach der fünften Game-Round wird schließlich das offizielle Leaderboard angezeigt und somit der Gewinner des Games entschieden.
Die Game-Round Nummer wird in der oberen Leiste auf dem Bildschirm angezeigt.

Zu **genaueren Angaben**, was diese Felder, Strukturen und Artefakte tun kann im unteren Teil "Elaborationen zum Spiel und dessen Entities" nachgesehen werden. Ebenso gibt es Erklärungen im Spiel selbst, die angezeigt werden, wenn mit der Maus über die Karten im unteren Menu des Spiel-Screens gehovert wird.

# Elaborationen zum Spiel und dessen Entities
Unterarten übernehmen Eigenschaften ihrer Überarten außer anders angegeben
## Player
**Beginn des Spiels**
Besitzt ein Startkapital an x Runen (noch festzulegen bzw änderbar)
Besitzt seinen eigenen **Shop** mit Listen der kaufbaren Strukturen und Statuen. (festgelegte Designs und Effekte -> json)
->UI: jede Struktur hat eine eigene Karte im Menu. nicht gekauft = grau, gekauft = farbig
Besitzt 0 Energie

**PlayerRound**
Spieler kann:
- Tile kaufen (0 bis 3, günstig) -> chance auf Artefakt finden -> gefundene Artefakte werden in den 3 freien Artefact-Slots gespeichert und können verwendet werden
- Structure kaufen -> auf Tile platzieren (wenn Tile in seinem Besitz und ohne Structure/ Statue drauf)
  -> eine gekaufte Structure muss sofort platziert werden
- Artefakte verwenden -> auf ein eigenes oder gegnerisches Feld

Limit der Käufe ist außer bei Tiles nur das Runenkapital

## Structures
Werden zu Spielbeginn geladen und jedem Spieler in seinem eigenen Shop zur Verfügung gestellt (kaufbar)
Jede Structure kann beliebig oft gekauft werden -> wenn gekauft, muss sie platziert werden
**Platzierbarkeit:** (sofern keine Anmerkung bei spezifischer Structure) auf Tile in Besitz platzierbar, solange diese frei ist (auf ihr keine Structure/ Statue steht)
Farmt Runen (oder Energie, je nach Angabe) jede Runde
Einige können genutzt werden (bsp. Runentisch)
Einmal platziert nicht mehr vom Spieler bewegbar (außer eine Statue/ Entity greift darauf zu)
### Findbare (aka bereits auf map/ fixedStructure)
(unterart der Structures)
Farmen jede Runde Runen
Nicht direkt nutzbar
### Baum
(Unterart der Structure)
Kann nicht gekauft werden, sondern nur von einer bestimmten Statue geschaffen werden. Es kann mehrere Bäume geben und sie können nur auf Tiles platziert werden, auf denen auch der Fluss ist (RiverTile true)
-> ist Teil eines zukünftig geplanten Features (siehe "Zukünftig geplante Features" weiter unten)
### Spezifische Structures
| **Struktur**                 | **Nutzen**                                                                | **Numbers** | **lvl 2**        | **lvl 3**                | **Beschränkung**                                               |
| ---------------------------- | ------------------------------------------------------------------------- | ----------- | ---------------- | ------------------------ | -------------------------------------------------------------- |
| Runentisch                   | Aktiv: Wandelt Energie in Runen um                                        |             |                  |                          |                                                                |
| Statue (zukünftiges Feature) | Aktiv: Siehe Statuen                                                      |             | Möglichkeit Deal | Möglichkeit Segen/ Fluch | 1x Nutzen pro Runde                                            |
|                              |                                                                           |             |                  |                          |                                                                |
| Mimisbrunnr                  | Gibt 1 Artefakt pro Runde                                                 |             |                  |                          | teuer - chance auf Falle (gestellt) statt Artefakt             |
|                              |                                                                           |             |                  |                          |                                                                |
| Helgrindr                    | Verhindert Debuffs                                                        |             |                  |                          | (bsp. verhindert nur 1 Debuff pro Runde von Artefakt)          |
| Huginn und Muninn            | Zeigt 1 Feld, welches der Spieler noch nicht besitzt mit einem Artefakt   |             |                  |                          | Kann auch ein bereits belegtes Feld sein (?)                   |
| Rans Halle                   | "Befreie Seelen" -> gibt + Energie (flat)                                 |             |                  |                          | Braucht Fluss                                                  |
| Surturs Schmiede             | Gibt jede Runde einen kleinen zufälligen buff für den Spieler (permanent) |             |                  |                          | Nur auf Steinboden (Welt der Zwerge: Svartalfheim) platzierbar |
| Baum                         | Gibt jede Runde eine größere Menge an Runen und + Energie (flat)          |             |                  |                          | Nur auf Fluss platzierbar (nicht kaufbar)                      |
Aktiv: Spieler muss auf Structure klicken
Passiv: jede Runde ohne nötige Aktion (alle die nicht aktiv sind, sind passiv)
## Artefakt
Kann gefunden werden beim Kauf eines Feldes
Wird wenn gefunden in den 3 freien Slots auf der Hand des Spielers gespeichert und kann dann genutzt werden (Zur Nutzung muss es auf ein Feld angewendet werden)
Ein spezifisches Artefakt kann mehrfach gefunden werden (keine Begrenzung)

### Spezifische Artefakte
| **Artefakt**             | **Effekt**                                                                      | **Numbers** |
|--------------------------|---------------------------------------------------------------------------------| ----------- |
| Freyas Holzamulett       | + Effizienz Runen-Farmen auf einem Feld                                         |             |
| Freyrs Goldener Apfel    | + Energie (flat)                                                                |             |
| Träne von Yggdrasil      | Anderer Spieler - Energie (flat)                                                |             |
| Mjölnir-Charm            | + Chance auf Artefakte                                                          |             |
| Fenrirs Knochen          | + Effizienz Energy generation auf einem Feld                                    |             |
| Schatten von Hel         | Andere Spieler - Effizienz Runen Farmen                                         |             |
| Flamme von Muspelheim    | + Preissenkung kaufbares für nächste Runde                                      |             |
| Eissplitter von Nilfheim | Andere Spieler - Effizienz Energie Farmen                                       |             |
| Blut von Jörmungandr     | + Effizienz Farmen am Fluss                                                     |             |
| Asche von Surtur         | Andere Spieler - Preissteigerung kaufbares für nächste Runde                    |             |
| Odins Auge               | + überprüft ein Tile und alle angrenzenden auf Fallen und deckt diese ggbf. auf |             |
Chance auf Artefakte: Genereller buff für den Spieler


## Falle
Fenrirs Ketten
(Unterart von Artefakt)
Nutzung: FieldArtifact -> Wird auf ein von niemandem gekauftes Feld gesetzt (änderbar) -> wird dort zur ActiveTrap
**ActiveTrap:** Sobald Feld mit einer ActiveTrap gekauft wird, verliert der Spieler, der das Feld kauft Runen

## Feld
Farmt Runen jede Runde -> jedes Feld hat eine resourceValue (Anzahl an Runen die gefarmt werden)

# Zukünftig geplante Features

- Ebenso kann der Spieler eine der verfügbaren **Statuen** kaufen. Dazu muss er auf die letzte Karte im unteren Menü drücken. Dadurch geht ein Fenster auf mit der Auswahl an Statuen. Nun kann man sich für eine dieser Statuen entscheiden, diese anklicken und auf ein Feld im Besitz des Spielers ziehen, wodurch sie gekauft und platziert wird. Dieselben Bedingungen wie bei den Strukturen müssen dafür erfüllt sein.
- Zukünftig wird es auch die Möglichkeit geben, Strukturen mit einer Funktion und die gewählte Statue aktiv verwenden zu können, zusätzlich zu dem Passiven Farmen von Runen, welches sie gewährleisten.

### Statue
(Ansehbar als Variante der Structure mit besonderen Regeln/ Nutzen)
Kaufbar, aber pro player max. 1 im Besitz möglich
Wenn gekauft = lvl 1
Farmt NICHTS
hochlevelbar: (teuer)
- lvl 2: kann verwendet werden: Deal (1x pro Runde) -> buff für debuff (evt. hinzufügen einer Begrenzung: nur 1x pro Spiel?)
- lvl 3: kann verwendet werden: Blessing (1x pro Runde?) -> starker buff (ebenso Begrenzung?)


### Spezifische Statuen
| **Statue**  | **Deal (lvl 2)**                                                                                                     | **Blessing (lvl 3)**                                                       | **Curse (lvl 3)**         | **World**                  |
| ----------- |:---------------------------------------------------------------------------------------------------------------------| -------------------------------------------------------------------------- | ------------------------- | -------------------------- |
| Jörmungandr | Zerstört 1 Struktur (random) von gewähltem Spieler - braucht Opfer: 1 eigene Struktur                                | -                                                                          | -                         | Midgard (humans)           |
| Freyr       | Lässt 1 Baum wachsen auf Feld mit Fluss - Energie = 0                                                                | Lässt Bäume wachsen auf jedem Feld mit Fluss                               | Überwuchert: - 1 Struktur | Alfheim (light, yellow)    |
| Dwarf       | Schmiede stellt nächstes Nutzen +1 Artefakt her -1 Struktur gibt keine Runen mehr                                    | Nächstes Artefakt debuffed alle Spieler                                    | zerstört: -1 Schmiede     | Svartalfheim (bright grey) |
| Freyja      | Findet +1 Artefakt - kostet Runen                                                                                    | +1 kostenloses Feld auswählbar                                             | Conquers: -1 Feld         | Vanaheim (lush green)      |
| Hel         | Blockiert Statue ausgewählten Spielers für nächste Runde - Blockiert random Struktur nächste Runde (- Nutzen/ Runen) | anderer Spieler -1 Statue                                                  | Death: -1 Statue          | Helheim (dead)             |
| Nidhöggr    | Frisst einen Baum - füttern: -2 Artefakt                                                                             | Frisst alle Bäume auf der Map                                              | Frisst 2 Strukturen       | Nilfheim (ice)             |
| Loki        | Klaut ein Artefakt von dir und einem ausgewählten Spieler und vertauscht sie                                         | Stiehlt 1 anderen Spieler gewisse Anzahl an Runen und gibt sie dem Spieler | Stellt Falle: 2 Fallen    | Jotunheim (giants, brown)  |
| Surtr       | Für Feld mit Flammenschwert: ausgewählter Spieler -1 Struktur/ Statue - -1 Flammenschwert                            | + 2 Felder Muspelheim (Fire world) ohne Struktur                           | -1 Struktur/ Statue       | Muspelheim (fire)          |
| Thor        |                                                                                                                      |                                                                            |                           | Asgard                     |
Thor muss noch designed werden (gerne Ideen her)
Jörmungandr hat nur einen Deal
