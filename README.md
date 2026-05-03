# Console Collector

Pokemon-style retro console collection app: photograph a console, get a holographic collectible card.

## Architecture

Monorepo with three top-level concerns:

| Path | Stack | Purpose |
| --- | --- | --- |
| `backend/` | NestJS + Prisma + PostgreSQL + JWT | REST API: auth, console catalog, game catalog, collection, vision recognition, sharing. |
| `android/` | Kotlin + Jetpack Compose + CameraX + Retrofit | Native Android client. |
| `data/consoles.json` | Hand-curated catalog | 80+ console models (Sony / Microsoft / Nintendo / Sega / Atari / handhelds), 1972 → 2025, with rarity tiers. |
| `data/games.json` | Hand-curated catalog | 70+ games across major franchises (Halo, Zelda, Mario, Final Fantasy, Metal Gear, etc.) with rarity tiers and platform tags. |

Vision recognition is performed via [fal.ai](https://fal.ai) (any-llm vision endpoint, defaults to Gemini Flash 1.5). The backend builds a system prompt asking the model to identify the console (or game disc/cartridge/box) and return structured JSON, then fuzzy-matches the result against the local catalog. If [IGDB](https://api-docs.igdb.com/) credentials (`IGDB_CLIENT_ID` + `IGDB_CLIENT_SECRET`) are set, unmatched game titles are imported from IGDB on demand (cover art, release year, summary).

## Running locally

Requirements: Docker, Docker Compose, JDK 17, Android SDK 34, a `FAL_API_KEY` from https://fal.ai.

```bash
# 1. Backend + Postgres
cp backend/.env.example backend/.env  # then edit FAL_API_KEY etc.
docker compose up --build              # starts postgres + backend on :3000
docker compose exec backend npx prisma migrate deploy
docker compose exec backend npm run seed

# 2. Android — open `android/` in Android Studio, or:
cd android
echo "sdk.dir=$ANDROID_HOME" > local.properties
./gradlew assembleDebug    # produces app/build/outputs/apk/debug/app-debug.apk
```

The Android emulator reaches the host machine at `http://10.0.2.2:3000`. To override (e.g. for a real device on LAN), put `API_BASE_URL=http://192.168.x.x:3000/` in `android/local.properties`.

## Backend API surface

Swagger UI is auto-generated at `http://localhost:3000/api/docs`.

| Method | Path | Auth | Notes |
| --- | --- | --- | --- |
| `POST` | `/api/auth/register` | – | `email`, `password`, `displayName`, optional `city`, `country`. |
| `POST` | `/api/auth/login` | – | Returns `{ accessToken, user }`. |
| `GET`  | `/api/auth/me` | JWT | Current user. |
| `PATCH`| `/api/users/me` | JWT | Update profile fields. |
| `GET`  | `/api/consoles` | – | Catalog (`?manufacturer=`, `?search=`). |
| `POST` | `/api/recognize/console` | JWT | Body: `{ images: [data-uri or url, …] }`. Returns recognized console + suggested catalog id. |
| `POST` | `/api/recognize/game` | JWT | Same body shape. Returns recognized game title, platform, region, edition + suggested catalog id (with on-demand IGDB import when configured). |
| `GET`  | `/api/games` | – | Game catalog (`?platform=`, `?search=`). |
| `GET`  | `/api/games/:slug` | – | Single game lookup. |
| `GET`  | `/api/collection` | JWT | User's console items. |
| `POST` | `/api/collection` | JWT | Add a console item. |
| `PATCH`| `/api/collection/:id` | JWT | Update status / notes. |
| `DELETE`| `/api/collection/:id` | JWT | Remove. |
| `GET`  | `/api/collection/stats` | JWT | Totals + rarity breakdown + catalog completion. |
| `GET`  | `/api/collection/games` | JWT | User's game items. |
| `POST` | `/api/collection/games` | JWT | Add a game item (`gameId`, `platformSlug`, optional `edition`, `region`). |
| `PATCH`| `/api/collection/games/:id` | JWT | Update status / edition / region / notes. |
| `DELETE`| `/api/collection/games/:id` | JWT | Remove. |
| `GET`  | `/api/collection/games/stats` | JWT | Game totals + rarity / platform breakdown. |
| `POST` | `/api/share/collection` | JWT | Generate public link to your collection (consoles + games). |
| `POST` | `/api/share/item/:itemId` | JWT | Generate public link to a single console card. |
| `POST` | `/api/share/game-item/:itemId` | JWT | Generate public link to a single game card. |
| `GET`  | `/api/share/collection/:token` | – | Public resolve (returns owner, items, games). |
| `GET`  | `/api/share/item/:token` | – | Public resolve (single console card). |
| `GET`  | `/api/share/game-item/:token` | – | Public resolve (single game card). |

## Card rarity → visual treatment

| Rarity | Treatment |
| --- | --- |
| COMMON | Matte slate gradient. |
| UNCOMMON | Soft green sheen. |
| RARE | Animated blue holographic sweep. |
| EPIC | Animated rainbow sweep + outer glow. |
| LEGENDARY | Gold/red shimmer, intense rainbow sweep. |

See `android/app/src/main/kotlin/com/virt92/consolecollector/ui/collection/ConsoleCard.kt`.

## Roadmap (post-MVP)

- Achievement unlock logic + UI.
- Marketplace (`FOR_SALE`, `FOR_TRADE`, `GIVING_AWAY` already modelled in DB).
- Friend graph + collection comparison.
- Push notifications.
- Image hosting / CDN (currently photos are sent inline as base64 data URIs).
- iOS client (Compose Multiplatform candidate).
