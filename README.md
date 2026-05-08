# LobsterClawe

LobsterClawe is an Android recipe-to-grocery assistant. It turns a user’s taste profile and mood into personalized Indian recipes, lets them customize recipes with AI, saves recipes offline, and fetches live grocery price comparisons through a self-hosted OpenClaw gateway.

## Problem

People who cook at home still have to bounce between multiple apps to answer two basic questions:

1. What should I cook tonight?
2. Where can I buy the ingredients without overpaying?

Recipe apps only solve discovery, grocery apps only solve purchasing, and general AI assistants usually do not have live Indian grocery price context. That friction leads to decision fatigue and often pushes users back to ordering out.

## Solution

LobsterClawe combines recipe generation and grocery price lookup in one mobile flow:

1. OpenRouter generates personalized recipes based on mood, cuisine preferences, dietary goal, spice level, and household size.
2. OpenClaw runs the grocery price agent on a self-hosted gateway.
3. SearXNG powers the live search layer used by the OpenClaw skill.
4. The app shows the recipe, lets users customize it, saves it locally, and pushes ingredients into the Grocery tab for price comparison.

The result is a single on-device workflow from “what should I cook?” to “where should I buy it?”

## Features

- Mood-based recipe feed: Quick Cook, Weekend Mode, Budget Friendly, and Healthy.
- Taste-profile onboarding with cuisine, dietary goal, spice level, and household size.
- AI recipe customization from the recipe detail screen.
- Ingredient handoff to the Grocery tab with live price lookup.
- Offline saved recipes using Room.
- Streaming AI chat for general cooking questions.
- Gateway connection testing from Settings.

## Tech Stack

- Kotlin
- Jetpack Compose and Material 3
- Room for offline saved recipes
- SharedPreferences for taste profile and onboarding state
- OkHttp and Gson for networking and JSON parsing
- OpenRouter for recipe generation, recipe customization, and chat
- OpenClaw for grocery price lookup
- SearXNG for live web search behind the OpenClaw skill

## Project Structure

```text
LobsterClawe/
├── app/
│   ├── src/main/java/com/lobsterclawe/
│   │   ├── ui/home/            recipe feed and mood selection
│   │   ├── ui/recipe/          recipe detail and customization
│   │   ├── ui/grocery/         ingredient price comparison
│   │   ├── ui/chat/            streaming chat assistant
│   │   ├── ui/saved/           offline saved recipes
│   │   ├── ui/settings/        taste profile and gateway test
│   │   └── network/            OpenRouter and OpenClaw clients
│   └── src/main/res/           app resources
├── openclaw-skill/
│   ├── recipe-scraper/         recipe generation skill
│   └── ingredients-scraper/    grocery price skill
├── SETUP.md                    step-by-step environment setup
└── README.md                   project overview
```

## Setup

Use the detailed guide in [SETUP.md](SETUP.md) for the full environment setup.

Important notes:

- `local.properties` must be present in the project root.
- The app reads `OPENCLAW_GATEWAY_URL`, `OPENCLAW_GATEWAY_TOKEN`, and `OPENROUTER_API_KEY` at build time.
- Missing values can break startup because they are wired directly into `BuildConfig`.
- The Android manifest already allows cleartext traffic to localhost and private LAN ranges for the local gateway.

### Skill installation

The skill files in this repo live under `openclaw-skill/` and should be copied into the OpenClaw workspace as skill folders:

```bash
mkdir -p ~/.openclaw/workspace/skills/lobsterclawe/recipe-scraper
mkdir -p ~/.openclaw/workspace/skills/lobsterclawe/ingredients-scraper

cp openclaw-skill/recipe-scraper/SKILL.md ~/.openclaw/workspace/skills/lobsterclawe/recipe-scraper/SKILL.md
cp openclaw-skill/ingredients-scraper/SKILL.md ~/.openclaw/workspace/skills/lobsterclawe/ingredients-scraper/SKILL.md
```

If you already have a working OpenClaw install, keep that setup and only add these two skill folders.

## Instructions

1. Make sure the OpenClaw gateway is running on the Ubuntu machine.
2. Make sure SearXNG is available for the OpenClaw skill.
3. Add your OpenRouter API key to OpenClaw.
4. Put your Ubuntu machine’s LAN IP, OpenClaw gateway token, and OpenRouter API key into `local.properties`.
5. Sync the Gradle project in Android Studio.
6. Run the app on a device with USB debugging enabled.
7. Open Settings and tap **Test Connection** to confirm the gateway is reachable.

## Usage

### 1. Onboarding

The app opens to onboarding the first time. Set your cuisine preferences, dietary goal, spice level, and household size, then save the profile.

### 2. Home

Open the Home tab and choose a mood:

- Quick Cook
- Weekend Mode
- Budget Friendly
- Healthy

The app asks OpenRouter for three personalized Indian recipes and renders them as cards.

### 3. Recipe detail

Tap a recipe card to open the detail view. From there you can:

- Save the recipe offline.
- Tap a tag such as vegan or low-carb to customize the recipe.
- Tap **Get Ingredients** to send the ingredient list into the Grocery tab.

### 4. Grocery

Open the Grocery tab and tap **Fetch Prices** to ask OpenClaw for live price comparisons for each ingredient.

### 5. Saved

Saved recipes are stored locally through Room and can be reopened without generating them again.

### 6. Chat

Use the Chat tab for general cooking help. Responses stream from OpenRouter so the assistant feels conversational.

### 7. Settings

Use Settings to adjust your taste profile and test the OpenClaw gateway connection.

## Architecture

```text
local.properties
    │
    ▼
BuildConfig
    │
    ├── OPENCLAW_GATEWAY_URL  ──▶  OpenClawClient  ──▶  OpenClaw gateway :18789
    │                                                     │
    │                                                     └── OpenClaw skill ──▶ SearXNG :8888
    │
    └── OPENROUTER_API_KEY    ──▶  OpenRouterClient ──▶  OpenRouter API
```

## Troubleshooting

- If the app says the gateway is unreachable, confirm the Ubuntu machine IP in `local.properties` matches the LAN IP shown by the server.
- If recipes or chat fail, confirm the OpenRouter API key is valid and added to OpenClaw.
- If grocery lookups fail, confirm OpenClaw is running and the skill folders were copied into the OpenClaw workspace.
- If you see a cleartext networking error, check the private IP and network security configuration.

## Demo

Presentation and demo assets are included in the repository:

- `RVCE_LobsterCLaw.pptx`
- `Lobsterclaw.apk`
- `LobsterClaw_AI_Disclosure.docx`
