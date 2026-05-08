# LobsterClawe — OpenClaw Setup Guide

Complete step-by-step guide to get OpenClaw and SearXNG wired into the LobsterClawe Android app.

---

## Prerequisites

- Ubuntu machine (the "server") with OpenClaw already installed
- Android phone on the same WiFi router (bands don't matter as long as it's the same router)
- Android Studio installed on your dev machine
- OpenRouter account at https://openrouter.ai

---

## Step 1 — Verify OpenClaw is running

SSH into your Ubuntu machine and check:

```bash
openclaw gateway status
```

You should see the gateway running on port `18789`. If not:

```bash
openclaw gateway run
# or as a background service:
systemctl --user start openclaw-gateway
```

---

## Step 2 — Verify SearXNG is running

```bash
curl http://localhost:8888/search?q=test&format=json
```

You should get a JSON response with search results. If SearXNG is not running:

```bash
# If installed via Docker:
docker start searxng

# Check logs if it fails:
docker logs searxng
```

Make sure SearXNG is configured to allow JSON format output. In your SearXNG `settings.yml`:

```yaml
search:
  formats:
    - html
    - json   # ← this must be present
```

Restart SearXNG after any config change:

```bash
docker restart searxng
```

---

## Step 3 — Get your Gateway Token

Run this on your Ubuntu machine:

```bash
openclaw dashboard
```

This prints a URL like:
```
http://127.0.0.1:18789/#token=8704c2742c297d38490608641
```

Your token is everything after `#token=`:
```
8704c2742c297d38490608641
```

Save this — you'll need it in Step 6.

Alternatively, read it directly from config:

```bash
cat ~/.openclaw/openclaw.json | python3 -c \
  "import json,sys; print(json.load(sys.stdin)['gateway']['auth']['token'])"
```

---

## Step 4 — Install the skills

Copy both skill files into the OpenClaw workspace:

```bash
# Create the skill folders if they don't exist
mkdir -p ~/.openclaw/workspace/skills/lobsterclawe/recipe-scraper
mkdir -p ~/.openclaw/workspace/skills/lobsterclawe/ingredients-scraper

# Copy both skills from this repo
cp openclaw-skill/recipe-scraper/SKILL.md ~/.openclaw/workspace/skills/lobsterclawe/recipe-scraper/SKILL.md
cp openclaw-skill/ingredients-scraper/SKILL.md ~/.openclaw/workspace/skills/lobsterclawe/ingredients-scraper/SKILL.md
```

Verify they're in place:

```bash
find ~/.openclaw/workspace/skills/lobsterclawe -maxdepth 2 -name SKILL.md
# Should show one SKILL.md under recipe-scraper and one under ingredients-scraper
```

Restart the gateway so it picks up the new skills:

```bash
systemctl --user restart openclaw-gateway
# or:
openclaw gateway restart
```

---

## Step 5 — Add your OpenRouter API key to OpenClaw

```bash
openclaw models auth paste-token --provider openrouter
```

When prompted, paste your OpenRouter API key (starts with `sk-or-...`).
Get one at https://openrouter.ai/keys if you don't have one.

Verify it worked:

```bash
openclaw models status
```

You should see OpenRouter listed as an authenticated provider.

---

## Step 6 — Find your Ubuntu machine's local IP

```bash
ip addr show | grep "inet " | grep -v 127.0.0.1
```

Look for a line like:
```
inet 192.168.1.45/24 brd 192.168.1.255 scope global wlan0
```

Your IP is `192.168.1.45` (the part before the `/24`). Write it down.

Quick sanity check — from your phone's browser, open:
```
http://192.168.1.45:18789
```
If you see any response (even an error page), the phone can reach OpenClaw. If it times out, the phone and Ubuntu machine are on different networks.

---

## Step 7 — Configure the Android app

Open the project in Android Studio. Find or create `local.properties` in the project root (same folder as `settings.gradle.kts`):

```properties
# local.properties — never commit this file, it's in .gitignore
sdk.dir=/path/to/your/Android/sdk

OPENCLAW_GATEWAY_URL=http://192.168.1.45:18789
OPENCLAW_GATEWAY_TOKEN=8704c2742c297d38490608641
OPENROUTER_API_KEY=sk-or-your-key-here
```

Replace:
- `192.168.1.45` → your actual Ubuntu machine IP from Step 6
- `8704c2742c297d38490608641` → your actual gateway token from Step 3
- `sk-or-your-key-here` → your actual OpenRouter API key

---

## Step 8 — Build and run

In Android Studio:

1. Click **Sync Project with Gradle Files** (elephant icon in toolbar)
2. Make sure your phone is connected via USB with USB debugging enabled
3. Click **Run** (green play button)
4. Go to the **Settings** tab in the app
5. Tap **Test Connection** — it should show **Connected ✓**

---

## Step 9 — Test the full flow

1. **Recipe test** — Go to Home, select a mood, tap the refresh button. You should see 3 recipe cards load.

2. **Grocery test** — Open any recipe, tap **Get Ingredients**. Go to the Grocery tab and tap **Fetch Prices**. After 10–30 seconds (SearXNG search takes time), you should see price comparisons for each ingredient.

---

## Troubleshooting

### "CLEARTEXT communication not permitted"
The `network_security_config.xml` is missing or the IP in `local.properties` doesn't match the private IP range allowed. Make sure `192.168.x.x` / `10.x.x.x` is in the config.

### "Connected" in Settings but Grocery shows error
The skill files aren't loaded by OpenClaw. Repeat Step 4 and restart the gateway.

### SearXNG returns empty results
- Check SearXNG is running: `curl http://localhost:8888/search?q=test&format=json`
- Check `json` is in the `search.formats` list in SearXNG `settings.yml`
- Some engines may be rate-limited — wait a few minutes and retry

### Gateway token mismatch on dashboard
In the OpenClaw dashboard UI, paste **only** the token string — not the full URL. The token is the part after `#token=` in the dashboard URL.

### App builds but crashes on launch
Check `local.properties` has all three keys. A missing key causes a null BuildConfig field which can crash on startup.

### Price results are empty / all stores missing
SearXNG may be blocking requests from OpenClaw. Check SearXNG logs:
```bash
docker logs searxng --tail 50
```
Also verify the `SEARXNG_URL` used by OpenClaw points to `http://localhost:8888`.

---

## Architecture reference

```
local.properties
    │
    ▼
BuildConfig (compile-time)
    │
    ├── OPENCLAW_GATEWAY_URL  ──▶  OpenClawClient.kt  ──▶  OpenClaw :18789
    │                                                            │
    │                                                       SKILL-grocery.md
    │                                                       SKILL-recipes.md
    │                                                            │
    │                                                       SearXNG :8888
    │
    └── OPENROUTER_API_KEY    ──▶  OpenRouterClient.kt ──▶  openrouter.ai
```

---

*LobsterClawe v1 · May 2026*