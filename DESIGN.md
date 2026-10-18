# Design contract

## Authority
The user explicitly requested shadcn style. This is a native translation of that established visual language, with Android/iOS input and window behavior. No alternative visual identity is introduced.

## Surface
Operate mode: a focused workspace setup form and a shared component gallery. The form has a header with project mark and theme control, a clear title and short explanation, vertically ordered fields, notification preferences as a dismissible sheet, review as a confirmation dialog, then a local success summary. The gallery adds independent Switch, Textarea, RadioGroup and Alert examples alongside the six form components.

## Visual system
- Zinc neutrals; near-black primary action on white, inverted in dark mode.
- Semantic color roles, thin borders, restrained 8dp control corners, no decorative gradients or illustration.
- Platform sans typography; 28sp screen title, 16sp field values, 14sp labels and secondary text.
- 24dp screen gutters, max content width 520dp, 20–24dp between field groups.
- Controls have at least 48dp interactive height. Text containers grow with font scale.
- Focus rings, visible checked/selected state, descriptive errors, progress and disabled state.

## Native behavior
Shared Foundation input and toggle semantics, Material3 sheets, and Compose Dialog for confirmation and searchable selection. The role picker uses a scrollable dialog so results remain available above the iOS keyboard. Native safe drawing and IME insets. State survives Android recreation using saveable state. Sheet changes are staged until saved; dismissal discards pending changes.

## Documentation surface
Read mode, retaining the same zinc palette and restrained borders. Desktop uses a persistent component directory, focused reading column and page contents; small screens use an accessible collapsible directory. The complete form and ten component pages embed the real shared Compose code compiled to Wasm. Kotlin examples, search, copy actions, theme selection and direct hash links support trying and adopting the components. Browser preview is identified separately from native device verification.

## Verification
Android emulator: 15 interaction tests passed, covering the complete form, keyboard, overlays, themes, saved-state restoration, the four added components, gallery navigation, Activity recreation and disabled/loading callbacks under accessibility activation. The four new component tests also passed in dark mode at 150% font scale; reviewed captures showed no clipping.

iOS: 13 native XCUITests passed on iPhone 17 Pro / iOS 26.5 in hosted macOS CI, with no failures, skips or retries. Coverage includes the complete form, keyboard Next/Done, search, sheets, dialogs, themes, reset, all four added components and gallery navigation. Physical devices, manual VoiceOver and larger Dynamic Type remain unverified.
