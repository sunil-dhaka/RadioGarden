# RadioGarden

I've been listening to [radio.garden](https://radio.garden) on my laptop for quite some time now -- it's a beautiful way to explore radio stations from around the world. But their Android app is riddled with ads, and that's just annoying. So here's the solution: a clean, ad-free Android client that does one thing well -- streams radio.garden stations in the background.

## Features

- Stream any radio.garden station by pasting its URL (channel or place link)
- Background playback with notification controls (play/pause/stop)
- Audio focus handling and noisy-audio detection (pauses on headphone disconnect)
- Persistent station library with Room database
- Set a default station, swipe-to-delete with undo
- Dark-only theme -- warm amber on deep charcoal
- Pre-loaded with two stations: Radio Aashiqanaa (Kanpur) and Marwar Radio (Pali)

## Architecture

Single-activity Compose app with manual dependency injection.

```
data/
  local/       Room database (stations table)
  remote/      OkHttp API client (channel metadata, stream URL resolution via 302 redirect)
  repository/  Combines local + remote, URL parsing
  preferences/ DataStore (default station, first launch flag)
playback/      Media3 MediaSessionService (ExoPlayer, audio focus, notification)
ui/
  theme/       Material 3 dark theme, Inter font (Google Fonts provider)
  screens/     HomeScreen, NowPlayingCard, StationItem, AddStationSheet
```

## Tech Stack

| Component | Library |
|-----------|---------|
| UI | Jetpack Compose + Material 3 |
| Playback | Media3 ExoPlayer + MediaSessionService |
| Persistence | Room |
| Preferences | DataStore |
| Network | OkHttp |
| Serialization | kotlinx.serialization |
| Fonts | Google Fonts (Inter) |

## How It Works

1. User pastes a `radio.garden` URL (e.g. `https://radio.garden/listen/marwar-radio/J5OrSNeF`)
2. App extracts the channel ID from the URL
3. Fetches metadata from `radio.garden/api/ara/content/channel/{id}`
4. Resolves the actual stream URL by following the 302 redirect from `radio.garden/api/ara/content/listen/{id}/channel.mp3`
5. Stores the station locally and streams via ExoPlayer

Place URLs (e.g. `/visit/pali/...`) are also supported -- if a place has multiple stations, a picker is shown.

## Build

Requires Android Studio or JDK 17+.

```bash
# Set JAVA_HOME if building from CLI
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build debug APK
./gradlew assembleDebug

# APK location
ls app/build/outputs/apk/debug/app-debug.apk
```

**Min SDK:** 26 (Android 8.0)
**Target SDK:** 36

## Install

Download the latest APK from [Releases](../../releases) or build from source.

```bash
adb install -r -t -g app-debug.apk
```

## License

MIT
