# Lestora Highlights

The highlight mod is meant to highlight particular blocks.  It does this for two specific cases:

## Features
- **Highlight Shape**:  Highlights solid blocks within a specified radius sphere. This is a personal test tool I used to get the mod published with some functionality. I use it to "dig a crater". It doesn't actually do anything, but it highlights where you should dig to keep a spherical hole.
- **Highlight Light Emissions**:  This will review nearby spaces and find light-emitting blocks like torches and glowstone.  It will then highlight the boundaries with smart features like tracking, melding nearby light source boundaries, and more.  It will also suggest where you should place new light sources to maximize torch use.

## Manual Installation
1. Download the mod JAR from CurseForge.
2. Place the JAR file into your `mods` folder.
3. Launch Minecraft with the Forge profile.

## Commands
- Use the command `/lestora createSphere <radius>` to highlight a spherical area of breakable blocks from your current location.
- Use the command `/lestora meldBoundaries [true/false]` Default true, set to false to show all boundaries of all torches.  It's not pretty, but good for debugging.
- Use the command `/lestora lightSourceScanDistance <radius 5-100>` Default 41.  The distance around the player to scan for light sources for which to draw boundaries.
- Use the command `/lestora clearHighlights` For debug, especially early in development.  Clears whole-block highlights like torch suggestions and spherical highlights.

## Compatibility
- **Minecraft Version:** 1.21.4
- **Forge Version:** 54.1.0

## Troubleshooting
If you run into issues (e.g., crashes or unexpected behavior), check the logs in your `crash-reports` or `logs` folder. You can also open an issue on the mod’s GitHub repository.

## Contributing
Contributions are welcome! Please submit pull requests or open issues if you have suggestions or bug reports.

## License
This project is licensed under the MIT License.
