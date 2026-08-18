# IGDB API Search Examples

## Relevant links

API documentation located [here](https://api-docs.igdb.com/#examples)
Search specs [here](https://api-docs.igdb.com/#search-1)

Examples of how to use the IGDB API (v4) for game search features, as discussed during the development of the "Add Game" screen.

## 1. Simple Relevance Search
Best for finding a specific game title. Uses the `search` keyword.
- **Endpoint:** `POST https://api.igdb.com/v4/games`
- **Query:**
```text
fields name, summary, cover.url;
search "God of War";
limit 10;
```
*Note: Results are ordered by relevance. You cannot use `where` or `sort` with the `search` keyword in this endpoint.*

## 2. Fuzzy / "Search-as-you-type"
Best for auto-complete and partial matches. Uses a `where` clause with the case-insensitive operator (`~`) and a wildcard (`*`).
- **Endpoint:** `POST https://api.igdb.com/v4/games`
- **Query:**
```text
fields name, cover.url, first_release_date;
where name ~ "Starcr"*;
sort total_rating_count desc;
limit 50;
```
*Note: This allows combining search with sorting (e.g., by popularity) and additional filters.*

## 3. Global Multi-category Search
Useful for searching across games, characters, platforms, etc., simultaneously.
- **Endpoint:** `POST https://api.igdb.com/v4/search`
- **Query:**
```text
fields *;
search "Sonic";
limit 10;
```

## 4. Technical Features Metadata
To find games with specific attributes (e.g., 4K, HDR, Cross-platform).
- **Endpoint:** `POST https://api.igdb.com/v4/game_version_features`
- **Query:**
```text
fields game.name, feature.name, value.note;
where game = 12345;
```

---

### Authentication Headers
All requests require:
- `Client-ID: [YOUR_CLIENT_ID]`
- `Authorization: Bearer [YOUR_ACCESS_TOKEN]`
- `Accept: application/json`
