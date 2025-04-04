# Netzwerkprotokoll für Settlers of Asgard (Programmierprojekt CS108 Gruppe3) v3.0.0

## 1. Einleitung
Dieses Dokument beschreibt das Netzwerkprotokoll für das rundenbasierte Strategiespiel Settlers of Asgard. Es basiert auf TCP und verwendet ein textbasiertes, befehlsorientiertes Format mit fester Befehlslänge. Alle Nachrichten bestehen aus Befehlen und Parametern. Zwischen jedem Befehl und Parameter muss ein Dollar-Zeichen (`$`) stehen.

## 2. Nachrichtenformat
Jede Nachricht hat folgendes Format:  
`<Befehl>$<Parameter1>$<Parameter2>$...`  
Beispiel:  
`BILD$PL01$Castle$0001$0002`
Dieser Befehl bedeutet, dass Spieler PL01 eine Struktur vom Typ "Castle" auf das Feld (1,2) bauen möchte.

**Feste Befehlslänge:** Jeder Befehl hat genau 4 Zeichen. Eine Ausnahme stellen die Nachrichten in der Fehlerbehandlung dar (`ERROR`, `OK`). Die Parameter haben eine variable Länge und werden durch Dollar-Symbole getrennt. Falls keine Parameter benötigt werden, bleibt die Nachricht nach dem Dollar-Symbol leer.

**Maximale Parameterlängen:**  
- **Spielername:** maximal 16 Zeichen.  
- **Nachricht:** maximal 200 Zeichen.  
- Bei Überschreitung dieser Längen wird ein Fehler (Fehlercode 103) zurückgegeben.

## 3. Befehle

### Spielverwaltung
| Befehl | Richtung | Beschreibung | Parameter | Beispiel |
|--------|----------|--------------|-----------|----------|
| `RGST` | **C→S** | Spieler verbindet sich erstmals mit Server | `Spielername` | `RGST$ThorOdinson` |
| `JOIN` | **C→S** | Spieler tritt Lobby bei | `Spielername$LobbyID` | `JOIN$ThorOdinson$SP01` |
| `LEAV` | **C→S** | Spieler verlässt Lobby | `Spielername$LobbyID` | `LEAV$ThorOdinson$SP01` |
| `LEAV` | **C→S** | Spieler verlässt Lobby | `Spielername$LobbyID` | `LEAV$ThorOdinson$SP01` |
| `EXIT` | **C→S** | Spieler disconnected | `Spielername$LobbyID` | `EXIT$ThorOdinson$SP01` |
| `CHAN` | **C→S** | Spieler möchte seinen Namen ändern | `Spielername` | `CHAN$LokiLaufeyson` |
| `CHTG` | **C→S** | Nachricht senden (global) | `SpielerID$Nachricht` | `CHTG$PL01$HelloWorld!` |
| `CHTP` | **C→S** | Nachricht senden (privat) | `SpielerID1$SpielerID2$Nachricht` | `CHTP$PL01$PL02$HelloWorld!` |
| `LIST` | **C→S** | Liste der Lobbies | | `LIST$` |
| `STRT` | **C→S** | Spiel starten | | `STRT$` |
| `STDN` | **S→C** | Nachricht an Clients: Server wird beendet| | `STDN$` |  
| `PING` | **C→S & S→C** | Verbindung prüfen | | `PING$` |
| `STAT` | **C→S** | Spielstatus abrufen | `SpielID` | `STAT$SP01` |
| `SYNC` | **C→S** | Synchronisation des Spielzustands anfordern | | `SYNC$` |

### Spielmechanik
| Befehl | Richtung | Beschreibung | Parameter | Beispiel |
|--------|----------|--------------|-----------|----------|
| `TURN` | **S→C** | Startet Zug | `SpielerID` | `TURN$PL01` |
| `ENDT` | **S→C** | Beendet Zug | `SpielerID` | `ENDT$PL01` |
| `BUYH` | **C→S** | Kauft ein Hexfeld | `SpielerID$X$Y` | `BUYH$PL01$0001$0002` |
| `BILD` | **C→S** | Baut Struktur | `SpielerID$Struktur$X$Y` | `BILD$PL01$Castle$0001$0002` |
| `UPGD` | **C→S** | Eine Struktur verbessern | `SpielerID$X$Y` | `UPGD$PL01$0001$0002` |
| `TRAD` | **C→S** | Handeln | `Spieler1ID$Spieler2ID$RessourcenID1$RessourcenID2$Anzahl1$Anzahl2` | `TRAD$PL01$PL02$R001$R002$0005$0020` |
| `BLNC` | **C→S** | Aktuelles Ressourcenkonto abrufen | `SpielerID` | `BLNC$PL01` |
| `RITU` | **C→S** | Ritual starten | `SpielerID$RessourcenID$Anzahl` | `RITU$SP01$R001$0005` |
| `BLES` | **C→S** | Segen erhalten | `SpielerID$GottID` | `BLES$PL01$G001` |
| `CURS` | **C→S** | Fluch wirken | `Spieler1ID$Spieler2ID$GottID` | `CURS$PL01$PL02$G002` |
| `ARTF` | **C→S** | Artefakt benutzen | `SpielerID$ArtefaktID` | `ARTF$PL01$AR01` |
| `FIND` | **C→S** | Artefakt suchen | `SpielerID$X$Y` | `FIND$PL01$0001$0002` |

## 4. Fehlerbehandlung
Eine allgemeine Fehlernachricht hat folgendes Format:  
`ERROR$<Fehlercode>$<Fehlermeldung>`  
Beispiel:  
`ERROR$100$Unbekannter Befehl`

### Spezifische Liste der Fehlercodes
| Fehlercode | Name | Beschreibung | Beispiel |
|------------|------|--------------|----------|
| 100 | Unbekannter Befehl | Tritt auf, wenn der empfangene Befehl nicht im Protokoll definiert ist. | `LEAV$ThorOdinson$SP01` |
| 101 | Falsche Parameteranzahl | Tritt auf, wenn ein Befehl zu wenige oder zu viele Parameter enthält. | `JOIN$ThorOdinson$SP01$PL01` |
| 102 | Ungültiger Parameter | Tritt auf, wenn ein Parameter nicht dem erwarteten Format entspricht. | `JOIN$12345$SP01` |
| 103 | Ungültige Länge | Tritt auf, wenn ein Befehl oder Parameter die maximal erlaubte Länge überschreitet. | `JOIN$ThorOdinson123456783456$SP01;` |
| 104 | Unzulässige Zeichen | Tritt auf, wenn unzulässige Zeichen (z. B. Semikolons) innerhalb eines Parameters gefunden werden. | `JOIN$Thor$Odinson$SP01` |
| 105 | Unbekannte SpielerID | Tritt auf, wenn eine SpielerID (oder Spielername) nicht existiert oder ungültig ist. | `TURN$PL999` |
| 106 | Nichtautorisierte Aktion | Tritt auf, wenn ein Spieler versucht, eine Aktion auszuführen, zu der er nicht berechtigt ist. | `STRT$PL02` |
| 107 | Spiel-ID nicht gefunden | Tritt auf, wenn eine angegebene Spiel-ID nicht existiert oder ungültig ist. | `JOIN$ThorOdinson$SP999` |

### Protokoll-Antwort bei erfolgreichen Befehlen
Wenn ein Befehl korrekt verarbeitet wurde, schickt der Server eine Bestätigung zurück, damit der Client weiß, dass alles erfolgreich war. Diese Nachricht hat folgendes Format:  
`OK$<Befehl>`  
Beispiel:  
`OK$JOIN`
