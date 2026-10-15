# NetEase Cloud Music theme

This theme changes visual tokens only. It does not change layouts, component hierarchy, dimensions, navigation, playback, or business logic.

## Semantic color tokens

| Token | Light | Dark | Usage |
| --- | --- | --- | --- |
| `netease_brand` | `#EC4141` | `#FF5C5C` | Primary actions, selected and active states |
| `netease_on_brand` | `#FFFFFF` | `#FFFFFF` | Content placed on the brand color |
| `netease_brand_container` | `#FDECEC` | `#5C2020` | Restrained selected/hover backgrounds |
| `netease_on_brand_container` | `#8F1F1F` | `#FFDADA` | Content on a brand container |
| `netease_background` | `#FFFFFF` | `#121212` | Window and page background |
| `netease_surface` | `#FFFFFF` | `#1A1A1A` | Primary component surface |
| `netease_surface_subtle` | `#F5F5F5` | `#242424` | Grouping and inactive surfaces |
| `netease_surface_elevated` | `#FFFFFF` | `#2A2A2A` | Menus, dialogs, and elevated cards |
| `netease_text_primary` | `#333333` | `#F5F5F5` | Primary copy and titles |
| `netease_text_secondary` | `#666666` | `#B3B3B3` | Supporting information |
| `netease_text_tertiary` | `#999999` | `#808080` | Hints and disabled/weak information |
| `netease_divider` | `#E8E8E8` | `#333333` | Dividers and subtle boundaries |
| `netease_outline` | `#D6D6D6` | `#4A4A4A` | Input and component outlines |
| `netease_ripple` | `#1FEC4141` | `#29FF5C5C` | Pressed/ripple feedback |
| `netease_shadow` | `#1A000000` | `#66000000` | Lightweight elevation shadow |

Android resource qualifiers provide the dark values through `values-night`. Theme attributes map these semantic tokens to Material 3 roles, so screens should consume theme attributes such as `?attr/colorPrimary` and `?attr/colorOnSurface` instead of hard-coded values.

## Acceptance criteria

- Primary actions, active controls, selection indicators, links, and progress use the brand red or its restrained container variant.
- Light mode uses white for page/surface backgrounds, `#F5F5F5` only for visual grouping, `#333333` for primary text, `#666666` for secondary text, and `#E8E8E8` for dividers.
- Dark mode switches through `values-night` without flashes of light surfaces; it uses lifted near-black surfaces and the brighter red `#FF5C5C` for readable emphasis.
- Primary text/background and white/brand action pairs meet WCAG AA for normal text. Brand red is not used for small text on white; use the darker brand-container foreground for that case.
- Existing layouts, view dimensions, component hierarchy, navigation, playback behavior, and product-flavor boundaries remain unchanged.
- Cards and overlays retain moderate existing corners and low elevation; no decorative gradients or heavy shadows are introduced.
- Both `:app:assembleNormalDebug` and `:app:assembleFdroidDebug` succeed, and Android lint reports no new theme/resource errors.

## Constraints

- Do not hard-code these colors in layouts or Kotlin; add or reuse a semantic token.
- Do not use brand red as a large-area page background or for non-semantic decoration.
- Album artwork and artwork-derived player palettes may remain content-driven; the surrounding application chrome must return to semantic theme colors.
- User-selected wallpaper/adaptive accents remain an explicit opt-in. The default and Material theme accents are the NetEase brand colors.
- The black theme intentionally keeps its pure-black surface for AMOLED behavior while sharing brand, text, outline, and state roles with dark mode.
