# Settlers Of Asgard - Game Design

// einleitung

## Starten des Spiels

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

// hier lobby start usw machen

# Entities
Unterarten übernehmen Eigenschaften ihrer Überarten außer anders angegeben
## Player
**Beginn des Spiels**
Besitzt ein Startkapital an x Runen (noch festzulegen bzw änderbar)
Besitzt seinen eigenen **Shop** mit Listen der kaufbaren Strukturen. (festgelegte Designs und Effekte -> json)
	->UI: jede Struktur hat eine eigene Karte im Menu. nicht gekauft = grau, gekauft = farbig
Besitzt 0 Energie

**PlayerRound**
Spieler kann: 
- Tile kaufen (0 bis 3, günstig) -> chance auf Artefakt finden -> gefundene Artefakte werden in den 3 freien Artefact-Slots gespeichert und können verwendet werden
- Structure kaufen -> auf Tile platzieren (wenn Tile in seinem Besitz und ohne Structure/ Statue drauf)
-> ob eine gekaufte Structure sofort platziert werden muss kann noch entschieden werden -> ja wird sie!

Limit der Käufe ist außer bei Tiles nur das Runenkapital (änderbar)

## Structures
Werden zu Spielbeginn aus den json files geladen und jedem Spieler in seinem eigenen Shop zur Verfügung gestellt (kaufbar)
Jede Structure kann beliebig oft gekauft werden (änderbar) -> wenn gekauft, muss platziert werdne um noch mal gekauft werden zu können (da gekaufte die eine dazu bestimmte Karte in dem UI Menü belegt)
**Platzierbarkeit:** (sofern keine Anmerkung bei spezifischer Structure) auf Tile in Besitz platzierbar, solange diese frei ist (auf ihr keine Structure/ Statue steht)
Farmt Runen (oder Energie, je nach Angabe) jede Runde
Einige können genutzt werden (bsp Runentisch)
Einmal platziert nicht mehr vom Spieler bewegbar (außer eine Statue/ Entity greift darauf zu)
Limitierung Nutzen bei nutzbaren Structures, bsp. 3x pro Runde (änderbar)

### Findbare (aka bereits auf map/ fixedStructure)
(unterart der Structure)
Farmen jede Runde Runen
Nicht direkt nutzbar
### Baum
(Unterart der Structure)
Kann nicht gekauft werden, sondern nur von einer bestimmten Statue geschaffen werden. Es kann mehrere Bäume geben und sie können nur auf Tiles platziert werden, auf denen auch der Fluss ist (RiverTile true)

### Spezifische Structures
| **Struktur**      | **Nutzen**                                                                               | **Numbers** | **lvl 2**        | **lvl 3**                | **Beschränkung**                                               |
|-------------------|------------------------------------------------------------------------------------------| ----------- | ---------------- | ------------------------ |----------------------------------------------------------------|
| Runentisch        | Aktiv: Wandelt Energie in Runen um                                                       |             |                  |                          |                                                                |
| Statue            | Aktiv: Siehe Statuen                                                                     |             | Möglichkeit Deal | Möglichkeit Segen/ Fluch | 1x Nutzen pro Runde                                            |
|                   |                                                                                          |             |                  |                          |                                                                |
| Mimisbrunnr       | Gibt 1 Artefakt pro Runde                                                                |             |                  |                          | teuer - chance auf Falle (gestellt) statt Artefakt             |
|                   |                                                                                          |             |                  |                          |                                                                |
| Helgrindr         | Verhindert Debuffs                                                                       |             |                  |                          | (bsp. verhindert nur 1 Debuff pro Runde von Artefakt)          |
| Huginn und Muninn | Zeigt 1 Feld, welches der Spieler noch nicht besitzt mit einem Artefakt                  |             |                  |                          | Kann auch ein bereits belegtes Feld sein (?)                   |
| Rans Halle        | "Fängt mythische Fische" (Design/ Beschreibung, nicht code relevant) -> + Energie (flat) |             |                  |                          | Braucht Fluss                                                  |
| Surturs Schmiede  | Gibt jede Runde einen kleinen zufälligen buff für den Spieler (permanent)                |             |                  |                          | Nur auf Steinboden (Welt der Zwerge: Svartalfheim) platzierbar |
| Baum              | Gibt jede Runde eine größere Menge an Runen und + Energie (flat)                         |             |                  |                          | Nur auf Fluss platzierbar (nicht kaufbar)                      |
Aktiv: Spieler muss auf Structure klicken
Passiv: jede Runde ohne nötige Aktion (alle die nicht aktiv sind, sind passiv)

## Artefakt
Kann gefunden werden beim Kauf eines Feldes
Wird wenn gefunden in den 3 freien Slots auf der Hand des Spielers gespeichert und kann dann genutzt werden 
PlayerArtifact -> Beeinflusst Spieler direkt
FieldArtifact -> Beeinflusst Feld (oder Structure) direkt
Ein spezifisches Artefakt kann mehrfach gefunden werden (keine Begrenzung)

### Spezifische Artefakte
| **Artefakt**             | **Effekt**                                                                         | **Numbers** |
|--------------------------|------------------------------------------------------------------------------------| ----------- |
| Freyas Holzamulett       | + Effizienz Runen-Farmen auf einem Feld                                            |             |
| Freyrs Goldener Apfel    | + Energie (flat)                                                                   |             |
| Träne von Yggdrasil      | Anderer Spieler - Energie (flat)                                                   |             |
| Mjölnir-Charm            | + Chance auf Artefakte                                                             |             |
| Fenrirs Knochen          | + Effizienz Energy generation auf einem Feld                                       |             |
| Schatten von Hel         | Andere Spieler - Effizienz Runen Farmen                                            |             |
| Flamme von Muspelheim    | + Preissenkung kaufbares für nächste Runde                                         |             |
| Eissplitter von Nilfheim | Andere Spieler - Effizienz Energie Farmen                                          |             |
| Blut von Jörmungandr     | + Effizienz Farmen am Fluss                                                        |             |
| Asche von Surtur         | Andere Spieler - Preissteigerung kaufbares für nächste Runde                       |             |
| Odins Auge               | + überprüft ein Tile und alle angrenzenden auf Artefakte und deckt diese ggbf. auf |             |
Chance auf Artefakte: Genereller buff für den Spieler 


## Falle
Fenrirs Ketten
(Unterart von Artefakt)
Nutzung: FieldArtifact -> Wird auf ein von niemandem gekauftes Feld gesetzt (änderbar) -> wird dort zur ActiveTrap
**ActiveTrap:** Sobald Feld mit einer ActiveTrap gekauft wird, verliert der Spieler, der das Feld kauft Runen

## Feld
Farmt Runen jede Runde -> jedes Feld hat eine eigene resourceValue (Anzahl an Runen die gefarmt werden) -> wenn ein Feld eine hohe resourceValue hat, haben anliegende Felder das auch (änderbar, vielleicht schwer zu implementieren?)
Falls nicht geändert: sobald gekauft, zeigt es den anderen Spielern den resourceValue: fördert das strategische Element
-> Nein, das ist schwer zu implementieren und macht das Spiel zu kompliziert.

# Zukünftige Features
## Statuen 
(Ansehbar als Variante der Structure mit besonderen Regeln/ Nutzen)
Kaufbar, aber pro player max. 1 im Besitz möglich
Wenn gekauft = lvl 1
Farmt NICHTS
hochlevelbar: (teuer)
- lvl 2: kann verwendet werden: Deal (1x pro Runde) -> buff für debuff (evt. hinzufügen einer Begrenzung: nur 1x pro Spiel?)
- lvl 3: kann verwendet werden: Blessing (1x pro Runde?) -> starker buff (ebenso Begrenzung?)

### Spezifische Statuen
| **Statue**  | **Deal (lvl 2)**                                                                                                     | **Blessing (lvl 3)**                                                       | **Curse (lvl 3)**         | **World**                  |
| ----------- | -------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- | ------------------------- | -------------------------- |
| Jörmungandr | Zerstört 1 Struktur (random) von gewähltem Spieler - braucht Opfer: 1 eigene Struktur                                | -                                                                          | -                         | Midgard (humans)           |
| Freyr       | Lässt 1 Baum wachsen auf Feld mit Fluss - Energie = 0                                                                | Lässt Bäume wachsen auf jedem Feld mit Fluss                               | Überwuchert: - 1 Struktur | Alfheim (light, yellow)    |
| Dwarf       | Schmiede stellt nächstes Nutzen +1 Artefakt her -1 Struktur gibt diese Runde keine Runen                             | Nächstes Artefakt debuffed alle Spieler                                    | zerstört: -1 Schmiede     | Svartalfheim (bright grey) |
| Freyja      | Findet +1 Artefakt - kostet Runen                                                                                    | +1 kostenloses Feld auswählbar                                             | Conquers: -1 Feld         | Vanaheim (lush green)      |
| Hel         | Blockiert Statue ausgewählten Spielers für nächste Runde - Blockiert random Struktur nächste Runde (- Nutzen/ Runen) | anderer Spieler -1 Statue                                                  | Death: -1 Statue          | Helheim (dead)             |
| Nidhöggr    | Frisst einen Baum - füttern: -2 Artefakt                                                                             | Frisst alle Bäume auf der Map                                              | Frisst 2 Strukturen       | Nilfheim (ice)             |
| Loki        | Stellt 1 ausgewählten Spieler eine Falle - Stiehlt: -1 Artefakt                                                      | Stiehlt 1 anderen Spieler gewisse Anzahl an Runen und gibt sie dem Spieler | Stellt Falle: 2 Fallen    | Jotunheim (giants, brown)  |
| Surtr       | Für Feld mit Flammenschwert: ausgewählter Spieler -1 Struktur/ Statue - -1 Flammenschwert                            | + 2 Felder Muspelheim (Fire world) ohne Struktur                           | -1 Struktur/ Statue       | Muspelheim (fire)          |
| Thor        |                                                                                                                      |                                                                            |                           | Asgard                     |
Thor muss noch designed werden (eventuell)
Jörmungandr hat nur einen Deal