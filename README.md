# UniProject

Bibliotečki informacioni sistem za evidenciju rada biblioteke.

## Opis projekta

Aplikacija omogućava bibliotekaru da vodi evidenciju o knjigama, autorima, kategorijama, članovima biblioteke i zaduženjima.

Bibliotekar se registruje i prijavljuje u sistem, dok se članovi biblioteke evidentiraju kao podaci i nemaju korisničke naloge.

## Funkcionalnosti

- Registracija i prijava bibliotekara
- CRUD operacije za knjige, autore i kategorije
- Evidencija, izmena i deaktivacija članova
- Zaduživanje i vraćanje knjiga
- Provera dostupnih primeraka
- Pregled zakasnelih zaduženja
- Pretraga knjiga po naslovu, autoru, kategoriji ili ISBN broju
- Pregled istorije zaduženja člana
- JWT zaštita REST API-ja

## Struktura baze

Baza `library_db` sadrži sledeće tabele:

- `author`
- `category`
- `book`
- `member`
- `loan`
- `librarian`

Glavne relacije:

- Autor 1—N Knjiga
- Kategorija 1—N Knjiga
- Član 1—N Zaduženje
- Knjiga 1—N Zaduženje

SQL skripta se nalazi u `backend/db/library_db.sql`.

## Tehnologije

### Backend

- Java 25
- Spring Boot
- Spring Data JPA
- Spring Security
- JWT
- Maven

### Frontend

- Angular
- Angular Material
- TypeScript

### Baza podataka

- MySQL

## Pokretanje projekta

### 1. Baza podataka

Napraviti MySQL bazu:

```sql
CREATE DATABASE library_db;
```

Zatim izvršiti SQL skriptu:

```text
backend/db/library_db.sql
```

### 2. Backend konfiguracija

Kopirati fajl:

```text
backend/src/main/resources/application.properties.example
```

i preimenovati kopiju u:

```text
application.properties
```

U kopiranom fajlu podesiti `spring.datasource.username` i `spring.datasource.password` prema lokalnoj MySQL konfiguraciji.

### 3. Pokretanje backenda

U direktorijumu `backend` pokrenuti:

```bash
.\mvnw.cmd spring-boot:run
```

Backend je dostupan na:

```text
http://localhost:8000
```

### 4. Pokretanje frontenda

U direktorijumu `frontend` pokrenuti:

```bash
npm install
npm start
```

Aplikacija je dostupna na:

```text
http://localhost:4200
```