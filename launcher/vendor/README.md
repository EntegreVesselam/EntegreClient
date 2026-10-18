# Pinned upstream

Packwiz installer v0.5.14, commit `7420866dfc6ae1f68079a166e9804b7ec31a59ca`.
JAR: https://github.com/packwiz/packwiz-installer/releases/download/v0.5.14/packwiz-installer.jar
SHA256: `c9f646908d340d84773948a9a7d98bc1dae250d35e1016dc6e2b8459760b5598`.
MIT license in `LICENSE`. All original bundled notices remain in the output JAR.

`UpdateManager.kt`, `OptionsSelectWindow.kt`, and `proguard.txt` are unmodified
reference sources from that commit, not compiled by this project.
`InstallWindow.class` and the manifest are replaced; five GUI/main classes
receive exact Turkish CONSTANT_Utf8 replacements through `localize_ui.py`.
All other upstream entries are byte-identical; method bytecode and attributes
of the translated classes are unchanged. The translator rejects an unexpected
source class or a missing literal instead of partially translating a new version.

The migration test fixture `packwiz-installer-bootstrap.jar` is the original
98,989-byte Entegre Client bootstrap, SHA256:
`a8fbb24dc604278e97f4688e82d3d91a318b98efc08d5dbfcbcbcab6443d116c`.
Source project: https://github.com/packwiz/packwiz-installer-bootstrap (MIT).
Exact distributed fixture: https://raw.githubusercontent.com/EntegreVesselam/EntegreClient/156c94b7ba55c03ec18a6df7f578e4f8912bb2d8/packwiz-installer-bootstrap.jar
This fixture is not the new shim and must never be replaced by it in vendor/.

The engine is pinned, not updated automatically from upstream. An Entegre
release may deliver a reviewed newer engine through its own Packwiz channel.
Changing upstream version requires rechecking the ABI, translation literals,
optional-mod behavior, and historical player-file ownership migration tests.
