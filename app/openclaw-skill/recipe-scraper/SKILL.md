# LobsterClawe — Recipe Discovery Skill

## Skill Name
`lobsterclawe-recipes`

## Purpose
Search the web for authentic Indian recipes based on the user's mood, cuisine preference,
dietary goal, spice level, and household size. Return structured JSON that the app can
render directly — no prose, no markdown, no explanation.

## When This Skill Is Invoked
The app sends a message like:
> "Find 3 Indian recipes. Mood: quick_cook. Cuisines: South Indian, Punjabi.
>  Dietary goal: Eat Healthy. Spice: medium. Household size: 2. Return only JSON."

## Instructions

### Step 1 — Build search queries from the input
Extract mood, cuisines, dietary goal, and spice level from the message.
Map mood to constraints:
- `quick_cook` → "under 30 minutes" OR "easy quick"
- `weekend_mode` → "slow cooked" OR "elaborate" OR "restaurant style"
- `budget_friendly` → "cheap" OR "budget" OR "economical"
- `healthy` → "low calorie" OR "high protein" OR "nutritious"

For each cuisine in the list, build one search query:
- `{cuisine} recipe {mood_keyword} Indian homestyle`

Examples:
- `South Indian quick recipe easy homestyle`
- `Punjabi dal recipe budget friendly Indian`
- `Bengali fish curry weekend homestyle`

### Step 2 — Search using SearXNG
Use web_search for each query (search up to 3 queries total).
Prefer results from:
- hebbarskitchen.com
- indianhealthyrecipes.com
- vegrecipesofindia.com
- archanaskitchen.com
- cookwithmanali.com
- tarladalal.com

Avoid: YouTube links, food delivery apps, aggregator listicles with no actual recipe.

### Step 3 — Extract recipe data from results
For each result, extract:
- Recipe name / title
- Approximate cook time in minutes (if not stated, estimate based on dish type)
- Ingredients list with quantities
- Step-by-step cooking instructions (summarise if very long, keep all key steps)
- Approximate nutrition (estimate if not provided — use standard Indian food values)
- Tags: derive from ingredients and method (e.g. "vegan", "gluten-free", "high-protein")

### Step 4 — Validate and fill gaps
- If cook time is missing: estimate (dal = 30 min, biryani = 60 min, stir fry = 15 min)
- If nutrition is missing: estimate using standard USDA/NIN values for Indian ingredients
- If fewer than 3 recipes found: generate the remaining ones from your knowledge
  using the same cuisine and mood constraints — do NOT leave the array short
- Servings must match the household size from the input message

### Step 5 — Return JSON only
Return ONLY the JSON array below. No preamble. No explanation. No markdown code fences.
If you include anything other than the raw JSON array, the app will crash.

## Output Format

```
[
  {
    "title": "Tadka Dal",
    "summary": "A comforting everyday lentil dish tempered with cumin and garlic.",
    "cookTimeMinutes": 25,
    "servings": 2,
    "nutrition": {
      "calories": 310,
      "proteinG": 14,
      "carbsG": 42,
      "fatG": 8
    },
    "ingredients": [
      "200g toor dal",
      "1 medium tomato, chopped",
      "1 tsp cumin seeds",
      "2 cloves garlic, minced",
      "1/2 tsp turmeric",
      "1 tsp ghee",
      "salt to taste"
    ],
    "steps": [
      "Wash and pressure cook toor dal with turmeric and 2 cups water for 3 whistles.",
      "Heat ghee in a pan. Add cumin seeds and let them splutter.",
      "Add garlic and sauté for 30 seconds until golden.",
      "Add tomatoes and cook until soft, about 3 minutes.",
      "Pour the cooked dal into the pan. Mix well and simmer for 5 minutes.",
      "Season with salt. Serve hot with rice or roti."
    ],
    "tags": ["vegan", "high-protein", "gluten-free", "quick"]
  }
]
```

## Critical Rules
- Return EXACTLY 3 recipe objects in the array — never fewer, never more
- All prices and quantities must be realistic for India (grams, kg, pieces — not cups for solids)
- Spice level from the input must be respected: mild = no chilli, hot = generous chilli
- Tags must only include values from: vegan, vegetarian, non-vegetarian, gluten-free,
  dairy-free, high-protein, low-carb, low-calorie, quick, budget-friendly
- Never return the same recipe twice in one response
- Do not include source URLs in the output