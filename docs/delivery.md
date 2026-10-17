# GitHub and documentation delivery

## Repository

- Public repository created: [hanzhang-13/shadcn-mobile](https://github.com/hanzhang-13/shadcn-mobile).
- Local `origin` points to `https://github.com/hanzhang-13/shadcn-mobile.git`.
- The repository was originally created through the `AZCodingAccount/shadcn-mobile` address. Final GitHub API verification resolves it to `hanzhang-13/shadcn-mobile` (repository ID `1423427936`), with the signed-in owner also reported as `hanzhang-13`. Local links use that canonical address.
- The owner has authorized the first commit and push after polishing the Chinese documentation. The earlier hold is lifted.
- GitHub CLI is authenticated under the current login with the approved `workflow` scope, so workflow files can be pushed.
- Hosted workflow execution and Pages publication require separate verification after pushing. Local build and syntax checks do not establish that hosted jobs pass.

## Prepared pipeline

| Workflow | Runs | Output |
| --- | --- | --- |
| Android | Relevant PRs, pushes to main, manual dispatch | Debug APK, 10 shared tests, 8 emulator interaction tests and screenshots |
| iOS | Relevant PRs, pushes to main, manual dispatch | Linked device/simulator frameworks, built Swift host, simulator startup smoke test and screenshot |
| Docs and Pages | Relevant PRs, pushes to main, manual dispatch | Static Chinese docs with real shared Compose/Wasm demos; main-only Pages deployment |

The iOS job needs the standard Apple silicon `macos-26` image and Xcode 26.4.1. It has been statically checked, not run on a Mac in this session. The workflow pins action revisions; runner images and toolchain availability should be reviewed when upgrading dependencies.

## Local use

```powershell
.\gradlew.bat :composeApp:wasmJsBrowserDistribution
node scripts/build-docs.mjs
node scripts/serve-docs.mjs
```

The local URL is `http://127.0.0.1:4173/shadcn-mobile/`. `website/dist/` is generated and ignored by Git. Documentation routes use hashes and relative asset paths so a GitHub Pages project subdirectory works without server-side rewrites.

The production Wasm build and static-site checks passed. The complete form, component examples, search, code copying, themes and responsive navigation were exercised in Chrome. See [browser verification and screenshots](web-verification.md) and [native verification](verification.md).

## Publication checks

1. Inspect existing local and remote commit history before committing. For an initial repository without a convention, use a concrete Chinese `type(scope): 描述` subject and the required `Assisted-by: Codex` trailer.
2. If `workflow` scope is still missing, run `gh auth refresh --hostname github.com --scopes workflow` and have the owner complete GitHub's identity verification.
3. Review the exact staged source and exclude local SDK, cache, emulator and generated build directories.
4. Recheck commit descriptions and remote history before pushing the `main` branch.
5. Set Pages source to GitHub Actions, then verify actual Android, iOS and Docs workflow results before reporting them as successful.
6. Open the deployed Pages URL and repeat the browser form and component interactions on the deployed artifact.

No deployment result is inferred from workflow syntax validation or a successful local build.
