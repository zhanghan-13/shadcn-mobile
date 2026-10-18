# shadcn-mobile

<!-- impeccable:product-schema 1 -->

## Platform
adaptive

## Stack
User requested Android Compose and initial Android/iOS support. Compose Multiplatform shares Kotlin UI and form logic; Android uses a native Activity and iOS uses a thin SwiftUI/UIKit host. The documentation website runs the same components through Kotlin/Wasm and documents their Kotlin APIs.

## Product Purpose
An executable prototype of six shadcn-inspired native components: Button, Input, Checkbox, Combobox, Dialog and Sheet, connected as one complete form.

## Users
Developers evaluating the visual language and native behavior of a mobile component library.

## Capabilities and Constraints
The user authorized implementation and emulator testing without further routine confirmation. The sample workflow is a locally simulated workspace setup (implementation assumption). It has no backend, account creation or data transmission. Android can be built and tested on this Windows workstation. Shared Kotlin compiles for iOS here; Apple framework linking, the Swift host and simulator validation require macOS with Xcode.

The user authorized a public GitHub repository and a GitHub Actions/Pages documentation workflow. The initial hold on commits and pushes was lifted by the user's request to polish the Chinese documentation, commit and push. Hosted workflow results and publication must be verified separately from local checks.

GitHub's final API response resolves that repository to `zhanghan-13/shadcn-mobile`; the signed-in user's current login is also `zhanghan-13`. Repository links and the local remote use this canonical address.

## Brand Commitments
Project name: shadcn-mobile. Preserve the restrained black/white shadcn visual language, composable APIs and editable source.

## Accessibility & Inclusion
Minimum 48dp interactive targets, semantic labels and roles, font scaling, keyboard interaction, safe areas, input-method insets and dark mode.
